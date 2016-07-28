package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.recap.model.csv.ReCAPCSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 22/7/16.
 */
@Component
public class CSVFailureFileNameProcessor implements Processor{

    Logger logger = LoggerFactory.getLogger(CSVFailureFileNameProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        ReCAPCSVRecord reCAPCSVRecord = (ReCAPCSVRecord) exchange.getIn().getBody();
        String fileName = FilenameUtils.removeExtension(reCAPCSVRecord.getFailureReportReCAPCSVRecordList().get(0).getFileName());
        exchange.getIn().setHeader("reportFileName", fileName);
    }
}
