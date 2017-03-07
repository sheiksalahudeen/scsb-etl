package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.recap.ReCAPConstants;
import org.recap.model.csv.DataDumpFailureReport;
import org.recap.model.jpa.ReportEntity;
import org.recap.util.datadump.DataDumpFailureReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by premkb on 29/9/16.
 */

@Component
public class FTPDataDumpFailureReportGenerator implements ReportGeneratorInterface {

    @Autowired
    ProducerTemplate producerTemplate;

    @Override
    public boolean isInterested(String reportType) {
        return reportType.equalsIgnoreCase(ReCAPConstants.BATCH_EXPORT_FAILURE) ? true : false;
    }

    @Override
    public boolean isTransmitted(String transmissionType) {
        return transmissionType.equalsIgnoreCase(ReCAPConstants.FTP) ? true : false;
    }

    @Override
    public boolean isOperationType(String operationType) {
        return operationType.equalsIgnoreCase(ReCAPConstants.BATCH_EXPORT) ? true : false;
    }

    @Override
    public String generateReport(List<ReportEntity> reportEntities, String fileName) {

        if(!CollectionUtils.isEmpty(reportEntities)) {
            DataDumpFailureReport dataDumpFailureReport = new DataDumpFailureReport();
            List<DataDumpFailureReport> dataDumpSuccessReportList = new ArrayList<>();
            for(ReportEntity reportEntity : reportEntities) {
                DataDumpFailureReport dataDumpFailureReportRecord = new DataDumpFailureReportGenerator().prepareDataDumpCSVFailureRecord(reportEntity);
                dataDumpSuccessReportList.add(dataDumpFailureReportRecord);
            }
            ReportEntity reportEntity = reportEntities.get(0);
            dataDumpFailureReport.setReportType(reportEntity.getType());
            dataDumpFailureReport.setInstitutionName(reportEntity.getInstitutionName());
            dataDumpFailureReport.setFileName(fileName);
            dataDumpFailureReport.setDataDumpFailureReportRecordList(dataDumpSuccessReportList);
            producerTemplate.sendBody(ReCAPConstants.DATADUMP_FAILURE_REPORT_FTP_Q, dataDumpFailureReport);
            DateFormat df = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_FOR_FILE_NAME);
            return FilenameUtils.removeExtension(dataDumpFailureReport.getFileName()) + "-" + dataDumpFailureReport.getReportType() + "-" + df.format(new Date()) + ".csv";
        }
        return null;
    }
}
