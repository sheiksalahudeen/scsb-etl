package org.recap.model.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.BibliographicHoldingsDetailsRepository;
import org.recap.repository.BibliographicItemDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertNotNull;

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
        BibliographicEntity bibliographicEntity = new BibliographicEntity("mock bib content", 1, new Date(), new Date(), String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
    }

    @Test
    public void saveBibMultipleHoldings() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity("mock bib content", 1, new Date(), new Date(), String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity1 = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));
        HoldingsEntity holdingsEntity2 = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));

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
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity("mock bib content", 1, new Date(), new Date(), String.valueOf(random.nextInt()));
        BibliographicEntity bibliographicEntity2 = new BibliographicEntity("mock bib content", 1, new Date(), new Date(), String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));

        bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity));

        Iterable<BibliographicEntity> savedBibliographicEntities = bibliographicDetailsRepository.save(Arrays.asList(bibliographicEntity1, bibliographicEntity2));
        assertNotNull(savedBibliographicEntities);
    }

    @Test
    public void saveBibSingleHoldingsSingleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity("mock bib content", 1, new Date(), new Date(), String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity(String.valueOf(random.nextInt()), "11", "11", "ff", 1, 1, 1, 1, new Date(), new Date(), "11", "11", "11", 1);
        itemEntity.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
    }

    @Test
    public void saveBibSingleHoldingsMultipleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity("mock bib content", 1, new Date(), new Date(), String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));

        ItemEntity itemEntity1 = new ItemEntity(String.valueOf(random.nextInt()), "11", "11", "ff", 1, 1, 1, 1, new Date(), new Date(), "11", "11", "11", 1);
        itemEntity1.setHoldingsEntity(holdingsEntity);

        ItemEntity itemEntity2 = new ItemEntity(String.valueOf(random.nextInt()), "11", "11", "ff", 1, 1, 1, 1, new Date(), new Date(), "11", "11", "22", 1);
        itemEntity2.setHoldingsEntity(holdingsEntity);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);
    }

    @Test
    public void saveBibMultipleHoldingsSingleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity("mock bib content", 1, new Date(), new Date(), String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity1 = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));
        ItemEntity itemEntity1 = new ItemEntity(String.valueOf(random.nextInt()), "11", "11", "ff", 1, 1, 1, 1, new Date(), new Date(), "11", "11", "11", 1);
        itemEntity1.setHoldingsEntity(holdingsEntity1);

        HoldingsEntity holdingsEntity2 = new HoldingsEntity("mock holdings content", new Date(), new Date(), String.valueOf(random.nextInt()));
        ItemEntity itemEntity2 = new ItemEntity(String.valueOf(random.nextInt()), "11", "11", "ff", 1, 1, 1, 1, new Date(), new Date(), "11", "11", "22", 1);
        itemEntity2.setHoldingsEntity(holdingsEntity2);

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity1, holdingsEntity2));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        assertNotNull(savedBibliographicEntity);

    }

}