package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.DataExportHeaderUtil;
import org.recap.model.export.DeletedRecord;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by peris on 11/1/16.
 */

public class DeletedJsonFormatActiveMQConsumer {

    DeletedJsonFormatterService deletedJsonFormatterService;
    private DataExportHeaderUtil dataExportHeaderUtil;
    private ProducerTemplate producerTemplate;

    public DeletedJsonFormatActiveMQConsumer(ProducerTemplate producerTemplate, DeletedJsonFormatterService deletedJsonFormatterService) {
        this.deletedJsonFormatterService = deletedJsonFormatterService;
        this.producerTemplate = producerTemplate;
    }

    public String processDeleteJsonString(Exchange exchange) throws Exception {
        List<DeletedRecord> deletedRecordList = (List<DeletedRecord>) exchange.getIn().getBody();
        System.out.println("Num records to generate json for: " + deletedRecordList.size());
        long startTime = System.currentTimeMillis();

        String deletedJsonString = null;
        String batchHeaders = (String) exchange.getIn().getHeader("batchHeaders");
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");

        try {
            String formattedOutputForDeletedRecords = deletedJsonFormatterService.getJsonForDeletedRecords(deletedRecordList);
            deletedJsonString = formattedOutputForDeletedRecords.format(formattedOutputForDeletedRecords);
            processSuccessReportEntity(deletedRecordList.size(), batchHeaders, requestId);
        } catch (Exception e) {
            processFailureReportEntity(deletedRecordList.size(), batchHeaders, requestId, e);
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate json for :"  + deletedRecordList.size() + " is : " + (endTime-startTime)/1000 + " seconds ");

        return deletedJsonString;
    }

    private void processSuccessReportEntity(Integer size, String batchHeaders, String requestId) {
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

        producerTemplate.sendBody(ReCAPConstants.DATADUMP_SUCCESS_REPORT_Q, values);

    }

    private void processFailureReportEntity(Integer size, String batchHeaders, String requestId, Exception e) {
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

        producerTemplate.sendBody(ReCAPConstants.DATADUMP_FAILURE_REPORT_Q, values);
    }

    public DataExportHeaderUtil getDataExportHeaderUtil() {
        if (null == dataExportHeaderUtil) {
            dataExportHeaderUtil = new DataExportHeaderUtil();
        }
        return dataExportHeaderUtil;
    }
}

