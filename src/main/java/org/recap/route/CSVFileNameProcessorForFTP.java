package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.recap.model.csv.ReCAPCSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peris on 8/16/16.
 */
public class CSVFileNameProcessorForFTP implements Processor {

    Logger logger = LoggerFactory.getLogger(CSVFileNameProcessorForFTP.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        ReCAPCSVRecord reCAPCSVRecord = (ReCAPCSVRecord) exchange.getIn().getBody();
        String fileName = FilenameUtils.removeExtension(reCAPCSVRecord.getFileName());
        exchange.getIn().setHeader("fileName", fileName);
        exchange.getIn().setHeader("directoryName", reCAPCSVRecord.getInstitutionName());
        exchange.getIn().setHeader("reportType", reCAPCSVRecord.getReportType());

    }
}
