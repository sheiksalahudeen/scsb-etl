package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.recap.RecapConstants;
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
public class CSVDataDumpFailureReportGenerator implements ReportGeneratorInterface {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Override
    public boolean isInterested(String reportType) {
        return reportType.equalsIgnoreCase(RecapConstants.BATCH_EXPORT_FAILURE) ? true : false;
    }

    @Override
    public boolean isTransmitted(String transmissionType) {
        return transmissionType.equalsIgnoreCase(RecapConstants.FILE_SYSTEM) ? true : false;
    }

    @Override
    public boolean isOperationType(String operationType) {
        return operationType.equalsIgnoreCase(RecapConstants.BATCH_EXPORT) ? true : false;
    }

    @Override
    public String generateReport(List<ReportEntity> reportEntities, String fileName) {
        if(!CollectionUtils.isEmpty(reportEntities)) {
            DataDumpFailureReport dataDumpFailureReport = new DataDumpFailureReport();
            List<DataDumpFailureReport> dataDumpFailureReportList = new ArrayList<>();
            for (ReportEntity reportEntity : reportEntities) {
                DataDumpFailureReport dataDumpSuccessReport1 = new DataDumpFailureReportGenerator().prepareDataDumpCSVFailureRecord(reportEntity);
                dataDumpFailureReportList.add(dataDumpSuccessReport1);
            }
            ReportEntity reportEntity = reportEntities.get(0);
            dataDumpFailureReport.setReportType(reportEntity.getType());
            dataDumpFailureReport.setInstitutionName(reportEntity.getInstitutionName());
            dataDumpFailureReport.setFileName(fileName);
            dataDumpFailureReport.setDataDumpFailureReportRecordList(dataDumpFailureReportList);
            DateFormat df = new SimpleDateFormat(RecapConstants.DATE_FORMAT_FOR_FILE_NAME);
            String generatedFileName = FilenameUtils.removeExtension(dataDumpFailureReport.getFileName()) + "-" + dataDumpFailureReport.getReportType() + "-" + df.format(new Date()) + ".csv";
            producerTemplate.sendBody(RecapConstants.DATADUMP_FAILURE_REPORT_CSV_Q, dataDumpFailureReport);

            return generatedFileName;
        }
        return null;
    }

}
