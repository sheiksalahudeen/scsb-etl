package org.recap.repository;

import org.recap.model.jpa.MatchingInstitutionBibViewEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by premkb on 24/1/17.
 */
public interface MatchingInstitutionBibRepository extends CrudRepository<MatchingInstitutionBibViewEntity, Integer> {

    @Query(value="SELECT MATCHING FROM MatchingInstitutionBibViewEntity MATCHING WHERE MATCHING.bibId IN (?1)")
    List<MatchingInstitutionBibViewEntity> findByBibIdList(List<String> bibIdList);
}
