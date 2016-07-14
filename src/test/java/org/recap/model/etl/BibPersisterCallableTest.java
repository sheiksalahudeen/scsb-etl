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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

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
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("loadReportEntity");
            if (object != null) {
                loadReportEntities.add((LoadReportEntity) object);
            }
        }

        assertEquals(loadReportEntities.size(), 1);
        if (!CollectionUtils.isEmpty(loadReportEntities)) {
            csvUtil.writeToCsv(loadReportEntities);
        }
    }

    @Test
    public void loadBibHoldingsMultipleItems() throws Exception {
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

        BibliographicEntity bibliographicEntity = null;

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord1, institutionMap, itemStatusMap, collectionGroupMap);
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity = (BibliographicEntity) object;
            }
        }
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
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 5);
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
        BibRecord bibRecord = null;
        bibRecord = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord);

        BibliographicEntity bibliographicEntity = null;

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord, institutionMap, itemStatusMap, collectionGroupMap);
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity = (BibliographicEntity) object;
            }
        }

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
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 2);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 3);
    }

    //TODO : need to fix constraint in bibliographic_item_t
    @Test
    @Ignore
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
        BibRecord bibRecord = null;
        bibRecord = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord);

        BibliographicEntity bibliographicEntity = null;

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord, institutionMap, itemStatusMap, collectionGroupMap);
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity = (BibliographicEntity) object;
            }
        }

        assertNotNull(bibliographicEntity);
        assertEquals(bibliographicEntity.getHoldingsEntities().size(), 2);
        assertEquals(bibliographicEntity.getItemEntities().size(), 4);

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 2);
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 2);

    }

    @Test
    @Ignore
    public void duplicateBibsWithDifferentItems() throws Exception {
        Mockito.when(institutionMap.get("NYPL")).thenReturn(3);
        Mockito.when(itemStatusMap.get("Available")).thenReturn(1);
        Mockito.when(collectionGroupMap.get("Open")).thenReturn(2);

        Map<String, Integer> institution = new HashMap<>();
        institution.put("NYPL", 3);
        Mockito.when(institutionMap.entrySet()).thenReturn(institution.entrySet());

        Map<String, Integer> collection = new HashMap<>();
        collection.put("Open", 2);
        Mockito.when(collectionGroupMap.entrySet()).thenReturn(collection.entrySet());

        URL resource = getClass().getResource("sampleRecord1.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord1 = null;
        bibRecord1 = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord1);

        BibliographicEntity bibliographicEntity1 = null;

        BibPersisterCallable bibPersisterCallable = new BibPersisterCallable(bibRecord1, institutionMap, itemStatusMap, collectionGroupMap);
        Map<String, Object> map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity1 = (BibliographicEntity) object;
            }
        }

        resource = getClass().getResource("sampleRecord2.xml");
        assertNotNull(resource);
        file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord2 = null;
        bibRecord2 = (BibRecord) JAXBHandler.getInstance().unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
        assertNotNull(bibRecord2);

        BibliographicEntity bibliographicEntity2 = null;

        bibPersisterCallable = new BibPersisterCallable(bibRecord2, institutionMap, itemStatusMap, collectionGroupMap);
        map = (Map<String, Object>) bibPersisterCallable.call();
        if (map != null) {
            Object object = map.get("bibliographicEntity");
            if (object != null) {
                bibliographicEntity2 = (BibliographicEntity) object;
            }
        }

        assertNotNull(bibliographicEntity1);
        assertNotNull(bibliographicEntity2);

        BibliographicEntity savedBibliographicEntity1 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity1);
        entityManager.refresh(savedBibliographicEntity1);
        assertNotNull(savedBibliographicEntity1);
        assertNotNull(savedBibliographicEntity1.getBibliographicId());

        BibliographicEntity savedBibliographicEntity2 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity2);
        entityManager.refresh(savedBibliographicEntity2);
        assertNotNull(savedBibliographicEntity2);
        assertNotNull(savedBibliographicEntity2.getBibliographicId());

        BibliographicEntity byOwningInstitutionBibId = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(3, ".b153286131");
        assertNotNull(byOwningInstitutionBibId);
        assertEquals(byOwningInstitutionBibId.getHoldingsEntities().size(), 2);
        assertEquals(byOwningInstitutionBibId.getItemEntities().size(), 2);
    }


}