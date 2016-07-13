package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.InstitutionEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;

/**
 * Created by pvsubrah on 6/22/16.
 */
public class InstitutionDetailsRepositoryTest extends BaseTestCase {

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Test
    public void saveAndFind() throws Exception {
        assertNotNull(institutionDetailsRepository);

        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionCode("test");
        institutionEntity.setInstitutionName("test");

        InstitutionEntity savedInstitutionEntity = institutionDetailsRepository.save(institutionEntity);
        assertNotNull(savedInstitutionEntity);
        assertNotNull(savedInstitutionEntity.getInstitutionId());

        InstitutionEntity byInstitutionCode = institutionDetailsRepository.findByInstitutionCode("test");
        assertNotNull(byInstitutionCode);

        InstitutionEntity byInstitutionName = institutionDetailsRepository.findByInstitutionName("test");
        assertNotNull(byInstitutionName);
    }

}