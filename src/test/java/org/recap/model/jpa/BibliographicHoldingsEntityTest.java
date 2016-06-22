package org.recap.model.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.BibliographicHoldingsDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Created by pvsubrah on 6/22/16.
 */
public class BibliographicHoldingsEntityTest extends BaseTestCase {

    @Autowired
    BibliographicHoldingsDetailsRepository bibliographicHoldingsDetailsRepository;

    @Test
    public void oneBibManyHoldings() throws Exception {
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        Random random = new Random();
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setContent("mock bib content");

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));
        holdingsEntity.setContent("mock holdings content");


        BibliographicHoldingsEntity bibliographicHoldingsEntity = new BibliographicHoldingsEntity();
        bibliographicHoldingsEntity.setBibliographicEntity(bibliographicEntity);
        bibliographicHoldingsEntity.setHoldingsEntity(holdingsEntity);


        BibliographicHoldingsEntity savedBibHoldingsRecord = bibliographicHoldingsDetailsRepository.save(bibliographicHoldingsEntity);
        assertNotNull(savedBibHoldingsRecord);


        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
        bibliographicEntity1.setOwningInstitutionId(1);
        bibliographicEntity1.setContent("mock bib content");

        BibliographicHoldingsEntity bibliographicHoldingsEntity1 = new BibliographicHoldingsEntity();
        bibliographicHoldingsEntity1.setBibliographicEntity(bibliographicEntity1);
        holdingsEntity.setBibliographicHoldingsEntities(null);
        bibliographicHoldingsEntity1.setHoldingsEntity(holdingsEntity);

        BibliographicHoldingsEntity savedBibHoldingsRecord1 = bibliographicHoldingsDetailsRepository.save(bibliographicHoldingsEntity1);
        assertNotNull(savedBibHoldingsRecord1);

    }

}