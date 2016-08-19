package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.GenericFile;
import org.recap.ReCAPConstants;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by peris on 8/19/16.
 */

@Component
public class XMLFileLoadReportProcessor implements Processor {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Override
    public void process(Exchange exchange) throws Exception {
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setCreatedDate(new Date());
        GenericFile camelFileExchangeFile = (GenericFile) exchange.getProperty(ReCAPConstants.CAMEL_EXCHANGE_FILE);
        reportEntity.setFileName(camelFileExchangeFile.getFileName());
        reportEntity.setType(ReCAPConstants.XML_LOAD);
        reportEntity.setInstitutionName((String) exchange.getProperty(ReCAPConstants.INST_NAME));

        ReportDataEntity reportDataEntity = new ReportDataEntity();
        reportDataEntity.setHeaderName(ReCAPConstants.FILE_LOAD_STATUS);
        reportDataEntity.setHeaderValue(ReCAPConstants.FILE_LOADED);

        reportEntity.setReportDataEntities(Arrays.asList(reportDataEntity));

        reportDetailRepository.save(reportEntity);

    }
}
