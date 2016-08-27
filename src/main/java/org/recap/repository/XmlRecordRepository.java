package org.recap.repository;

import org.recap.model.jpa.XmlRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by peris on 7/17/16.
 */

public interface XmlRecordRepository extends PagingAndSortingRepository<XmlRecordEntity, Integer> {

    Page<XmlRecordEntity> findByXmlFileName(Pageable pageable, String xmlFileName);

    Page<XmlRecordEntity> findByXmlFileNameContaining(Pageable pageable, String xmlFileName);

    Long countByXmlFileName(String xmlFileName);

    Long countByXmlFileNameContaining(String xmlFileName);

    XmlRecordEntity findById(Integer id);

    @Query(value = "select distinct (xml_file) from xml_records_t",  nativeQuery = true)
    List findDistinctFileNames();

    @Query(value = "select institution_id  from institution_t where institution_code in (select owning_inst from xml_records_t where xml_file = ?1) limit 1",  nativeQuery = true)
    Integer findInstIdByFileNames(String fileName);
}
