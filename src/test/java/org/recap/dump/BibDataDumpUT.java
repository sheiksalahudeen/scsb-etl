package org.recap.dump;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.BibliographicPK;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.util.DataDumpUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by chenchulakshmig on 3/8/16.
 */
public class BibDataDumpUT extends BaseTestCase {

    @Mock
    private Map<String, Integer> institutionMap;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Mock
    private Map<String, Integer> collectionGroupMap;

    @Mock
    private Map itemStatusMap;

    @PersistenceContext
    private EntityManager entityManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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

        BibRecord bibRecord = dataDumpUtil.getBibRecord(fetchedBibliographicEntity);

        String xmlContent = JAXBHandler.getInstance().marshal(bibRecord);
        assertNotNull(xmlContent);

        File file = new File(xmlFileName);
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

        BibRecord bibRecord = dataDumpUtil.getBibRecord(fetchedBibliographicEntity);

        String xmlContent = JAXBHandler.getInstance().marshal(bibRecord);
        assertNotNull(xmlContent);

        File file = new File(xmlFileName);
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

        BibRecord bibRecord = dataDumpUtil.getBibRecord(fetchedBibliographicEntity);

        String xmlContent = JAXBHandler.getInstance().marshal(bibRecord);
        assertNotNull(xmlContent);

        File file = new File(xmlFileName);
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
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
        bibRecord = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord);

        BibliographicEntity bibliographicEntity = null;

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable();
        bibPersisterCallable.setItemStatusMap(itemStatusMap);
        bibPersisterCallable.setInstitutionEntitiesMap(institutionMap);
        bibPersisterCallable.setCollectionGroupMap(collectionGroupMap);
        bibPersisterCallable.setXmlRecordEntity(xmlRecordEntity);
        bibPersisterCallable.setBibRecord(bibRecord);
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity = (BibliographicEntity) object;
            }
        }
        return bibliographicEntity;
    }

    @Test
    public void saveAndGenerateDumpForMultipleRecords() throws Exception {
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

        BibliographicEntity bibliographicEntity1 = getBibliographicEntity("singleRecord.xml");
        assertNotNull(bibliographicEntity1);
        BibliographicEntity savedBibliographicEntity1 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity1);
        entityManager.refresh(savedBibliographicEntity1);
        assertNotNull(savedBibliographicEntity1);

        BibliographicEntity bibliographicEntity2 = getBibliographicEntity("BibHoldingsMultipleItems.xml");
        assertNotNull(bibliographicEntity2);
        BibliographicEntity savedBibliographicEntity2 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity2);
        entityManager.refresh(savedBibliographicEntity2);
        assertNotNull(savedBibliographicEntity2);

        BibRecords bibRecords = dataDumpUtil.getBibRecords(Arrays.asList(savedBibliographicEntity1, savedBibliographicEntity2));
        String xmlContent = JAXBHandler.getInstance().marshal(bibRecords);
        assertNotNull(xmlContent);

        File file = new File("multipleRecords.xml");
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
    }

}
