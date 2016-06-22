package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.BibliographicEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class BibliographicDetailsRepositoryTest extends BaseTestCase {

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Test
    public void saveBibEntity() throws Exception {

        assertNotNull(bibliographicDetailsRepository);

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("Mock Bib Content");
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setOwningInstitutionBibId("777");
        bibliographicEntity.setOwningInstitutionId(1);
        BibliographicEntity savedbibliographictEntity = bibliographicDetailsRepository.save(bibliographicEntity);
        Integer bibliographicId = savedbibliographictEntity.getBibliographicId();
        assertNotNull(bibliographicId);

        bibliographicEntity = bibliographicDetailsRepository.findOne(bibliographicId);
        assertNotNull(bibliographicEntity);


    }

}