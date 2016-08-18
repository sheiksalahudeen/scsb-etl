package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

    @Autowired
    CSVFailureReportGenerator csvFailureReportGenerator;

    @Autowired
    CSVSuccessReportGenerator csvSuccessReportGenerator;

    @Autowired
    FTPFailureReportGenerator ftpFailureReportGenerator;

    @Autowired
    FTPSuccessReportGenerator ftpSuccessReportGenerator;

    List<ReportGeneratorInterface> reportGenerators;

    public String generateReport(String fileName, String reportType, String institutionName, Date from, Date to, String transmissionType) {

        List<ReportEntity> reportEntities = reportDetailRepository.findByFileAndInstitutionAndTypeAndDateRange(fileName, institutionName, reportType, from, to);

        for (Iterator<ReportGeneratorInterface> iterator = getReportGenerators().iterator(); iterator.hasNext(); ) {
            ReportGeneratorInterface reportGeneratorInterface = iterator.next();
            if(reportGeneratorInterface.isInterested(reportType) && reportGeneratorInterface.isTransmitted(transmissionType)){
                String generatedFileName = reportGeneratorInterface.generateReport(reportEntities);
                return generatedFileName;
            }
        }

        return null;

    }

    public List<ReportGeneratorInterface> getReportGenerators() {
        if(CollectionUtils.isEmpty(reportGenerators)) {
            reportGenerators = new ArrayList<>();
            reportGenerators.add(csvFailureReportGenerator);
            reportGenerators.add(csvSuccessReportGenerator);
            reportGenerators.add(ftpFailureReportGenerator);
            reportGenerators.add(ftpSuccessReportGenerator);
        }
        return reportGenerators;
    }
}
