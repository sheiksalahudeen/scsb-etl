package org.recap.report;

import org.recap.model.csv.ReCAPCSVRecord;
import org.springframework.stereotype.Component;

/**
 * Created by peris on 8/17/16.
 */

@Component
public class CSVReportGenerator extends ReportGenerator {

    @Override
    public void transmit(ReCAPCSVRecord reCAPCSVRecord) {
        producerTemplate.sendBody("seda:csvQ", reCAPCSVRecord);
    }
}
