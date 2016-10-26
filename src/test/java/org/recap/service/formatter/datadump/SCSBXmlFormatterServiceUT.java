package org.recap.service.formatter.datadump;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.camel.BibDataProcessor;
import org.recap.camel.ETLExchange;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.InstitutionEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.ReportDetailRepository;
import org.recap.util.DBReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by premkb on 23/8/16.
 */
public class SCSBXmlFormatterServiceUT extends BaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(SCSBXmlFormatterServiceUT.class);

    @Autowired
    BibDataProcessor bibDataProcessor;

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Value("${etl.report.directory}")
    private String reportDirectoryPath;
    @Autowired
    private ProducerTemplate producer;

    @Mock
    private Map itemStatusMap;

    @Mock
    private Map<String, Integer> institutionMap;

    @Mock
    private Map<String, Integer> collectionGroupMap;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Autowired
    DBReportUtil dbReportUtil;


    @Autowired
    SCSBXmlFormatterService scsbXmlFormatterService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void verifySCSBXmlGeneration() throws Exception {
        Mockito.when(institutionMap.get("PUL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Open")).thenReturn(2);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("PUL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Open", 2);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        XmlRecordEntity xmlRecordEntity = new XmlRecordEntity();
        xmlRecordEntity.setXmlFileName("princeton.xml");

        URL resource = getClass().getResource("princeton.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord = null;
        bibRecord = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord);

        BibliographicEntity bibliographicEntity = null;

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable();
        bibPersisterCallable.setItemStatusMap(itemStatusMap);
        bibPersisterCallable.setInstitutionEntitiesMap(institutionMap);
        bibPersisterCallable.setCollectionGroupMap(collectionGroupMap);
        bibPersisterCallable.setXmlRecordEntity(xmlRecordEntity);
        bibPersisterCallable.setBibRecord(bibRecord);
        bibPersisterCallable.setDBReportUtil(dbReportUtil);
        bibPersisterCallable.setInstitutionName("PUL");
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity = (BibliographicEntity) object;
            }
        }

        assertNotNull(bibliographicEntity);
        assertEquals(bibliographicEntity.getHoldingsEntities().size(), 1);
        assertEquals(bibliographicEntity.getItemEntities().size(), 1);

        assertNotNull(bibliographicDetailsRepository);

        assertNotNull(producer);

        ETLExchange etlExchange = new ETLExchange();
        etlExchange.setBibliographicEntities(Arrays.asList(bibliographicEntity));
        etlExchange.setInstitutionEntityMap(new HashMap());
        etlExchange.setCollectionGroupMap(new HashMap());
        bibDataProcessor.setXmlFileName("princeton.xml");
        bibDataProcessor.setInstitutionName("PUL");

        bibDataProcessor.processETLExchagneAndPersistToDB(etlExchange);

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 1);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 1);

        java.lang.Thread.sleep(500);

        List<ReportEntity> reportEntities = reportDetailRepository.findByFileNameAndInstitutionNameAndType(bibDataProcessor.getXmlFileName(), bibDataProcessor.getInstitutionName(), ReCAPConstants.FAILURE);
        assertNotNull(reportEntities);


        Map<String, Object> formattedOutput = (Map<String, Object>) scsbXmlFormatterService.getFormattedOutput(Arrays.asList(savedBibliographicEntity));
        String formattedString = (String) formattedOutput.get("formattedString");

        System.out.println(formattedString);


    }
}
