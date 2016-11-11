package org.recap.camel.datadump.consumer;

import org.apache.camel.Exchange;
import org.recap.camel.datadump.DataExportHeaderUtil;
import org.recap.model.export.DeletedRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by peris on 11/1/16.
 */

public class DeletedJsonFormatActiveMQConsumer {

    DeletedJsonFormatterService deletedJsonFormatterService;
    ReportDetailRepository reportDetailRepository;
    private DataExportHeaderUtil dataExportHeaderUtil;

    public DeletedJsonFormatActiveMQConsumer(DeletedJsonFormatterService deletedJsonFormatterService,ReportDetailRepository reportDetailRepository) {
        this.deletedJsonFormatterService = deletedJsonFormatterService;
        this.reportDetailRepository = reportDetailRepository;
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
            List<ReportEntity> byFileName = reportDetailRepository.findByFileName
                    (requestId);

            if(CollectionUtils.isEmpty(byFileName)){
                ReportEntity reportEntity = new ReportEntity();
                reportEntity.setCreatedDate(new Date());
                reportEntity.setInstitutionName(getDataExportHeaderUtil().getValueFor(batchHeaders, "requestingInstitutionCode"));
                reportEntity.setType("Batch Export");
                reportEntity.setFileName(requestId);
                ArrayList<ReportDataEntity> reportDataEntities = new ArrayList<>();
                ReportDataEntity reportDataEntity = new ReportDataEntity();
                reportDataEntities.add(reportDataEntity);
                reportDataEntity.setHeaderName("Num Bibs Exported");
                reportDataEntity.setHeaderValue(String.valueOf(deletedRecordList.size()));
                reportEntity.setReportDataEntities(reportDataEntities);
                reportDetailRepository.save(reportEntity);
            } else {
                ReportEntity reportEntity = byFileName.get(0);
                List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
                for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
                    ReportDataEntity reportDataEntity = iterator.next();
                    if(reportDataEntity.getHeaderName().equals("Num Bibs Exported")){
                        reportDataEntity.setHeaderValue(String.valueOf(Integer.valueOf(reportDataEntity.getHeaderValue())+deletedRecordList.size()));
                    }
                }
                reportDetailRepository.save(reportEntity);
            }
        } catch (Exception e) {
            List<ReportEntity> byFileName = reportDetailRepository.findByFileName
                    (requestId);
            if(CollectionUtils.isEmpty(byFileName)){
                ReportEntity reportEntity = new ReportEntity();
                reportEntity.setCreatedDate(new Date());
                reportEntity.setInstitutionName(getDataExportHeaderUtil().getValueFor(batchHeaders, "requestingInstitutionCode"));
                reportEntity.setType("Batch Export");
                reportEntity.setFileName(requestId);
                ArrayList<ReportDataEntity> reportDataEntities = new ArrayList<>();
                ReportDataEntity reportDataEntity = new ReportDataEntity();
                reportDataEntities.add(reportDataEntity);
                reportDataEntity.setHeaderName("Failed Bibs");
                reportDataEntity.setHeaderValue(String.valueOf(deletedRecordList.size()));
                reportDataEntity.setHeaderName("Failed Bibs cause");
                reportDataEntity.setHeaderValue(e.getMessage());
                reportEntity.setReportDataEntities(reportDataEntities);
                reportDetailRepository.save(reportEntity);
            } else {
                ReportEntity reportEntity = byFileName.get(0);
                List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
                for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
                    ReportDataEntity reportDataEntity = iterator.next();
                    if(reportDataEntity.getHeaderName().equals("Failed Bibs")){
                        reportDataEntity.setHeaderValue(String.valueOf(Integer.valueOf(reportDataEntity.getHeaderValue())+deletedRecordList.size()));
                        reportDataEntity.setHeaderName("Failed Bibs cause");
                        reportDataEntity.setHeaderValue(e.getMessage());
                    }
                }
                reportDetailRepository.save(reportEntity);
            }
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Time taken to generate json for :"  + deletedRecordList.size() + " is : " + (endTime-startTime)/1000 + " seconds ");

        return deletedJsonString;
    }

    public DataExportHeaderUtil getDataExportHeaderUtil() {
        if (null == dataExportHeaderUtil) {
            dataExportHeaderUtil = new DataExportHeaderUtil();
        }
        return dataExportHeaderUtil;
    }
}

