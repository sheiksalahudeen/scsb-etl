package org.recap.repository;

import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by SheikS on 8/8/2016.
 */
public interface ReportDetailRepository extends JpaRepository<ReportEntity, Integer> {
    List<ReportEntity> findByFileName(String fileName);

    @Query(value = "select * from report_t where FILE_NAME=?1 and CREATED_DATE >= ?2 and CREATED_DATE <= ?3",  nativeQuery = true)
    List<ReportEntity> findByFileAndDateRange(String fileName, Date from, Date to);
}
