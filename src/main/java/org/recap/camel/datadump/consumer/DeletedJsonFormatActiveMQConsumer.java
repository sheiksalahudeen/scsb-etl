package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.DataExportHeaderUtil;
import org.recap.model.export.DeletedRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;
import org.springframework.util.CollectionUtils;

import java.util.*;

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

        String formattedOutputForDeletedRecords = deletedJsonFormatterService.getJsonForDeletedRecords(deletedRecordList);
        String deletedJsonString = null;
        String batchHeaders = (String) exchange.getIn().getHeader("batchHeaders");
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");

        try {
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
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, "requestingInstitutionCode"));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(size));
        values.put(ReCAPConstants.NUM_BIBS_EXPORTED, ReCAPConstants.NUM_BIBS_EXPORTED);
        values.put(ReCAPConstants.BATCH_EXPORT, ReCAPConstants.BATCH_EXPORT);
        values.put(ReCAPConstants.REQUEST_ID, requestId);

        producerTemplate.sendBody(ReCAPConstants.DATADUMP_SUCCESS_REPORT_Q, values);

    }

    private void processFailureReportEntity(Integer size, String batchHeaders, String requestId, Exception e) {
        HashMap values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, "requestingInstitutionCode"));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(size));
        values.put(ReCAPConstants.FAILURE_CAUSE, e.getMessage());
        values.put(ReCAPConstants.BATCH_EXPORT, "Batch Export");
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

