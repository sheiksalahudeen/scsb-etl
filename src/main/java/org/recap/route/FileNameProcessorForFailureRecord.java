package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.recap.model.csv.ReCAPCSVFailureRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peris on 8/16/16.
 */
public class FileNameProcessorForFailureRecord implements Processor {

    Logger logger = LoggerFactory.getLogger(FileNameProcessorForFailureRecord.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        ReCAPCSVFailureRecord reCAPCSVFailureRecord = (ReCAPCSVFailureRecord) exchange.getIn().getBody();
        String fileName = FilenameUtils.removeExtension(reCAPCSVFailureRecord.getFileName());
        exchange.getIn().setHeader("fileName", fileName);
        exchange.getIn().setHeader("directoryName", reCAPCSVFailureRecord.getInstitutionName());
        exchange.getIn().setHeader("reportType", reCAPCSVFailureRecord.getReportType());

    }
}
