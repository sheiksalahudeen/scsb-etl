package org.recap.report;

import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.jpa.ReportEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by peris on 8/17/16.
 */

@Component
public class CSVReportGenerator implements ReportGeneratorInterface  {
    @Override
    public boolean isInterested(String reportType) {
        return false;
    }

    @Override
    public void generateReport(List<ReportEntity> reportEntities) {

    }

//    @Override
//    public void transmit() {
//        producerTemplate.sendBody("seda:csvQ", reCAPCSVRecord);
//    }
//
//    @Override
//    public boolean isInterested(String reportType) {
//        return reportType.equals("Failure")? true : false;
//    }
}
