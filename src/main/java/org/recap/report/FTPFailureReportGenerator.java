package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.recap.ReCAPConstants;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVFailureRecord;
import org.recap.model.jpa.ReportEntity;
import org.recap.util.ReCAPCSVFailureRecordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by angelind on 18/8/16.
 */

@Component
public class FTPFailureReportGenerator implements ReportGeneratorInterface {

    @Autowired
    ProducerTemplate producerTemplate;

    @Override
    public boolean isInterested(String reportType) {
        return reportType.equalsIgnoreCase(org.recap.ReCAPConstants.FAILURE) ? true : false;
    }

    @Override
    public boolean isTransmitted(String transmissionType) {
        return transmissionType.equalsIgnoreCase(org.recap.ReCAPConstants.FTP) ? true : false;
    }

    @Override
    public String generateReport(List<ReportEntity> reportEntities, String fileName) {

        if(!CollectionUtils.isEmpty(reportEntities)) {
            ReCAPCSVFailureRecord reCAPCSVFailureRecord = new ReCAPCSVFailureRecord();
            List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecords = new ArrayList<>();
            for(ReportEntity reportEntity : reportEntities) {
                FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new ReCAPCSVFailureRecordGenerator().prepareFailureReportReCAPCSVRecord(reportEntity);
                failureReportReCAPCSVRecords.add(failureReportReCAPCSVRecord);
            }
            ReportEntity reportEntity = reportEntities.get(0);
            reCAPCSVFailureRecord.setReportType(reportEntity.getType());
            reCAPCSVFailureRecord.setInstitutionName(reportEntity.getInstitutionName());
            reCAPCSVFailureRecord.setFileName(fileName);
            reCAPCSVFailureRecord.setFailureReportReCAPCSVRecordList(failureReportReCAPCSVRecords);
            producerTemplate.sendBody(ReCAPConstants.FTP_SUCCESS_Q, reCAPCSVFailureRecord);
            DateFormat df = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_FOR_FILE_NAME);
            String generatedFileName = FilenameUtils.removeExtension(reCAPCSVFailureRecord.getFileName()) + "-" + reCAPCSVFailureRecord.getReportType() + "-" + df.format(new Date()) + ".csv";
            return generatedFileName;
        }

        return null;
    }
}
