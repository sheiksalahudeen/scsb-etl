package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by chenchulakshmig on 13/7/16.
 */
public class ItemDetailsRepositoryTest extends BaseTestCase {

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void saveAndFind() throws Exception {
        assertNotNull(bibliographicDetailsRepository);
        assertNotNull(itemDetailsRepository);
        assertNotNull(entityManager);

        Random random = new Random();
        int owningInstitutionId = 2;

        Long count = itemDetailsRepository.countByOwningInstitutionId(owningInstitutionId);

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content");
        Date date = new Date();
        bibliographicEntity.setCreatedDate(date);
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(date);
        bibliographicEntity.setOwningInstitutionId(owningInstitutionId);
        String owningInstitutionBibId = String.valueOf(random.nextInt());
        bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings");
        holdingsEntity.setCreatedDate(date);
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(date);
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setCallNumberType("0");
        itemEntity.setCallNumber("callNum");
        itemEntity.setCreatedDate(date);
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(date);
        itemEntity.setLastUpdatedBy("etl");
        itemEntity.setBarcode("1231");
        String owningInstitutionItemId = String.valueOf(random.nextInt());
        itemEntity.setOwningInstitutionItemId(owningInstitutionItemId);
        itemEntity.setOwningInstitutionId(owningInstitutionId);
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCustomerCode("PA");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        holdingsEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedEntity);

        assertNotNull(savedEntity);
        assertNotNull(savedEntity.getBibliographicId());
        assertNotNull(savedEntity.getHoldingsEntities().get(0).getHoldingsId());
        ItemEntity savedItemEntity = savedEntity.getItemEntities().get(0);
        assertNotNull(savedItemEntity);
        assertNotNull(savedItemEntity.getItemId());

        Long countAfterAdd = itemDetailsRepository.countByOwningInstitutionId(owningInstitutionId);
        assertTrue(countAfterAdd > count);

        ItemEntity byOwningInstitutionId = itemDetailsRepository.findByOwningInstitutionId(owningInstitutionId);
        assertNotNull(byOwningInstitutionId);

        ItemEntity byOwningInstitutionItemId = itemDetailsRepository.findByOwningInstitutionItemId(owningInstitutionItemId);
        assertNotNull(byOwningInstitutionItemId);

        Page<ItemEntity> pageByOwningInstitutionId = itemDetailsRepository.findByOwningInstitutionId(new PageRequest(0, 10), owningInstitutionId);
        assertNotNull(pageByOwningInstitutionId);
        assertTrue(countAfterAdd == pageByOwningInstitutionId.getTotalElements());

        assertEquals(savedItemEntity.getCallNumberType(), "0");
        assertEquals(savedItemEntity.getCallNumber(), "callNum");
        assertEquals(savedItemEntity.getCreatedBy(), "etl");
        assertEquals(savedItemEntity.getLastUpdatedBy(), "etl");
        assertEquals(savedItemEntity.getBarcode(), "1231");
        assertEquals(savedItemEntity.getOwningInstitutionItemId(), owningInstitutionItemId);
        assertEquals(savedItemEntity.getCustomerCode(), "PA");
        assertNotNull(savedItemEntity.getHoldingsEntity());
        assertTrue(savedItemEntity.getOwningInstitutionId() == owningInstitutionId);
        assertTrue(savedItemEntity.getCollectionGroupId() == 1);
    }

}