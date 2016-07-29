package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FilenameUtils;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 28/7/16.
 */
@Component
public class CSVSuccessFileNameProcessor implements Processor{

    @Override
    public void process(Exchange exchange) throws Exception {
        SuccessReportReCAPCSVRecord successReportReCAPCSVRecord = (SuccessReportReCAPCSVRecord) exchange.getIn().getBody();
        String fileName = FilenameUtils.removeExtension(successReportReCAPCSVRecord.getFileName());
        String institution = successReportReCAPCSVRecord.getOwningInstitution();
        exchange.getIn().setHeader("institutionName", institution);
        exchange.getIn().setHeader("reportFileName", fileName);
    }
}
