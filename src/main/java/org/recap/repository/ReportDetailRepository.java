package org.recap.repository;

import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * Created by SheikS on 8/8/2016.
 */
public interface ReportDetailRepository extends PagingAndSortingRepository<ReportEntity, Integer> {
    List<ReportEntity> findByFileName(String fileName);
}
