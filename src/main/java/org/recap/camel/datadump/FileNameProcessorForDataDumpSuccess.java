package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.recap.ReCAPConstants;
import org.recap.model.csv.DataDumpSuccessReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by premkb on 01/10/16.
 */
public class FileNameProcessorForDataDumpSuccess implements Processor {

    Logger logger = LoggerFactory.getLogger(FileNameProcessorForDataDumpSuccess.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        DataDumpSuccessReport dataDumpSuccessReport = (DataDumpSuccessReport) exchange.getIn().getBody();
        String fileName = FilenameUtils.removeExtension(dataDumpSuccessReport.getFileName());
        exchange.getIn().setHeader(ReCAPConstants.REPORT_FILE_NAME, fileName);
        exchange.getIn().setHeader(ReCAPConstants.REPORT_TYPE, dataDumpSuccessReport.getReportType());
        exchange.getIn().setHeader(ReCAPConstants.DIRECTORY_NAME, dataDumpSuccessReport.getInstitutionName());
    }
}
