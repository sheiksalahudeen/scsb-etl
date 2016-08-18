package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.recap.model.csv.ReCAPCSVSuccessRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by angelind on 18/8/16.
 */
public class FileNameProcessorForSuccessRecord implements Processor {

    Logger logger = LoggerFactory.getLogger(FileNameProcessorForSuccessRecord.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        ReCAPCSVSuccessRecord reCAPCSVSuccessRecord = (ReCAPCSVSuccessRecord) exchange.getIn().getBody();
        String fileName = FilenameUtils.removeExtension(reCAPCSVSuccessRecord.getReportFileName());
        exchange.getIn().setHeader("fileName", fileName);
        exchange.getIn().setHeader("reportType", reCAPCSVSuccessRecord.getReportType());
        exchange.getIn().setHeader("directoryName", reCAPCSVSuccessRecord.getInstitutionName());
    }
}
