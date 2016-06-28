package org.recap.model.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.BibliographicHoldingsDetailsRepository;
import org.recap.repository.BibliographicItemDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by chenchulakshmig on 23/6/16.
 */
public class BibliographicEntityTest extends BaseTestCase {

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    HoldingsDetailsRepository holdingsDetailsRepository;

    @Autowired
    BibliographicHoldingsDetailsRepository bibliographicHoldingsDetailsRepository;

    @Autowired
    BibliographicItemDetailsRepository bibliographicItemDetailsRepository;

    @Test
    public void saveBibSingleHoldings() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content");
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));


        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings");
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
    }

    @Test
    public void saveBibMultipleHoldings() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content");
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));


        HoldingsEntity holdingsEntity1 = new HoldingsEntity();
        holdingsEntity1.setContent("mock holdings");
        holdingsEntity1.setCreatedDate(new Date());
        holdingsEntity1.setLastUpdatedDate(new Date());
        holdingsEntity1.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));


        HoldingsEntity holdingsEntity2 = new HoldingsEntity();
        holdingsEntity2.setContent("mock holdings");
        holdingsEntity2.setCreatedDate(new Date());
        holdingsEntity2.setLastUpdatedDate(new Date());
        holdingsEntity2.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity1, holdingsEntity2));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(1).getHoldingsId());
    }

    @Test
    public void saveMultipleBibSingleHoldings() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content");
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        BibliographicEntity bibliographicEntity2 = new BibliographicEntity();
        bibliographicEntity2.setContent("mock Content");
        bibliographicEntity2.setCreatedDate(new Date());
        bibliographicEntity2.setLastUpdatedDate(new Date());
        bibliographicEntity2.setOwningInstitutionId(1);
        bibliographicEntity2.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings");
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity));

        Iterable<BibliographicEntity> savedBibliographicEntities = bibliographicDetailsRepository.save(Arrays.asList(bibliographicEntity1, bibliographicEntity2));
        assertNotNull(savedBibliographicEntities);
    }

    @Test
    public void saveBibSingleHoldingsSingleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content");
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));


        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings");
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setBarcode("123");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
    }

    @Test
    public void saveBibSingleHoldingsMultipleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content");
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));


        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings");
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setCreatedDate(new Date());
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
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setCreatedDate(new Date());
        itemEntity2.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity2.setOwningInstitutionId(1);
        itemEntity2.setBarcode("123");
        itemEntity2.setCallNumber("x.12321");
        itemEntity2.setCollectionGroupId(1);
        itemEntity2.setCallNumberType("1");
        itemEntity2.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));


        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
    }

    @Test
    public void boundWith() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content");
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setOwningInstitutionBibId("10001");


        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings");
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setCreatedDate(new Date());
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
        bibliographicEntity2.setContent("mock Content");
        bibliographicEntity2.setCreatedDate(new Date());
        bibliographicEntity2.setLastUpdatedDate(new Date());
        bibliographicEntity2.setOwningInstitutionId(1);
        bibliographicEntity2.setOwningInstitutionBibId("10002");

        bibliographicEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity2.setItemEntities(Arrays.asList(itemEntity1));


        ExecutorService executorService = Executors.newFixedThreadPool(2);

        List<Future> futureList = new ArrayList<>();
        futureList.add(executorService.submit(new BibRepositoryCallable(bibliographicEntity1, bibliographicDetailsRepository)));
        futureList.add(executorService.submit(new BibRepositoryCallable(bibliographicEntity2, bibliographicDetailsRepository)));

        List<BibliographicEntity> savedEntities = new ArrayList<>();

        for (Iterator<Future> iterator = futureList.iterator(); iterator.hasNext(); ) {
            Future future = iterator.next();
            savedEntities.add((BibliographicEntity) future.get());
        }

        assertTrue(savedEntities.size() == 2);

    }

    @Test
    public void saveTwoItemsOneItemWithBadData() throws Exception{
        Random random = new Random();
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content");
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setOwningInstitutionBibId("10001");


        HoldingsEntity holdingsEntity1 = new HoldingsEntity();
        holdingsEntity1.setContent("mock holdings");
        holdingsEntity1.setCreatedDate(new Date());
        holdingsEntity1.setLastUpdatedDate(new Date());
        holdingsEntity1.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity1));

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setOwningInstitutionItemId("101");
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setBarcode("123");
        itemEntity1.setCallNumber("x.12321");
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCallNumberType("1");
        itemEntity1.setHoldingsEntity(holdingsEntity1);

        bibliographicEntity1.setItemEntities(Arrays.asList(itemEntity1));

        BibliographicEntity bibliographicEntity2 = new BibliographicEntity();
        bibliographicEntity2.setContent("mock Content");
        bibliographicEntity2.setCreatedDate(new Date());
        bibliographicEntity2.setLastUpdatedDate(new Date());
        bibliographicEntity2.setOwningInstitutionId(1);
        bibliographicEntity2.setOwningInstitutionBibId("10002");


        HoldingsEntity holdingsEntity2 = new HoldingsEntity();
        holdingsEntity2.setContent("mock holdings");
        holdingsEntity2.setCreatedDate(new Date());
        holdingsEntity2.setLastUpdatedDate(new Date());
        holdingsEntity2.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        bibliographicEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity2));

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setCreatedDate(new Date());
        itemEntity2.setItemAvailabilityStatusId(1);
        itemEntity2.setOwningInstitutionItemId("102");
        itemEntity2.setOwningInstitutionId(1);
        itemEntity2.setBarcode("1234");
        itemEntity2.setCallNumber("x.123212");
        itemEntity2.setCollectionGroupId(1);
        itemEntity2.setCallNumberType("1");
        itemEntity2.setHoldingsEntity(holdingsEntity2);

        bibliographicEntity2.setItemEntities(Arrays.asList(itemEntity2));

        bibliographicDetailsRepository.save(Arrays.asList(bibliographicEntity1, bibliographicEntity2));
    }

}