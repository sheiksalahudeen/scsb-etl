package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.recap.util.ReCAPCSVFailureRecordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by peris on 8/17/16.
 */
public abstract class ReportGenerator {
    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ProducerTemplate producerTemplate;

    @Value("${etl.report.directory}")
    private String reportDirectory;


    public String generateReport(String fileName, String reportType, String institutionName, Date from, Date to) {

        List<ReportEntity> reportEntities = reportDetailRepository.findByFileAndDateRange(fileName, from, to);

        if (!CollectionUtils.isEmpty(reportEntities)) {
            List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecords = new ArrayList<>();
            for(ReportEntity reportEntity : reportEntities) {
                FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new ReCAPCSVFailureRecordGenerator().prepareFailureReportReCAPCSVRecord(reportEntity);
                failureReportReCAPCSVRecords.add(failureReportReCAPCSVRecord);
            }

            ReCAPCSVRecord reCAPCSVRecord = new ReCAPCSVRecord();
            reCAPCSVRecord.setReportType(reportType);
            reCAPCSVRecord.setInstitutionName(institutionName);
            reCAPCSVRecord.setFailureReportReCAPCSVRecordList(failureReportReCAPCSVRecords);

            transmit(reCAPCSVRecord);
        }

        String ddMMMyyyy = new SimpleDateFormat("ddMMMyyyy").format(new Date());
        String expectedGeneratedFileName = fileName+"-"+reportType+"-"+ddMMMyyyy+".csv";

        return  expectedGeneratedFileName;
    }

    public abstract void transmit(ReCAPCSVRecord reCAPCSVRecord);


}
