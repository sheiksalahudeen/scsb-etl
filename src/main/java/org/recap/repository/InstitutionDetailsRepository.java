package org.recap.repository;

import org.recap.model.jpa.InstitutionEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface InstitutionDetailsRepository extends CrudRepository<InstitutionEntity, Integer> {
    public InstitutionEntity findByInstitutionCode(String code);
}
