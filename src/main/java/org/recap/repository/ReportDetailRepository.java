package org.recap.repository;

import org.recap.model.jpa.ReportEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by SheikS on 8/8/2016.
 */
public interface ReportDetailRepository extends PagingAndSortingRepository<ReportEntity, Integer> {
}
