package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.recap.RecapConstants;
import org.recap.model.jaxb.BibRecord;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;
import org.recap.util.XmlFormatter;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peris on 11/1/16.
 */

public class SCSBXMLFormatActiveMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SCSBXMLFormatActiveMQConsumer.class);

    SCSBXmlFormatterService scsbXmlFormatterService;
    XmlFormatter xmlFormatter;
    private DataExportHeaderUtil dataExportHeaderUtil;

    public SCSBXMLFormatActiveMQConsumer(SCSBXmlFormatterService scsbXmlFormatterService, XmlFormatter xmlFormatter) {
        this.scsbXmlFormatterService = scsbXmlFormatterService;
        this.xmlFormatter = xmlFormatter;
    }

    public String processSCSBXmlString(Exchange exchange) throws Exception {
        List<BibRecord> records = (List<BibRecord>) exchange.getIn().getBody();
        logger.info("Num records to generate scsb XMl for: {} " , records.size());
        long startTime = System.currentTimeMillis();

        String toSCSBXmlString = null;
        String batchHeaders = (String) exchange.getIn().getHeader(RecapConstants.BATCH_HEADERS);
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");
        try {
            toSCSBXmlString = scsbXmlFormatterService.getSCSBXmlForBibRecords(records);
            processSuccessReportEntity(exchange, records.size(), batchHeaders, requestId);
        } catch (Exception e) {
            logger.error(RecapConstants.ERROR,e);
            processFailureReportEntity(exchange, records.size(), batchHeaders, requestId, e);
        }
        long endTime = System.currentTimeMillis();

        logger.info("Time taken to generate scsb xml for : {} is : seconds " , records.size() ,  (endTime - startTime) / 1000 );

        return toSCSBXmlString;
    }

    private void processSuccessReportEntity(Exchange exchange, Integer size, String batchHeaders, String requestId) {
        Map values = new HashMap<>();
        values.put(RecapConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.REQUESTING_INST_CODE));
        values.put(RecapConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.INSTITUTION_CODES));
        values.put(RecapConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.FETCH_TYPE));
        values.put(RecapConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.COLLECTION_GROUP_IDS));
        values.put(RecapConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.TRANSMISSION_TYPE));
        values.put(RecapConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.EXPORT_FROM_DATE));
        values.put(RecapConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.EXPORT_FORMAT));
        values.put(RecapConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.TO_EMAIL_ID));
        values.put(RecapConstants.NUM_RECORDS, String.valueOf(size));
        values.put(RecapConstants.NUM_BIBS_EXPORTED, RecapConstants.NUM_BIBS_EXPORTED);
        values.put(RecapConstants.BATCH_EXPORT, RecapConstants.BATCH_EXPORT_SUCCESS);
        values.put(RecapConstants.REQUEST_ID, requestId);

        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
        fluentProducerTemplate
                .to(RecapConstants.DATADUMP_SUCCESS_REPORT_Q)
                .withBody(values)
                .withHeader(RecapConstants.BATCH_HEADERS, exchange.getIn().getHeader(RecapConstants.BATCH_HEADERS))
                .withHeader(RecapConstants.EXPORT_FORMAT, exchange.getIn().getHeader(RecapConstants.EXPORT_FORMAT))
                .withHeader(RecapConstants.TRANSMISSION_TYPE, exchange.getIn().getHeader(RecapConstants.TRANSMISSION_TYPE));
        fluentProducerTemplate.send();

    }

    private void processFailureReportEntity(Exchange exchange, Integer size, String batchHeaders, String requestId, Exception e) {
        HashMap values = new HashMap();

        values.put(RecapConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.REQUESTING_INST_CODE));
        values.put(RecapConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.INSTITUTION_CODES));
        values.put(RecapConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.FETCH_TYPE));
        values.put(RecapConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.COLLECTION_GROUP_IDS));
        values.put(RecapConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.TRANSMISSION_TYPE));
        values.put(RecapConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.EXPORT_FROM_DATE));
        values.put(RecapConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.EXPORT_FORMAT));
        values.put(RecapConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.TO_EMAIL_ID));
        values.put(RecapConstants.NUM_RECORDS, String.valueOf(size));
        values.put(RecapConstants.FAILURE_CAUSE, String.valueOf(e.getCause()));
        values.put(RecapConstants.FAILED_BIBS, RecapConstants.FAILED_BIBS);
        values.put(RecapConstants.BATCH_EXPORT, RecapConstants.BATCH_EXPORT_FAILURE);
        values.put(RecapConstants.REQUEST_ID, requestId);

        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
        fluentProducerTemplate
                .to(RecapConstants.DATADUMP_FAILURE_REPORT_Q)
                .withBody(values)
                .withHeader(RecapConstants.BATCH_HEADERS, exchange.getIn().getHeader(RecapConstants.BATCH_HEADERS))
                .withHeader(RecapConstants.EXPORT_FORMAT, exchange.getIn().getHeader(RecapConstants.EXPORT_FORMAT))
                .withHeader(RecapConstants.TRANSMISSION_TYPE, exchange.getIn().getHeader(RecapConstants.TRANSMISSION_TYPE));
        fluentProducerTemplate.send();
    }

    public DataExportHeaderUtil getDataExportHeaderUtil() {
        if (null == dataExportHeaderUtil) {
            dataExportHeaderUtil = new DataExportHeaderUtil();
        }
        return dataExportHeaderUtil;
    }

    public void setDataExportHeaderUtil(DataExportHeaderUtil dataExportHeaderUtil) {
        this.dataExportHeaderUtil = dataExportHeaderUtil;
    }


}

