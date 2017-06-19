package org.recap.repository;

import org.recap.model.jpa.ReportDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by premkb on 24/1/17.
 */
public interface ReportDataRepository extends JpaRepository<ReportDataEntity, Integer> {

    /**
     * Gets report data for matching institution bib.
     *
     * @param recordNumList  the record num list
     * @param headerNameList the header name list
     * @return the report data for matching institution bib
     */
    @Query(value="SELECT REPORTDATA FROM ReportDataEntity REPORTDATA WHERE REPORTDATA.recordNum IN (?1) AND REPORTDATA.headerName IN (?2) ORDER BY REPORTDATA.recordNum")
    List<ReportDataEntity> getReportDataForMatchingInstitutionBib(List<String> recordNumList,List<String> headerNameList);
}
