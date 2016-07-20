package org.recap.model.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by chenchulakshmig on 23/6/16.
 */
public class BibliographicEntityTest extends BaseTestCase {

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    HoldingsDetailsRepository holdingsDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void saveBibSingleHoldings() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        String owningInstitutionBibId = String.valueOf(random.nextInt());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);


        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setCallNumberType("0");
        itemEntity.setCallNumber("callNum");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("etl");
        itemEntity.setBarcode("1231");
        itemEntity.setOwningInstitutionItemId(".i1231");
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCustomerCode("PA");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        holdingsEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedEntity);

        BibliographicPK bibliographicPK = new BibliographicPK();
        bibliographicPK.setOwningInstitutionId(1);
        bibliographicPK.setOwningInstitutionBibId(owningInstitutionBibId);
        BibliographicEntity byBibliographicPK = bibliographicDetailsRepository.findOne(bibliographicPK);

        assertNotNull(byBibliographicPK);
        assertNotNull(byBibliographicPK.getBibliographicId());
        assertNotNull(byBibliographicPK.getLastUpdatedBy());
        assertNotNull(byBibliographicPK.getCreatedBy());
        assertNotNull(byBibliographicPK.getHoldingsEntities().get(0).getHoldingsId());
    }

    @Test
    public void saveBibMultipleHoldings() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity1 = new HoldingsEntity();
        holdingsEntity1.setContent("mock holdings".getBytes());
        holdingsEntity1.setCreatedDate(new Date());
        holdingsEntity1.setCreatedBy("etl");
        holdingsEntity1.setLastUpdatedDate(new Date());
        holdingsEntity1.setLastUpdatedBy("etl");
        holdingsEntity1.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity2 = new HoldingsEntity();
        holdingsEntity2.setContent("mock holdings".getBytes());
        holdingsEntity2.setCreatedDate(new Date());
        holdingsEntity2.setCreatedBy("etl");
        holdingsEntity2.setLastUpdatedDate(new Date());
        holdingsEntity2.setLastUpdatedBy("etl");
        holdingsEntity2.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity1, holdingsEntity2));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);

        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(1).getHoldingsId());
    }

    @Test
    public void saveMultipleBibSingleHoldings() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content".getBytes());
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setCreatedBy("etl");
        bibliographicEntity1.setLastUpdatedBy("etl");
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        BibliographicEntity bibliographicEntity2 = new BibliographicEntity();
        bibliographicEntity2.setContent("mock Content".getBytes());
        bibliographicEntity2.setCreatedDate(new Date());
        bibliographicEntity2.setCreatedBy("etl");
        bibliographicEntity2.setLastUpdatedBy("etl");
        bibliographicEntity2.setLastUpdatedDate(new Date());
        bibliographicEntity2.setOwningInstitutionId(1);
        bibliographicEntity2.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity));

        BibliographicEntity savedEntity1 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity1);
        entityManager.refresh(savedEntity1);
        BibliographicEntity savedEntity2 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity2);
        entityManager.refresh(savedEntity2);

        assertNotNull(savedEntity1);
        assertNotNull(savedEntity1.getBibliographicId());

        assertNotNull(savedEntity2);
        assertNotNull(savedEntity2.getBibliographicId());
    }

    @Test
    public void saveBibSingleHoldingsSingleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("etl");
        itemEntity.setBarcode("123");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("1");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);

        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
        assertNotNull(savedBibliographicEntity.getItemEntities().get(0).getItemId());
    }

    @Test
    public void saveBibSingleHoldingsMultipleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setBarcode("123");
        itemEntity1.setCallNumber("x.12321");
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCallNumberType("1");
        itemEntity1.setHoldingsEntity(holdingsEntity);

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setCreatedDate(new Date());
        itemEntity2.setCreatedBy("etl");
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setLastUpdatedBy("etl");
        itemEntity2.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity2.setOwningInstitutionId(1);
        itemEntity2.setCustomerCode("1");
        itemEntity2.setBarcode("123");
        itemEntity2.setItemAvailabilityStatusId(1);
        itemEntity2.setCallNumber("x.12321");
        itemEntity2.setCollectionGroupId(1);
        itemEntity2.setCallNumberType("1");
        itemEntity2.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);

        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
        assertNotNull(savedBibliographicEntity.getItemEntities().get(0).getItemId());
        assertNotNull(savedBibliographicEntity.getItemEntities().get(1).getItemId());
    }

    @Test
    public void boundWith() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content".getBytes());
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setCreatedBy("etl");
        bibliographicEntity1.setLastUpdatedBy("etl");
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setOwningInstitutionBibId("10001");

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setOwningInstitutionItemId("101");
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setBarcode("123");
        itemEntity1.setCallNumber("x.12321");
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCallNumberType("1");
        itemEntity1.setHoldingsEntity(holdingsEntity);

        bibliographicEntity1.setItemEntities(Arrays.asList(itemEntity1));

        BibliographicEntity bibliographicEntity2 = new BibliographicEntity();
        bibliographicEntity2.setContent("mock Content".getBytes());
        bibliographicEntity2.setCreatedDate(new Date());
        bibliographicEntity2.setCreatedBy("etl");
        bibliographicEntity2.setLastUpdatedBy("etl");
        bibliographicEntity2.setLastUpdatedDate(new Date());
        bibliographicEntity2.setOwningInstitutionId(1);
        bibliographicEntity2.setOwningInstitutionBibId("10002");

        bibliographicEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity2.setItemEntities(Arrays.asList(itemEntity1));

        BibliographicEntity savedEntity1 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity1);
        entityManager.refresh(savedEntity1);
        BibliographicEntity savedEntity2 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity2);
        entityManager.refresh(savedEntity2);

        assertNotNull(savedEntity1);
        assertNotNull(savedEntity1.getBibliographicId());

        assertNotNull(savedEntity2);
        assertNotNull(savedEntity2.getBibliographicId());
    }

    @Test
    public void saveTwoItemsOneItemWithBadData() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content".getBytes());
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setCreatedBy("etl");
        bibliographicEntity1.setLastUpdatedBy("etl");
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setOwningInstitutionBibId("10001");

        HoldingsEntity holdingsEntity1 = new HoldingsEntity();
        holdingsEntity1.setContent("mock holdings".getBytes());
        holdingsEntity1.setCreatedDate(new Date());
        holdingsEntity1.setCreatedBy("etl");
        holdingsEntity1.setLastUpdatedDate(new Date());
        holdingsEntity1.setLastUpdatedBy("etl");
        holdingsEntity1.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity1));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setOwningInstitutionItemId("101");
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setBarcode("123");
        itemEntity1.setCallNumber("x.12321");
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCallNumberType("1");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setHoldingsEntity(holdingsEntity1);

        bibliographicEntity1.setItemEntities(Arrays.asList(itemEntity1));

        BibliographicEntity bibliographicEntity2 = new BibliographicEntity();
        bibliographicEntity2.setContent("mock Content".getBytes());
        bibliographicEntity2.setCreatedDate(new Date());
        bibliographicEntity2.setCreatedBy("etl");
        bibliographicEntity2.setLastUpdatedBy("etl");
        bibliographicEntity2.setLastUpdatedDate(new Date());
        bibliographicEntity2.setOwningInstitutionId(1);
        bibliographicEntity2.setOwningInstitutionBibId("10002");

        HoldingsEntity holdingsEntity2 = new HoldingsEntity();
        holdingsEntity2.setContent("mock holdings".getBytes());
        holdingsEntity2.setCreatedDate(new Date());
        holdingsEntity2.setCreatedBy("etl");
        holdingsEntity2.setLastUpdatedDate(new Date());
        holdingsEntity2.setLastUpdatedBy("etl");
        holdingsEntity2.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity2));

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setCreatedDate(new Date());
        itemEntity2.setCreatedBy("etl");
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setLastUpdatedBy("etl");
        itemEntity2.setItemAvailabilityStatusId(1);
        itemEntity2.setOwningInstitutionItemId("102");
        itemEntity2.setOwningInstitutionId(1);
        itemEntity2.setBarcode("1234");
        itemEntity2.setCallNumber("x.123212");
        itemEntity2.setCollectionGroupId(1);
        itemEntity2.setCallNumberType("1");
        itemEntity2.setCustomerCode("1");
        itemEntity2.setHoldingsEntity(holdingsEntity2);

        bibliographicEntity2.setItemEntities(Arrays.asList(itemEntity2));

        BibliographicEntity savedEntity1 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity1);
        entityManager.refresh(savedEntity1);
        BibliographicEntity savedEntity2 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity2);
        entityManager.refresh(savedEntity2);

        assertNotNull(savedEntity1);
        assertNotNull(savedEntity1.getBibliographicId());

        assertNotNull(savedEntity2);
        assertNotNull(savedEntity2.getBibliographicId());
    }

    @Test
    public void refreshEntityAfterSave() throws Exception {

        String owningInstitutionBibId = "10001";
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content".getBytes());
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setOwningInstitutionBibId(owningInstitutionBibId);
        bibliographicEntity1.setCreatedBy("etl");
        bibliographicEntity1.setLastUpdatedBy("etl");
        BibliographicEntity savedEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity1);
        assertNotNull(savedEntity);
        entityManager.refresh(savedEntity);
        Integer bibliographicId = savedEntity.getBibliographicId();
        assertNotNull(bibliographicId);
        System.out.println("Saved Bib Id : " + bibliographicId);


    }

    @Test
    public void duplicateBibsWithDifferentItems() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content".getBytes());
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setCreatedBy("etl");
        bibliographicEntity1.setLastUpdatedBy("etl");
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        String owningInstitutionBibId = String.valueOf(random.nextInt());
        bibliographicEntity1.setOwningInstitutionBibId(owningInstitutionBibId);

        HoldingsEntity holdingsEntity1 = new HoldingsEntity();
        holdingsEntity1.setContent("mock holdings".getBytes());
        holdingsEntity1.setCreatedDate(new Date());
        holdingsEntity1.setCreatedBy("etl");
        holdingsEntity1.setLastUpdatedDate(new Date());
        holdingsEntity1.setLastUpdatedBy("etl");
        holdingsEntity1.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setBarcode("123");
        itemEntity1.setCallNumber("x.12321");
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCallNumberType("1");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setHoldingsEntity(holdingsEntity1);

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity1));
        bibliographicEntity1.setItemEntities(Arrays.asList(itemEntity1));

        BibliographicEntity savedBibliographicEntity1 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity1);
        entityManager.refresh(savedBibliographicEntity1);

        BibliographicEntity bibliographicEntity2 = new BibliographicEntity();
        bibliographicEntity2.setContent("mock Content".getBytes());
        bibliographicEntity2.setCreatedDate(new Date());
        bibliographicEntity2.setCreatedBy("etl");
        bibliographicEntity2.setLastUpdatedBy("etl");
        bibliographicEntity2.setLastUpdatedDate(new Date());
        bibliographicEntity2.setOwningInstitutionId(1);
        bibliographicEntity2.setOwningInstitutionBibId(owningInstitutionBibId);

        HoldingsEntity holdingsEntity2 = new HoldingsEntity();
        holdingsEntity2.setContent("mock holdings".getBytes());
        holdingsEntity2.setCreatedDate(new Date());
        holdingsEntity2.setCreatedBy("etl");
        holdingsEntity2.setLastUpdatedDate(new Date());
        holdingsEntity2.setLastUpdatedBy("etl");
        holdingsEntity2.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity2.setOwningInstitutionId(1);
        itemEntity2.setCreatedDate(new Date());
        itemEntity2.setCreatedBy("etl");
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setLastUpdatedBy("etl");
        itemEntity2.setBarcode("123");
        itemEntity2.setCallNumber("x.12321");
        itemEntity2.setCollectionGroupId(1);
        itemEntity2.setCallNumberType("1");
        itemEntity2.setCustomerCode("1");
        itemEntity2.setItemAvailabilityStatusId(1);
        itemEntity2.setHoldingsEntity(holdingsEntity2);

        if (bibliographicEntity2.getHoldingsEntities()==null){
            bibliographicEntity2.setHoldingsEntities(new ArrayList<>());
        }
        bibliographicEntity2.getHoldingsEntities().addAll(Arrays.asList(holdingsEntity2));
        if (bibliographicEntity2.getItemEntities()==null){
            bibliographicEntity2.setItemEntities(new ArrayList<>());
        }
        bibliographicEntity2.getItemEntities().addAll(Arrays.asList(itemEntity2));

        BibliographicEntity savedBibliographicEntity2 = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity2);
        entityManager.refresh(savedBibliographicEntity2);

        BibliographicEntity byOwningInstitutionBibId = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(1, owningInstitutionBibId);
        assertNotNull(byOwningInstitutionBibId);
        assertEquals(byOwningInstitutionBibId.getHoldingsEntities().size(), 2);
        assertEquals(byOwningInstitutionBibId.getItemEntities().size(), 2);

    }

}