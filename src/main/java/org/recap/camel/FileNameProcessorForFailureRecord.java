package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.recap.RecapConstants;
import org.recap.model.csv.ReCAPCSVFailureRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peris on 8/16/16.
 */
public class FileNameProcessorForFailureRecord implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(FileNameProcessorForFailureRecord.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        ReCAPCSVFailureRecord reCAPCSVFailureRecord = (ReCAPCSVFailureRecord) exchange.getIn().getBody();
        String fileName = FilenameUtils.removeExtension(reCAPCSVFailureRecord.getFileName());
        exchange.getIn().setHeader(RecapConstants.REPORT_FILE_NAME, fileName);
        exchange.getIn().setHeader(RecapConstants.DIRECTORY_NAME, reCAPCSVFailureRecord.getInstitutionName());
        exchange.getIn().setHeader(RecapConstants.REPORT_TYPE, reCAPCSVFailureRecord.getReportType());

    }
}
