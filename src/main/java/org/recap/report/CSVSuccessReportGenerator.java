package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.recap.ReCAPConstants;
import org.recap.model.csv.ReCAPCSVSuccessRecord;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.recap.model.jpa.ReportEntity;
import org.recap.util.ReCAPCSVSuccessRecordGenerator;
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
public class CSVSuccessReportGenerator implements ReportGeneratorInterface{

    @Autowired
    ProducerTemplate producerTemplate;

    @Override
    public boolean isInterested(String reportType) {
        return reportType.equalsIgnoreCase(org.recap.ReCAPConstants.SUCCESS) ? true : false;
    }

    @Override
    public boolean isTransmitted(String transmissionType) {
        return transmissionType.equalsIgnoreCase(org.recap.ReCAPConstants.FILE_SYSTEM) ? true : false;
    }

    @Override
    public boolean isOperationType(String operationType) {
        return operationType.equalsIgnoreCase(org.recap.ReCAPConstants.OPERATION_TYPE_ETL) ? true : false;
    }
    @Override
    public String generateReport(List<ReportEntity> reportEntities, String fileName) {

        if(!CollectionUtils.isEmpty(reportEntities)) {
            ReCAPCSVSuccessRecord reCAPCSVSuccessRecord = new ReCAPCSVSuccessRecord();
            List<SuccessReportReCAPCSVRecord> successReportReCAPCSVRecords = new ArrayList<>();
            for (ReportEntity reportEntity : reportEntities) {
                SuccessReportReCAPCSVRecord successReportReCAPCSVRecord = new ReCAPCSVSuccessRecordGenerator().prepareSuccessReportReCAPCSVRecord(reportEntity);
                successReportReCAPCSVRecords.add(successReportReCAPCSVRecord);
            }
            ReportEntity reportEntity = reportEntities.get(0);
            reCAPCSVSuccessRecord.setReportType(reportEntity.getType());
            reCAPCSVSuccessRecord.setInstitutionName(reportEntity.getInstitutionName());
            reCAPCSVSuccessRecord.setReportFileName(fileName);
            reCAPCSVSuccessRecord.setSuccessReportReCAPCSVRecordList(successReportReCAPCSVRecords);
            producerTemplate.sendBody(ReCAPConstants.CSV_SUCCESS_Q, reCAPCSVSuccessRecord);
            DateFormat df = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_FOR_FILE_NAME);
            String generatedFileName = FilenameUtils.removeExtension(reCAPCSVSuccessRecord.getReportFileName()) + "-" + reCAPCSVSuccessRecord.getReportType() + "-" + df.format(new Date()) + ".csv";
            return generatedFileName;
        }

        return null;
    }
}
