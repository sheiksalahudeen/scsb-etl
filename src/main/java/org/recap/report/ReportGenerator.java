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
import java.util.Iterator;
import java.util.List;

/**
 * Created by peris on 8/17/16.
 */
public class ReportGenerator {
    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    ProducerTemplate producerTemplate;

    @Value("${etl.report.directory}")
    private String reportDirectory;

    @Autowired
    FTPReportGenerator ftpReportGenerator;

    @Autowired
    CSVReportGenerator csvReportGenerator;


    List<ReportGeneratorInterface> reportGenerators;


    public ReportGenerator() {
        reportGenerators = new ArrayList<>();
        reportGenerators.add(ftpReportGenerator);
        reportGenerators.add(csvReportGenerator);
    }

    public String generateReport(String fileName, String reportType, String institutionName, Date from, Date to) {

        List<ReportEntity> reportEntities = reportDetailRepository.findByFileAndDateRange(fileName, from, to);

        for (Iterator<ReportGeneratorInterface> iterator = reportGenerators.iterator(); iterator.hasNext(); ) {
            ReportGeneratorInterface reportGeneratorInterface = iterator.next();
            if(reportGeneratorInterface.isInterested(reportType)){
                reportGeneratorInterface.generateReport(reportEntities);
            }
        }

        return null;

    }


}
