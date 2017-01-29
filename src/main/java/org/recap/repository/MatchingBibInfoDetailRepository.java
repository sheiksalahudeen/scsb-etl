package org.recap.repository;

import org.recap.model.jpa.MatchingBibInfoDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by premkb on 29/1/17.
 */
public interface MatchingBibInfoDetailRepository extends JpaRepository<MatchingBibInfoDetail, Integer> {

    @Query(value ="SELECT recordNum FROM MatchingBibInfoDetail WHERE bibId IN (?1)")
    List<Integer> getRecordNum(List<String> bibIdList);

    @Query(value ="SELECT MBID FROM MatchingBibInfoDetail MBID WHERE recordNum IN (?1)")
    List<MatchingBibInfoDetail> findByRecordNum(List<Integer> recordNumList);
}
