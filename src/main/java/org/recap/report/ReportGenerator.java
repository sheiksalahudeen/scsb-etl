package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.recap.util.ReCAPCSVFailureRecordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by peris on 8/17/16.
 */

@Component
public class ReportGenerator {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ProducerTemplate producerTemplate;

    @Value("${etl.report.directory}")
    private String reportDirectory;

    public String generateReport(String fileName, Date from, Date to) {

        List<ReportEntity> reportEntities = getReportDetailRepository().findByFileAndDateRange(fileName, from, to);

        if (!CollectionUtils.isEmpty(reportEntities)) {
            List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecords = new ArrayList<>();
            for(ReportEntity reportEntity : reportEntities) {
                FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new ReCAPCSVFailureRecordGenerator().prepareFailureReportReCAPCSVRecord(reportEntity);
                failureReportReCAPCSVRecords.add(failureReportReCAPCSVRecord);
            }

            ReCAPCSVRecord reCAPCSVRecord = new ReCAPCSVRecord();
            reCAPCSVRecord.setFailureReportReCAPCSVRecordList(failureReportReCAPCSVRecords);

            producerTemplate.sendBody("seda:csvQ", reCAPCSVRecord);
        }

        String ddMMMyyyy = new SimpleDateFormat("ddMMMyyyy").format(new Date());
        String expectedGeneratedFileName = "test"+"-Failure-"+ddMMMyyyy+".csv";

        return  expectedGeneratedFileName;
    }

    public void setReportDetailRepository(ReportDetailRepository reportDetailRepository) {
        this.reportDetailRepository = reportDetailRepository;
    }

    public ReportDetailRepository getReportDetailRepository() {
        return reportDetailRepository;
    }
}
