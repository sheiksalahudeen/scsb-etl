package org.recap.dump;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.etl.BibPersisterCallable;
import org.recap.model.jaxb.*;
import org.recap.model.jaxb.marc.*;
import org.recap.model.jpa.*;
import org.recap.repository.BibliographicDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

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
        JAXBHandler instance = JAXBHandler.getInstance();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(xmlFileName, instance);

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

        BibRecord bibRecord = new BibRecord();
        Bib bib = getBib(instance, fetchedBibliographicEntity);
        bibRecord.setBib(bib);
        List<Holdings> holdings = getHoldings(instance, fetchedBibliographicEntity.getHoldingsEntities());
        bibRecord.setHoldings(holdings);

        String xmlContent = instance.marshal(bibRecord);
        assertNotNull(xmlContent);

        File file = new File(xmlFileName);
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
    }

    @Test
    public void saveAndGenerateDumpForMultipleItems() throws Exception {
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
        JAXBHandler instance = JAXBHandler.getInstance();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(xmlFileName, instance);

        assertNotNull(bibliographicEntity);
        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 1);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 5);

        BibliographicPK bibliographicPK = new BibliographicPK(3, ".b103167134");
        BibliographicEntity fetchedBibliographicEntity = bibliographicDetailsRepository.findOne(bibliographicPK);
        assertNotNull(fetchedBibliographicEntity);
        assertNotNull(fetchedBibliographicEntity.getInstitutionEntity());
        assertNotNull(fetchedBibliographicEntity.getHoldingsEntities());
        assertEquals(fetchedBibliographicEntity.getHoldingsEntities().size(), 1);

        BibRecord bibRecord = new BibRecord();
        Bib bib = getBib(instance, fetchedBibliographicEntity);
        bibRecord.setBib(bib);
        List<Holdings> holdings = getHoldings(instance, fetchedBibliographicEntity.getHoldingsEntities());
        bibRecord.setHoldings(holdings);

        String xmlContent = instance.marshal(bibRecord);
        assertNotNull(xmlContent);

        File file = new File(xmlFileName);
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
    }

    @Test
    public void saveAndGenerateDumpForMultipleHoldings() throws Exception {
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
        JAXBHandler instance = JAXBHandler.getInstance();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(xmlFileName, instance);

        assertNotNull(bibliographicEntity);
        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getHoldingsEntities());
        assertEquals(savedBibliographicEntity.getHoldingsEntities().size(), 2);
        assertNotNull(savedBibliographicEntity.getItemEntities());
        assertEquals(savedBibliographicEntity.getItemEntities().size(), 4);

        BibliographicPK bibliographicPK = new BibliographicPK(3, ".b103167134");
        BibliographicEntity fetchedBibliographicEntity = bibliographicDetailsRepository.findOne(bibliographicPK);
        assertNotNull(fetchedBibliographicEntity);
        assertNotNull(fetchedBibliographicEntity.getInstitutionEntity());
        assertNotNull(fetchedBibliographicEntity.getHoldingsEntities());
        assertEquals(fetchedBibliographicEntity.getHoldingsEntities().size(), 2);

        BibRecord bibRecord = new BibRecord();

        Bib bib = getBib(instance, fetchedBibliographicEntity);
        bibRecord.setBib(bib);

        List<Holdings> holdings = getHoldings(instance, fetchedBibliographicEntity.getHoldingsEntities());
        bibRecord.setHoldings(holdings);

        String xmlContent = instance.marshal(bibRecord);
        assertNotNull(xmlContent);

        File file = new File(xmlFileName);
        FileUtils.writeStringToFile(file, xmlContent);
        assertTrue(file.exists());
    }

    private BibliographicEntity getBibliographicEntity(String xmlFileName, JAXBHandler instance) throws URISyntaxException, IOException {
        XmlRecordEntity xmlRecordEntity = new XmlRecordEntity();
        xmlRecordEntity.setXmlFileName(xmlFileName);

        URL resource = getClass().getResource(xmlFileName);
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);
        assertTrue(file.exists());
        BibRecord bibRecord = null;
        bibRecord = (BibRecord) instance.unmarshal(FileUtils.readFileToString(file, "UTF-8"), BibRecord.class);
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

    private Bib getBib(JAXBHandler instance, BibliographicEntity fetchedBibliographicEntity) {
        Bib bib = new Bib();
        bib.setOwningInstitutionBibId(fetchedBibliographicEntity.getOwningInstitutionBibId());
        bib.setOwningInstitutionId(fetchedBibliographicEntity.getInstitutionEntity().getInstitutionCode());
        ContentType contentType = getContentType(instance, fetchedBibliographicEntity.getContent());
        bib.setContent(contentType);
        return bib;
    }

    private List<Holdings> getHoldings(JAXBHandler instance, List<HoldingsEntity> holdingsEntityList) {
        List<Holdings> holdingsList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(holdingsEntityList)) {
            for (HoldingsEntity holdingsEntity : holdingsEntityList) {
                Holdings holdings = new Holdings();
                Holding holding = new Holding();

                holding.setOwningInstitutionHoldingsId(holdingsEntity.getOwningInstitutionHoldingsId());

                ContentType contentType = getContentType(instance, holdingsEntity.getContent());
                holding.setContent(contentType);

                Items items = getItems(holdingsEntity.getItemEntities());
                holding.setItems(Arrays.asList(items));

                holdings.setHolding(Arrays.asList(holding));
                holdingsList.add(holdings);
            }
        }
        return holdingsList;
    }

    private Items getItems(List<ItemEntity> itemEntities) {
        Items items = new Items();
        ContentType itemContentType = new ContentType();
        CollectionType collectionType = new CollectionType();
        collectionType.setRecord(buildRecordTypes(itemEntities));
        itemContentType.setCollection(collectionType);
        items.setContent(itemContentType);
        return items;
    }

    private List<RecordType> buildRecordTypes(List<ItemEntity> itemEntities) {
        List<RecordType> recordTypes = new ArrayList<>();
        if (!CollectionUtils.isEmpty(itemEntities)) {
            for (ItemEntity itemEntity : itemEntities) {
                RecordType recordType = new RecordType();
                List<DataFieldType> dataFieldTypeList = new ArrayList<>();
                dataFieldTypeList.add(build876DataField(itemEntity));
                dataFieldTypeList.add(build900DataField(itemEntity));
                recordType.setDatafield(dataFieldTypeList);
                recordTypes.add(recordType);
            }
        }
        return recordTypes;
    }

    private DataFieldType build900DataField(ItemEntity itemEntity) {
        DataFieldType dataFieldType = new DataFieldType();
        List<SubfieldatafieldType> subfieldatafieldTypes = new ArrayList<>();
        dataFieldType.setTag("900");
        dataFieldType.setInd1(" ");
        dataFieldType.setInd2(" ");
        subfieldatafieldTypes.add(getSubfieldatafieldType("a", itemEntity.getCollectionGroupEntity().getCollectionGroupCode()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("b", itemEntity.getCustomerCode()));
        dataFieldType.setSubfield(subfieldatafieldTypes);
        return dataFieldType;
    }

    private DataFieldType build876DataField(ItemEntity itemEntity) {
        DataFieldType dataFieldType = new DataFieldType();
        List<SubfieldatafieldType> subfieldatafieldTypes = new ArrayList<>();
        dataFieldType.setTag("876");
        dataFieldType.setInd1(" ");
        dataFieldType.setInd2(" ");
        subfieldatafieldTypes.add(getSubfieldatafieldType("p", itemEntity.getBarcode()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("h", itemEntity.getUseRestrictions()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("a", itemEntity.getOwningInstitutionItemId()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("j", itemEntity.getItemStatusEntity().getStatusCode()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("t", itemEntity.getCopyNumber().toString()));
        subfieldatafieldTypes.add(getSubfieldatafieldType("3", itemEntity.getVolumePartYear()));
        dataFieldType.setSubfield(subfieldatafieldTypes);
        return dataFieldType;
    }

    private SubfieldatafieldType getSubfieldatafieldType(String code, String value) {
        SubfieldatafieldType subfieldatafieldType = new SubfieldatafieldType();
        subfieldatafieldType.setCode(code);
        subfieldatafieldType.setValue(value);
        return subfieldatafieldType;
    }

    private ContentType getContentType(JAXBHandler instance, byte[] byteContent) {
        String content = new String(byteContent, Charset.forName("UTF-8"));
        CollectionType collectionType = (CollectionType) instance.unmarshal(content, CollectionType.class);
        ContentType contentType = new ContentType();
        contentType.setCollection(collectionType);
        return contentType;
    }

}
