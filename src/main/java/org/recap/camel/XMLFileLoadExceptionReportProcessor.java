package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.lang3.StringUtils;
import org.recap.RecapConstants;
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
public class XMLFileLoadExceptionReportProcessor implements Processor {

    /**
     * The Report detail repository.
     */
    @Autowired
    ReportDetailRepository reportDetailRepository;

    /**
     * This method is invoked by route to prepare exception report entity from exchange and persist.
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        String institutionName = (String) exchange.getProperty(RecapConstants.INST_NAME);
        if (StringUtils.isNotEmpty(institutionName)) {
            ReportEntity reportEntity = new ReportEntity();
            reportEntity.setCreatedDate(new Date());
            GenericFile camelFileExchangeFile = (GenericFile) exchange.getProperty(RecapConstants.CAMEL_EXCHANGE_FILE);
            reportEntity.setFileName(camelFileExchangeFile.getFileName());
            reportEntity.setType(RecapConstants.XML_LOAD);
            reportEntity.setInstitutionName(institutionName);

            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName(RecapConstants.FILE_LOAD_STATUS);
            reportDataEntity.setHeaderValue(RecapConstants.FILE_LOAD_EXCEPTION);

            reportEntity.setReportDataEntities(Arrays.asList(reportDataEntity));

            reportDetailRepository.save(reportEntity);
        }

    }
}
