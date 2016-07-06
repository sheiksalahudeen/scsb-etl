package org.recap.model.etl;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.route.ETLExchange;
import org.recap.util.CsvUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by chenchulakshmig on 1/7/16.
 */
public class BibPersisterCallableTest extends BaseTestCase {

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    CsvUtil csvUtil;

    @Mock
    private Map<String, Integer> institutionMap;

    @Mock
    private Map itemStatusMap;

    @Mock
    private Map<String, Integer> collectionGroupMap;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void newInstance() {
        BibRecord bibRecord = new BibRecord();
        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord, institutionMap, itemStatusMap, collectionGroupMap);
        assertNotNull(bibPersisterCallable);
    }

    @Test
    public void checkNullConstraints() throws Exception {
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Open")).thenReturn(2);

        List<LoadReportEntity> loadReportEntities = new ArrayList<>();

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Open", 2);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        URL resource = getClass().getResource("BibWithoutItemBarcode.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord = null;
        bibRecord = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord);

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord, institutionMap, itemStatusMap, collectionGroupMap);
        Object object = bibPersisterCallable.call();
        assertNotNull(object);
        if (object instanceof LoadReportEntity) {
            loadReportEntities.add((LoadReportEntity) object);
        }

        assertEquals(loadReportEntities.size(), 1);
        if (!CollectionUtils.isEmpty(loadReportEntities)) {
            csvUtil.writeToCsv(loadReportEntities);
        }
    }

    @Test
    public void checkUniqueConstraints() throws Exception {
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Open")).thenReturn(2);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Open", 2);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        URL resource = getClass().getResource("BibHoldingsMultipleItems.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord1 = null;
        bibRecord1 = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord1);

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord1, institutionMap, itemStatusMap, collectionGroupMap);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) bibPersisterCallable.call();

        assertNotNull(bibliographicEntity);
        assertEquals(bibliographicEntity.getHoldingsEntities().size(), 1);
        assertEquals(bibliographicEntity.getItemEntities().size(), 5);

        assertNotNull(bibliographicDetailsRepository);
        assertNotNull(producer);

        ETLExchange etlExchange = new ETLExchange();
        etlExchange.setBibliographicEntities(Arrays.asList(bibliographicEntity));
        etlExchange.setInstitutionEntityMap(new HashMap());
        etlExchange.setCollectionGroupMap(new HashMap());

        producer.sendBody("activemq:queue:etlLoadQ", etlExchange);

        Thread.sleep(1000);

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 1);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 4);
    }

    @Test
    public void checkDataTruncateIssue() throws Exception {
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Open")).thenReturn(2);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Open", 2);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        URL resource = getClass().getResource("BibMultipleHoldingsItems.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord1 = null;
        bibRecord1 = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord1);

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord1, institutionMap, itemStatusMap, collectionGroupMap);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) bibPersisterCallable.call();

        assertNotNull(bibliographicEntity);
        assertEquals(bibliographicEntity.getHoldingsEntities().size(), 2);
        assertEquals(bibliographicEntity.getItemEntities().size(), 4);

        assertNotNull(bibliographicDetailsRepository);
        assertNotNull(producer);

        ETLExchange etlExchange = new ETLExchange();
        etlExchange.setBibliographicEntities(Arrays.asList(bibliographicEntity));
        etlExchange.setInstitutionEntityMap(new HashMap());
        etlExchange.setCollectionGroupMap(new HashMap());

        producer.sendBody("activemq:queue:etlLoadQ", etlExchange);

        Thread.sleep(1000);

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 1);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 2);
        assertEquals(bibliographicEntity.getHoldingsEntities().get(0).getItemEntities().size(), 2);
    }

    //TODO : need to fix constraint in bibliographic_item_t
    @Ignore
    @Test
    public void duplicateItemsForSingleBib() throws Exception {
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Open")).thenReturn(2);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Open", 2);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        URL resource = getClass().getResource("BibWithDuplicateItems.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord1 = null;
        bibRecord1 = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord1);

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord1, institutionMap, itemStatusMap, collectionGroupMap);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) bibPersisterCallable.call();
        assertNotNull(bibliographicEntity);
        assertEquals(bibliographicEntity.getHoldingsEntities().size(), 2);
        assertEquals(bibliographicEntity.getItemEntities().size(), 4);

        bibliographicDetailsRepository.save(bibliographicEntity);
        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(), bibliographicEntity.getOwningInstitutionBibId());
        assertNotNull(savedBibliographicEntity);
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 2);
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 2);

    }


}