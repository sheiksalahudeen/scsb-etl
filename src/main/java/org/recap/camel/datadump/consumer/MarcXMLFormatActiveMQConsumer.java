package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peris on 11/1/16.
 */

public class MarcXMLFormatActiveMQConsumer {
    Logger logger = LoggerFactory.getLogger(BibEntityGeneratorActiveMQConsumer.class);

    private MarcXmlFormatterService marcXmlFormatterService;
    private DataExportHeaderUtil dataExportHeaderUtil;

    public MarcXMLFormatActiveMQConsumer(MarcXmlFormatterService marcXmlFormatterService) {
        this.marcXmlFormatterService = marcXmlFormatterService;
    }

    public String processMarcXmlString(Exchange exchange) throws Exception {
        List<Record> records = (List<Record>) exchange.getIn().getBody();
        logger.info("Num records to generate marc XMl for: {} " , records.size());
        long startTime = System.currentTimeMillis();

        String toMarcXmlString = null;
        String batchHeaders = (String) exchange.getIn().getHeader(ReCAPConstants.BATCH_HEADERS);
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");
        try {
            toMarcXmlString = marcXmlFormatterService.covertToMarcXmlString(records);
            processSuccessReportEntity(exchange, records, batchHeaders, requestId);
        } catch (Exception e) {
            logger.error(ReCAPConstants.ERROR,e);
            processFailureReportEntity(exchange, records, batchHeaders, requestId, e);
        }

        long endTime = System.currentTimeMillis();

        logger.info("Time taken to generate marc xml for : {} is : {} seconds " , records.size() , (endTime - startTime) / 1000 );

        return toMarcXmlString;
    }

    private void processSuccessReportEntity(Exchange exchange, List<Record> records, String batchHeaders, String requestId) {
        Map values = new HashMap<>();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.REQUESTING_INST_CODE));
        values.put(ReCAPConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.INSTITUTION_CODES));
        values.put(ReCAPConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.FETCH_TYPE));
        values.put(ReCAPConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.COLLECTION_GROUP_IDS));
        values.put(ReCAPConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TRANSMISSION_TYPE));
        values.put(ReCAPConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FROM_DATE));
        values.put(ReCAPConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FORMAT));
        values.put(ReCAPConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TO_EMAIL_ID));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(records.size()));
        values.put(ReCAPConstants.NUM_BIBS_EXPORTED, ReCAPConstants.NUM_BIBS_EXPORTED);
        values.put(ReCAPConstants.BATCH_EXPORT, ReCAPConstants.BATCH_EXPORT_SUCCESS);
        values.put(ReCAPConstants.REQUEST_ID, requestId);

        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
        fluentProducerTemplate
                .to(ReCAPConstants.DATADUMP_SUCCESS_REPORT_Q)
                .withBody(values)
                .withHeader(ReCAPConstants.BATCH_HEADERS, exchange.getIn().getHeader(ReCAPConstants.BATCH_HEADERS))
                .withHeader(ReCAPConstants.EXPORT_FORMAT, exchange.getIn().getHeader(ReCAPConstants.EXPORT_FORMAT))
                .withHeader(ReCAPConstants.TRANSMISSION_TYPE, exchange.getIn().getHeader(ReCAPConstants.TRANSMISSION_TYPE));
        fluentProducerTemplate.send();

    }

    private void processFailureReportEntity(Exchange exchange, List<Record> records, String batchHeaders, String requestId, Exception e) {
        HashMap values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.REQUESTING_INST_CODE));
        values.put(ReCAPConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.INSTITUTION_CODES));
        values.put(ReCAPConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.FETCH_TYPE));
        values.put(ReCAPConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.COLLECTION_GROUP_IDS));
        values.put(ReCAPConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TRANSMISSION_TYPE));
        values.put(ReCAPConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FROM_DATE));
        values.put(ReCAPConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FORMAT));
        values.put(ReCAPConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TO_EMAIL_ID));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(records.size()));
        values.put(ReCAPConstants.FAILURE_CAUSE, String.valueOf(e.getCause()));
        values.put(ReCAPConstants.FAILED_BIBS, ReCAPConstants.FAILED_BIBS);
        values.put(ReCAPConstants.BATCH_EXPORT, ReCAPConstants.BATCH_EXPORT_FAILURE);
        values.put(ReCAPConstants.REQUEST_ID, requestId);

        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
        fluentProducerTemplate
                .to(ReCAPConstants.DATADUMP_FAILURE_REPORT_Q)
                .withBody(values)
                .withHeader(ReCAPConstants.BATCH_HEADERS, exchange.getIn().getHeader(ReCAPConstants.BATCH_HEADERS))
                .withHeader(ReCAPConstants.EXPORT_FORMAT, exchange.getIn().getHeader(ReCAPConstants.EXPORT_FORMAT))
                .withHeader(ReCAPConstants.TRANSMISSION_TYPE, exchange.getIn().getHeader(ReCAPConstants.TRANSMISSION_TYPE));
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

