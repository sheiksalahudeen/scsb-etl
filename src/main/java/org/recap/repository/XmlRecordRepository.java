package org.recap.repository;

import org.recap.model.jpa.XmlRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by peris on 7/17/16.
 */

public interface XmlRecordRepository extends PagingAndSortingRepository<XmlRecordEntity, Integer> {

    Page<XmlRecordEntity> findByXmlFileName(Pageable pageable, String xmlFileName);
}
