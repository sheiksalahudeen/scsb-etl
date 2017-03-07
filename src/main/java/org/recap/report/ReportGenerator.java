package org.recap.report;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
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

    @Autowired
    CSVDataDumpSuccessReportGenreator csvDataDumpSuccessReportGenreator;

    @Autowired
    CSVDataDumpFailureReportGenreator csvDataDumpFailureReportGenreator;

    @Autowired
    FTPDataDumpSuccessReportGenerator ftpDataDumpSuccessReportGenerator;

    @Autowired
    FTPDataDumpFailureReportGenerator ftpDataDumpFailureReportGenerator;

    List<ReportGeneratorInterface> reportGenerators;

    public String generateReport(String fileName, String operationType, String reportType, String institutionName, Date from, Date to, String transmissionType) {

        List<ReportEntity> reportEntities;
        if(operationType.equals(ReCAPConstants.BATCH_EXPORT)){
            reportType = operationType+reportType;
        }
        if(StringUtils.isNotBlank(fileName)) {
            reportEntities = reportDetailRepository.findByFileAndInstitutionAndTypeAndDateRange(fileName, institutionName, reportType, from, to);
        } else {
            reportEntities = reportDetailRepository.findByInstitutionAndTypeAndDateRange(institutionName, reportType, from, to);
            fileName = institutionName;
        }

        for (Iterator<ReportGeneratorInterface> iterator = getReportGenerators().iterator(); iterator.hasNext(); ) {
            ReportGeneratorInterface reportGeneratorInterface = iterator.next();
            if(reportGeneratorInterface.isOperationType(operationType) && reportGeneratorInterface.isInterested(reportType)
                    && reportGeneratorInterface.isTransmitted(transmissionType)){
                return reportGeneratorInterface.generateReport(reportEntities, fileName);
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
            reportGenerators.add(csvDataDumpSuccessReportGenreator);
            reportGenerators.add(csvDataDumpFailureReportGenreator);
            reportGenerators.add(ftpDataDumpSuccessReportGenerator);
            reportGenerators.add(ftpDataDumpFailureReportGenerator);
        }
        return reportGenerators;
    }
}
