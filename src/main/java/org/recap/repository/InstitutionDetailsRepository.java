package org.recap.repository;

import org.recap.model.jpa.InstitutionEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface InstitutionDetailsRepository extends PagingAndSortingRepository<InstitutionEntity, Integer> {

    InstitutionEntity findByInstitutionCode(String institutionCode);

    InstitutionEntity findByInstitutionName(String institutionName);

}
