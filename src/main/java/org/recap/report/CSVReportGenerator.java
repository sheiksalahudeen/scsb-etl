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
public class CSVReportGenerator extends ReportGenerator {

    @Override
    public void transmit(ReCAPCSVRecord reCAPCSVRecord) {
        producerTemplate.sendBody("seda:csvQ", reCAPCSVRecord);
    }
}
