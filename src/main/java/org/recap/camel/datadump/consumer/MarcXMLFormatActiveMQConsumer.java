package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.DataExportHeaderUtil;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.util.datadump.DataExportReportEntityHelper;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by peris on 11/1/16.
 */

public class MarcXMLFormatActiveMQConsumer {

    private MarcXmlFormatterService marcXmlFormatterService;
    private ReportDetailRepository reportDetailRepository;
    private DataExportHeaderUtil dataExportHeaderUtil;
    private DataExportReportEntityHelper dataExportReportEntityHelper;

    public MarcXMLFormatActiveMQConsumer(MarcXmlFormatterService marcXmlFormatterService, ReportDetailRepository reportDetailRepository, DataExportReportEntityHelper dataExportReportEntityHelper) {
        this.marcXmlFormatterService = marcXmlFormatterService;
        this.reportDetailRepository = reportDetailRepository;
        this.dataExportReportEntityHelper = dataExportReportEntityHelper;
    }

    public String processMarcXmlString(Exchange exchange) throws Exception {
        List<Record> records = (List<Record>) exchange.getIn().getBody();
        System.out.println("Num records to generate marc XMl for: " + records.size());
        long startTime = System.currentTimeMillis();

        String toMarcXmlString = null;
        String batchHeaders = (String) exchange.getIn().getHeader("batchHeaders");
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");
        try {
            toMarcXmlString = marcXmlFormatterService.covertToMarcXmlString(records);
            processSuccessReportEntity(records, batchHeaders, requestId);
        } catch (Exception e) {
            processFailureReportEntity(records, batchHeaders, requestId, e);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate marc xml for :" + records.size() + " is : " + (endTime - startTime) / 1000 + " seconds ");

        return toMarcXmlString;
    }

    private void processFailureReportEntity(List<Record> records, String batchHeaders, String requestId, Exception e) {
        List<ReportEntity> byFileName = reportDetailRepository.findByFileName
                (requestId);

        HashMap values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, "requestingInstitutionCode"));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(records.size()));
        values.put(ReCAPConstants.FAILURE_CAUSE, e.getMessage());
        values.put(ReCAPConstants.BATCH_EXPORT, "Batch Export");
        values.put(ReCAPConstants.REQUEST_ID, requestId);

        ReportEntity reportEntity = new ReportEntity();
        dataExportReportEntityHelper.processFailureReportEntity(byFileName, values);
        reportDetailRepository.save(reportEntity);
    }

    private void processSuccessReportEntity(List<Record> records, String batchHeaders, String requestId) {
        Map values = new HashMap<>();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, "requestingInstitutionCode"));
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(records.size()));
        values.put(ReCAPConstants.NUM_BIBS_EXPORTED, ReCAPConstants.NUM_BIBS_EXPORTED);
        values.put(ReCAPConstants.BATCH_EXPORT, ReCAPConstants.BATCH_EXPORT);
        values.put(ReCAPConstants.REQUEST_ID, requestId);

        List<ReportEntity> byFileName = reportDetailRepository.findByFileName
                (requestId);
        ReportEntity reportEntity = dataExportReportEntityHelper.processSuccessReportEntity(byFileName, values);

        reportDetailRepository.save(reportEntity);
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

