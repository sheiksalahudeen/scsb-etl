package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.InstitutionEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by pvsubrah on 6/22/16.
 */
public class InstitutionDetailsRepositoryTest extends BaseTestCase {

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Test
    public void getByCode() throws Exception {
        InstitutionEntity byInstitutionCode = institutionDetailsRepository.findByInstitutionCode("PUL");
        assertNotNull(byInstitutionCode);
    }

}