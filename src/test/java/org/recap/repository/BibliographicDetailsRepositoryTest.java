package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.BibliographicEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertNotNull;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class BibliographicDetailsRepositoryTest extends BaseTestCase {

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void saveBibEntity() throws Exception {

        assertNotNull(bibliographicDetailsRepository);
        assertNotNull(entityManager);

        Random random = new Random();

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("Mock Bib Content");
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setCreatedBy("etl");
        bibliographicEntity.setLastUpdatedBy("etl");
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
        bibliographicEntity.setOwningInstitutionId(1);
        BibliographicEntity savedBibliographictEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographictEntity);

        Integer bibliographicId = savedBibliographictEntity.getBibliographicId();
        assertNotNull(bibliographicId);
    }

}