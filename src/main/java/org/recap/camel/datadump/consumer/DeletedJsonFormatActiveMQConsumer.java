package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.export.DeletedRecord;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peris on 11/1/16.
 */

public class DeletedJsonFormatActiveMQConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DeletedJsonFormatActiveMQConsumer.class);

    DeletedJsonFormatterService deletedJsonFormatterService;
    private DataExportHeaderUtil dataExportHeaderUtil;

    public DeletedJsonFormatActiveMQConsumer(DeletedJsonFormatterService deletedJsonFormatterService) {
        this.deletedJsonFormatterService = deletedJsonFormatterService;
    }

    public String processDeleteJsonString(Exchange exchange) throws Exception {
        List<DeletedRecord> deletedRecordList = (List<DeletedRecord>) exchange.getIn().getBody();
        logger.info("Num records to generate json for: {} " , deletedRecordList.size());
        long startTime = System.currentTimeMillis();

        String deletedJsonString = null;
        String batchHeaders = (String) exchange.getIn().getHeader(ReCAPConstants.BATCH_HEADERS);
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");

        try {
            String formattedOutputForDeletedRecords = deletedJsonFormatterService.getJsonForDeletedRecords(deletedRecordList);
            deletedJsonString = String.format(formattedOutputForDeletedRecords);
            processSuccessReportEntity(exchange, deletedRecordList.size(), batchHeaders, requestId);
        } catch (Exception e) {
            logger.error(ReCAPConstants.ERROR,e);
            processFailureReportEntity(exchange, deletedRecordList.size(), batchHeaders, requestId, e);
        }
        long endTime = System.currentTimeMillis();

        logger.info("Time taken to generate json for : {} is : {} seconds " , deletedRecordList.size() , (endTime-startTime)/1000 );

        return deletedJsonString;
    }

    private void processSuccessReportEntity(Exchange exchange, Integer size, String batchHeaders, String requestId) {
        Map values = new HashMap<>();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.REQUESTING_INST_CODE));
        values.put(ReCAPConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.INSTITUTION_CODES));
        values.put(ReCAPConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.FETCH_TYPE));
        values.put(ReCAPConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.COLLECTION_GROUP_IDS));
        values.put(ReCAPConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TRANSMISSION_TYPE));
        values.put(ReCAPConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FROM_DATE));
        values.put(ReCAPConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FORMAT));
        values.put(ReCAPConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TO_EMAIL_ID));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(size));
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

    private void processFailureReportEntity(Exchange exchange, Integer size, String batchHeaders, String requestId, Exception e) {
        Map values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.REQUESTING_INST_CODE));
        values.put(ReCAPConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.INSTITUTION_CODES));
        values.put(ReCAPConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.FETCH_TYPE));
        values.put(ReCAPConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.COLLECTION_GROUP_IDS));
        values.put(ReCAPConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TRANSMISSION_TYPE));
        values.put(ReCAPConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FROM_DATE));
        values.put(ReCAPConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FORMAT));
        values.put(ReCAPConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TO_EMAIL_ID));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(size));
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
}

