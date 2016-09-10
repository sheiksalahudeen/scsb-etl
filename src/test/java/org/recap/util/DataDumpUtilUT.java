package org.recap.util;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.Holding;
import org.recap.model.jaxb.Holdings;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.jpa.*;
import org.recap.repository.BibliographicDetailsRepository;
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

import static org.junit.Assert.*;

/**
 * Created by premkb on 23/8/16.
 */
public class DataDumpUtilUT extends BaseTestCase {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpUtilUT.class);

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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    DBReportUtil dbReportUtil;

    private String bibContent = "<collection>\n"+
            "                <record>\n"+
            "                    <controlfield tag=\"001\">NYPG002000036-B</controlfield>\n"+
            "                    <controlfield tag=\"005\">20001116192424.2</controlfield>\n"+
            "                    <controlfield tag=\"008\">850225r19731907nyu b 001 0 ara</controlfield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"010\">\n"+
            "                        <subfield code=\"a\">   77173005  </subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"040\">\n"+
            "                        <subfield code=\"c\">NN</subfield>\n"+
            "                        <subfield code=\"d\">NN</subfield>\n"+
            "                        <subfield code=\"d\">CStRLIN</subfield>\n"+
            "                        <subfield code=\"d\">WaOLN</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"043\">\n"+
            "                        <subfield code=\"a\">ff-----</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\"0\" ind2=\"0\" tag=\"050\">\n"+
            "                        <subfield code=\"a\">DS36.6</subfield>\n"+
            "                        <subfield code=\"b\">.I26 1973</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\"0\" ind2=\"0\" tag=\"082\">\n"+
            "                        <subfield code=\"a\">910.031/767</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\"1\" ind2=\" \" tag=\"100\">\n"+
            "                        <subfield code=\"a\">Ibn Jubayr, MuhÌ£ammad ibn AhÌ£mad,</subfield>\n"+
            "                        <subfield code=\"d\">1145-1217.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\"1\" ind2=\"0\" tag=\"245\">\n"+
            "                        <subfield code=\"a\">RihÌ£lat</subfield>\n"+
            "                        <subfield code=\"b\">AbÄ« al-Husayn Muhammad ibn Ahmad ibn Jubayr al-KinÄ\u0081nÄ« al-AndalusÄ«\n"+
            "                            al-BalinsÄ«.\n"+
            "                        </subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"250\">\n"+
            "                        <subfield code=\"a\">2d ed.</subfield>\n"+
            "                        <subfield code=\"b\">rev. by M. J. de Goeje and printed for the Trustees of the \"E. J. W. Gibb\n"+
            "                            memorial\"\n"+
            "                        </subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"260\">\n"+
            "                        <subfield code=\"a\">[New York,</subfield>\n"+
            "                        <subfield code=\"b\">AMS Press,</subfield>\n"+
            "                        <subfield code=\"c\">1973] 1907.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"300\">\n"+
            "                        <subfield code=\"a\">363, 53 p.</subfield>\n"+
            "                        <subfield code=\"c\">23 cm.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"500\">\n"+
            "                        <subfield code=\"a\">Added t.p.: The travels of Ibn Jubayr. Edited from a ms. in the University\n"+
            "                            Library of Leyden by William Wright.\n"+
            "                        </subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"500\">\n"+
            "                        <subfield code=\"a\">Original ed. issued as v. 5 of \"E.J.W. Gibb memorial\" series.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"504\">\n"+
            "                        <subfield code=\"a\">Includes bibliographical references and index.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\"0\" tag=\"651\">\n"+
            "                        <subfield code=\"a\">Islamic Empire</subfield>\n"+
            "                        <subfield code=\"x\">Description and travel.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\"1\" ind2=\" \" tag=\"700\">\n"+
            "                        <subfield code=\"a\">Wright, William,</subfield>\n"+
            "                        <subfield code=\"d\">1830-1889.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\"1\" ind2=\" \" tag=\"700\">\n"+
            "                        <subfield code=\"a\">Goeje, M. J. de</subfield>\n"+
            "                        <subfield code=\"q\">(Michael Jan),</subfield>\n"+
            "                        <subfield code=\"d\">1836-1909.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\"0\" ind2=\" \" tag=\"740\">\n"+
            "                        <subfield code=\"a\">Travels of Ibn Jubayr.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\"0\" tag=\"830\">\n"+
            "                        <subfield code=\"a\">\"E.J.W. Gibb memorial\" series ;</subfield>\n"+
            "                        <subfield code=\"v\">v.5.</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"907\">\n"+
            "                        <subfield code=\"a\">.b100006279</subfield>\n"+
            "                        <subfield code=\"c\">m</subfield>\n"+
            "                        <subfield code=\"d\">a</subfield>\n"+
            "                        <subfield code=\"e\">-</subfield>\n"+
            "                        <subfield code=\"f\">ara</subfield>\n"+
            "                        <subfield code=\"g\">nyu</subfield>\n"+
            "                        <subfield code=\"h\">0</subfield>\n"+
            "                        <subfield code=\"i\">3</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"952\">\n"+
            "                        <subfield code=\"h\">*OAC (\"E. J. W. Gibb memorial\" series. v. 5)</subfield>\n"+
            "                    </datafield>\n"+
            "                    <datafield ind1=\" \" ind2=\" \" tag=\"952\">\n"+
            "                        <subfield code=\"h\">*OFV 87-659</subfield>\n"+
            "                    </datafield>\n"+
            "                    <leader>01814cam a2200409 450000</leader>\n"+
            "                </record>\n"+
            "            </collection>";

    private String holdingContent = "<collection>\n" +
            "                    <record>\n" +
            "                        <datafield ind1='0' ind2='1' tag='852'>\n" +
            "                            <subfield code='b'>rcppa</subfield>\n" +
            "                            <subfield code='t'>1</subfield>\n" +
            "                            <subfield code='h'>DF802</subfield>\n" +
            "                            <subfield code='i'>.xP45</subfield>\n" +
            "                            <subfield code='x'>tr fr anxafst</subfield>\n" +
            "                        </datafield>\n" +
            "                        <datafield ind1=' ' ind2='0' tag='866'>\n" +
            "                            <subfield code='x'>DESIGNATOR: t.</subfield>\n" +
            "                        </datafield>\n" +
            "                    </record>\n" +
            "                </collection>";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getBibRecords() throws Exception{
        DataDumpUtil dataDumpUtil = new DataDumpUtil();
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        BibRecords bibRecords = dataDumpUtil.getBibRecords(Arrays.asList(getBibliographicEntity()));
        assertNotNull(bibRecords);
        assertNotNull(bibRecords.getBibRecords());
        assertNotNull(bibRecords.getBibRecords().get(0).getBib());
        assertEquals("1",bibRecords.getBibRecords().get(0).getBib().getOwningInstitutionBibId());
        assertEquals("NYPL",bibRecords.getBibRecords().get(0).getBib().getOwningInstitutionId());
    }

    private BibliographicEntity getBibliographicEntity() throws URISyntaxException, IOException {
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent(bibContent.getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionBibId("1");
        bibliographicEntity.setOwningInstitutionId(3);
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionId(1);
        institutionEntity.setInstitutionCode("NYPL");
        institutionEntity.setInstitutionName("New York Public Library");
        bibliographicEntity.setInstitutionEntity(institutionEntity);
        return bibliographicEntity;
    }

    @Test
    public void saveAndGenerateDump() throws Exception {
        DataDumpUtil dataDumpUtil = new DataDumpUtil();
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Open")).thenReturn(2);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Open", 2);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        String xmlFileName = "singleRecord.xml";
        BibliographicEntity bibliographicEntity = getBibliographicEntity(xmlFileName);

        assertNotNull(bibliographicEntity);
        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 1);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 1);

        BibliographicPK bibliographicPK = new BibliographicPK(3, ".b103167134");
        BibliographicEntity fetchedBibliographicEntity = bibliographicDetailsRepository.findOne(bibliographicPK);
        assertNotNull(fetchedBibliographicEntity);
        assertNotNull(fetchedBibliographicEntity.getInstitutionEntity());
        assertNotNull(fetchedBibliographicEntity.getHoldingsEntities());
        assertEquals(fetchedBibliographicEntity.getHoldingsEntities().size(), 1);

        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        BibRecords bibRecords = dataDumpUtil.getBibRecords(Arrays.asList(getBibliographicEntity()));

        String xmlContent = JAXBHandler.getInstance().marshal(bibRecords);
        assertNotNull(xmlContent);

        File file = new File(dumpDirectoryPath + File.separator + xmlFileName);
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
    }

    @Test
    public void saveAndGenerateDumpForMultipleItems() throws Exception {
        DataDumpUtil dataDumpUtil = new DataDumpUtil();
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Shared")).thenReturn(1);
        Mockito.when(collectionGroupMap.containsKey("Shared")).thenReturn(true);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Shared", 1);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        String xmlFileName = "BibHoldingsMultipleItems.xml";
        BibliographicEntity bibliographicEntity = getBibliographicEntity(xmlFileName);

        assertNotNull(bibliographicEntity);
        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 1);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 5);

        BibliographicPK bibliographicPK = new BibliographicPK(3, ".b103167135");
        BibliographicEntity fetchedBibliographicEntity = bibliographicDetailsRepository.findOne(bibliographicPK);
        assertNotNull(fetchedBibliographicEntity);
        assertNotNull(fetchedBibliographicEntity.getInstitutionEntity());
        assertNotNull(fetchedBibliographicEntity.getHoldingsEntities());
        assertEquals(fetchedBibliographicEntity.getHoldingsEntities().size(), 1);

        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        BibRecords bibRecords = dataDumpUtil.getBibRecords(Arrays.asList(fetchedBibliographicEntity));

        String xmlContent = JAXBHandler.getInstance().marshal(bibRecords);
        assertNotNull(xmlContent);

        File file = new File(dumpDirectoryPath + File.separator + xmlFileName);
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
    }

    @Test
    public void saveAndGenerateDumpForMultipleHoldings() throws Exception {
        DataDumpUtil dataDumpUtil = new DataDumpUtil();
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Shared")).thenReturn(1);
        Mockito.when(collectionGroupMap.containsKey("Shared")).thenReturn(true);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Shared", 1);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        String xmlFileName = "BibMultipleHoldingsItems.xml";
        BibliographicEntity bibliographicEntity = getBibliographicEntity(xmlFileName);

        assertNotNull(bibliographicEntity);
        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 2);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 4);

        BibliographicPK bibliographicPK = new BibliographicPK(3, ".b103167136");
        BibliographicEntity fetchedBibliographicEntity = bibliographicDetailsRepository.findOne(bibliographicPK);
        assertNotNull(fetchedBibliographicEntity);
        assertNotNull(fetchedBibliographicEntity.getInstitutionEntity());
        assertNotNull(fetchedBibliographicEntity.getHoldingsEntities());
        assertEquals(fetchedBibliographicEntity.getHoldingsEntities().size(), 2);

        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        BibRecords bibRecords = dataDumpUtil.getBibRecords(Arrays.asList(fetchedBibliographicEntity));

        String xmlContent = JAXBHandler.getInstance().marshal(bibRecords);
        assertNotNull(xmlContent);

        File file = new File(dumpDirectoryPath + File.separator + xmlFileName);
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
    }

    @Test
    public void avoidPrivate()throws Exception{
        DataDumpUtil dataDumpUtil = new DataDumpUtil();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent(bibContent.getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId("12345");
        bibliographicEntity.setOwningInstitutionId(1);

        HoldingsEntity holdingsEntity1 = new HoldingsEntity();
        holdingsEntity1.setContent(holdingContent.getBytes());
        holdingsEntity1.setCreatedDate(new Date());
        holdingsEntity1.setCreatedBy("etl");
        holdingsEntity1.setLastUpdatedDate(new Date());
        holdingsEntity1.setLastUpdatedBy("etl");
        holdingsEntity1.setOwningInstitutionId(1);
        holdingsEntity1.setOwningInstitutionHoldingsId("54321");

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setCallNumberType("0");
        itemEntity1.setCallNumber("callNum");
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setBarcode("1231");
        itemEntity1.setOwningInstitutionItemId(".i1231");
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCustomerCode("PA");
        itemEntity1.setCopyNumber(1);
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setHoldingsEntity(holdingsEntity1);

        HoldingsEntity holdingsEntity2 = new HoldingsEntity();
        holdingsEntity2.setContent(holdingContent.getBytes());
        holdingsEntity2.setCreatedDate(new Date());
        holdingsEntity2.setCreatedBy("etl");
        holdingsEntity2.setLastUpdatedDate(new Date());
        holdingsEntity2.setLastUpdatedBy("etl");
        holdingsEntity2.setOwningInstitutionId(1);
        holdingsEntity2.setOwningInstitutionHoldingsId("54322");

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setCallNumberType("0");
        itemEntity2.setCallNumber("callNum");
        itemEntity2.setCreatedDate(new Date());
        itemEntity2.setCreatedBy("etl");
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setLastUpdatedBy("etl");
        itemEntity2.setBarcode("1232");
        itemEntity2.setOwningInstitutionItemId(".i1232");
        itemEntity2.setOwningInstitutionId(1);
        itemEntity2.setCollectionGroupId(2);
        itemEntity2.setCustomerCode("NA");
        itemEntity2.setCopyNumber(2);
        itemEntity2.setItemAvailabilityStatusId(1);
        itemEntity2.setHoldingsEntity(holdingsEntity2);

        HoldingsEntity holdingsEntity3 = new HoldingsEntity();
        holdingsEntity3.setContent(holdingContent.getBytes());
        holdingsEntity3.setCreatedDate(new Date());
        holdingsEntity3.setCreatedBy("etl");
        holdingsEntity3.setLastUpdatedDate(new Date());
        holdingsEntity3.setLastUpdatedBy("etl");
        holdingsEntity3.setOwningInstitutionId(1);
        holdingsEntity3.setOwningInstitutionHoldingsId("54323");

        ItemEntity itemEntity3 = new ItemEntity();
        itemEntity3.setCallNumberType("0");
        itemEntity3.setCallNumber("callNum");
        itemEntity3.setCreatedDate(new Date());
        itemEntity3.setCreatedBy("etl");
        itemEntity3.setLastUpdatedDate(new Date());
        itemEntity3.setLastUpdatedBy("etl");
        itemEntity3.setBarcode("1233");
        itemEntity3.setOwningInstitutionItemId(".i1233");
        itemEntity3.setOwningInstitutionId(1);
        itemEntity3.setCollectionGroupId(3);
        itemEntity3.setCustomerCode("PG");
        itemEntity3.setCopyNumber(3);
        itemEntity3.setItemAvailabilityStatusId(1);
        itemEntity3.setHoldingsEntity(holdingsEntity3);

        bibliographicEntity.setHoldingsEntities(new ArrayList<>());
        bibliographicEntity.getHoldingsEntities().add(holdingsEntity1);
        bibliographicEntity.getHoldingsEntities().add(holdingsEntity2);
        bibliographicEntity.getHoldingsEntities().add(holdingsEntity3);
        bibliographicEntity.setItemEntities(new ArrayList<>());
        bibliographicEntity.getItemEntities().add(itemEntity1);
        bibliographicEntity.getItemEntities().add(itemEntity2);
        bibliographicEntity.getItemEntities().add(itemEntity3);

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);

        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());

        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        BibRecords bibRecords = dataDumpUtil.getBibRecords(Arrays.asList(savedBibliographicEntity));
        List<BibRecord> bibRecordList = bibRecords.getBibRecords();
        assertEquals(2,bibRecordList.get(0).getHoldings().size());
    }

    private BibliographicEntity getBibliographicEntity(String xmlFileName) throws URISyntaxException, IOException {
        XmlRecordEntity xmlRecordEntity = new XmlRecordEntity();
        xmlRecordEntity.setXmlFileName(xmlFileName);

        URL resource = getClass().getResource(xmlFileName);
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord = null;
        try {
            bibRecord = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        assertNotNull(bibRecord);

        BibliographicEntity bibliographicEntity = null;

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable();
        bibPersisterCallable.setItemStatusMap(itemStatusMap);
        bibPersisterCallable.setInstitutionEntitiesMap(institutionMap);
        bibPersisterCallable.setCollectionGroupMap(collectionGroupMap);
        bibPersisterCallable.setXmlRecordEntity(xmlRecordEntity);
        bibPersisterCallable.setBibRecord(bibRecord);
        bibPersisterCallable.setDBReportUtil(dbReportUtil);
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity = (BibliographicEntity) object;
            }
        }
        return bibliographicEntity;
    }
}
