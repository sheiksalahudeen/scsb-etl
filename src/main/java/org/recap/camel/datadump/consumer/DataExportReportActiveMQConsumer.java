package org.recap.camel.datadump.consumer;

import org.recap.ReCAPConstants;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by peris on 11/11/16.
 */

@Component
public class DataExportReportActiveMQConsumer {

    @Autowired
    ReportDetailRepository reportDetailRepository;

    public ReportEntity saveSuccessReportEntity(Map body){
        String requestingInstitutionCode = (String) body.get(ReCAPConstants.REQUESTING_INST_CODE);
        String institutionCodes = (String) body.get(ReCAPConstants.INSTITUTION_CODES);
        String fetchType = (String) body.get(ReCAPConstants.FETCH_TYPE);
        String collectionGroupIds = (String) body.get(ReCAPConstants.COLLECTION_GROUP_IDS);
        String transmissionType = (String) body.get(ReCAPConstants.TRANSMISSION_TYPE);
        String exportFormat = (String) body.get(ReCAPConstants.EXPORT_FORMAT);
        String fromDate = body.get(ReCAPConstants.EXPORT_FROM_DATE) != null ? (String) body.get(ReCAPConstants.EXPORT_FROM_DATE) :"";
        String toEmailId = (String) body.get(ReCAPConstants.TO_EMAIL_ID);
        String type = (String) body.get(ReCAPConstants.BATCH_EXPORT);
        String requestId = (String) (body.get(ReCAPConstants.REQUEST_ID));
        String numBibsExported = (String) body.get(ReCAPConstants.NUM_BIBS_EXPORTED);
        String numRecords = (String) body.get(ReCAPConstants.NUM_RECORDS);

        List<ReportEntity> byFileName = getReportDetailRepository().findByFileNameAndType(requestId,ReCAPConstants.BATCH_EXPORT_SUCCESS);

        ReportEntity reportEntity = null;
        if (CollectionUtils.isEmpty(byFileName)) {
            reportEntity = new ReportEntity();
            reportEntity.setCreatedDate(new Date());
            reportEntity.setInstitutionName(requestingInstitutionCode);
            reportEntity.setType(type);
            reportEntity.setFileName(requestId);

            ArrayList<ReportDataEntity> reportDataEntities = new ArrayList<>();
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntities.add(reportDataEntity);
            reportDataEntity.setHeaderName(numBibsExported);
            reportDataEntity.setHeaderValue(numRecords);
            reportEntity.setReportDataEntities(reportDataEntities);

            ReportDataEntity reportDataEntityReqInst = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityReqInst);
            reportDataEntityReqInst.setHeaderName("RequestingInstitution");
            reportDataEntityReqInst.setHeaderValue(requestingInstitutionCode);

            ReportDataEntity reportDataEntityInstCodes = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityInstCodes);
            reportDataEntityInstCodes.setHeaderName("InstitutionCodes");
            reportDataEntityInstCodes.setHeaderValue(institutionCodes);

            ReportDataEntity reportDataEntityFetchType = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityFetchType);
            reportDataEntityFetchType.setHeaderName("FetchType");
            reportDataEntityFetchType.setHeaderValue(fetchType);

            ReportDataEntity reportDataEntityFromDate = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityFromDate);
            reportDataEntityFromDate.setHeaderName("ExportFromDate");
            reportDataEntityFromDate.setHeaderValue(fromDate.replaceAll("null",""));

            ReportDataEntity reportDataEntityCollecGroupIds = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityCollecGroupIds);
            reportDataEntityCollecGroupIds.setHeaderName("CollectionGroupIds");
            reportDataEntityCollecGroupIds.setHeaderValue(collectionGroupIds);

            ReportDataEntity reportDataEntityTransType = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityTransType);
            reportDataEntityTransType.setHeaderName("TransmissionType");
            reportDataEntityTransType.setHeaderValue(transmissionType);

            ReportDataEntity reportDataEntityExportFormat = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityExportFormat);
            reportDataEntityExportFormat.setHeaderName("ExportFormat");
            reportDataEntityExportFormat.setHeaderValue(exportFormat);

            ReportDataEntity reportDataEntityMailedTo = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityMailedTo);
            reportDataEntityMailedTo.setHeaderName("ToEmailId");
            reportDataEntityMailedTo.setHeaderValue(toEmailId);

        } else {
            reportEntity = byFileName.get(0);
            List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
            for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
                ReportDataEntity reportDataEntity = iterator.next();
                if (reportDataEntity.getHeaderName().equals(numBibsExported)) {
                    reportDataEntity.setHeaderValue(String.valueOf(Integer.valueOf(reportDataEntity.getHeaderValue()) + Integer.valueOf(numRecords)));
                }
            }
        }

        getReportDetailRepository().save(reportEntity);

        return reportEntity;
    }

    public ReportEntity saveFailureReportEntity(Map body){

        String requestingInstitutionCode = (String) body.get(ReCAPConstants.REQUESTING_INST_CODE);
        String institutionCodes = (String) body.get(ReCAPConstants.INSTITUTION_CODES);
        String fetchType = (String) body.get(ReCAPConstants.FETCH_TYPE);
        String collectionGroupIds = (String) body.get(ReCAPConstants.COLLECTION_GROUP_IDS);
        String transmissionType = (String) body.get(ReCAPConstants.TRANSMISSION_TYPE);
        String exportFormat = (String) body.get(ReCAPConstants.EXPORT_FORMAT);
        String fromDate = body.get(ReCAPConstants.EXPORT_FROM_DATE) != null ? (String) body.get(ReCAPConstants.EXPORT_FROM_DATE) :"";
        String toEmailId = (String) body.get(ReCAPConstants.TO_EMAIL_ID);
        String type = (String) body.get(ReCAPConstants.BATCH_EXPORT);
        String requestId = (String) (body.get(ReCAPConstants.REQUEST_ID));
        String failedBibs = (String) body.get(ReCAPConstants.FAILED_BIBS);
        String numRecords = (String) body.get(ReCAPConstants.NUM_RECORDS);
        String failureCause = (String) body.get(ReCAPConstants.FAILURE_CAUSE);

        List<ReportEntity> byFileName = getReportDetailRepository().findByFileNameAndType(requestId,ReCAPConstants.BATCH_EXPORT_FAILURE);


        ReportEntity reportEntity = null;
        if (CollectionUtils.isEmpty(byFileName)) {
            reportEntity = new ReportEntity();
            reportEntity.setCreatedDate(new Date());
            reportEntity.setInstitutionName(requestingInstitutionCode);
            reportEntity.setType(type);
            reportEntity.setFileName(requestId);

            ArrayList<ReportDataEntity> reportDataEntities = new ArrayList<>();
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntities.add(reportDataEntity);
            reportDataEntity.setHeaderName(failedBibs);
            reportDataEntity.setHeaderValue(numRecords);
            reportEntity.setReportDataEntities(reportDataEntities);

            ReportDataEntity reportDataEntityReqInst = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityReqInst);
            reportDataEntityReqInst.setHeaderName("RequestingInstitution");
            reportDataEntityReqInst.setHeaderValue(requestingInstitutionCode);

            ReportDataEntity reportDataEntityFailureCause = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityFailureCause);
            reportDataEntityFailureCause.setHeaderName(ReCAPConstants.FAILURE_CAUSE);
            reportDataEntityFailureCause.setHeaderValue(failureCause);

            ReportDataEntity reportDataEntityInstCodes = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityInstCodes);
            reportDataEntityInstCodes.setHeaderName("InstitutionCodes");
            reportDataEntityInstCodes.setHeaderValue(institutionCodes);

            ReportDataEntity reportDataEntityFetchType = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityFetchType);
            reportDataEntityFetchType.setHeaderName("FetchType");
            reportDataEntityFetchType.setHeaderValue(fetchType);

            ReportDataEntity reportDataEntityFromDate = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityFromDate);
            reportDataEntityFromDate.setHeaderName("ExportFromDate");
            reportDataEntityFromDate.setHeaderValue(fromDate.replaceAll("null",""));

            ReportDataEntity reportDataEntityCollecGroupIds = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityCollecGroupIds);
            reportDataEntityCollecGroupIds.setHeaderName("CollectionGroupIds");
            reportDataEntityCollecGroupIds.setHeaderValue(collectionGroupIds);

            ReportDataEntity reportDataEntityTransType = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityTransType);
            reportDataEntityTransType.setHeaderName("TransmissionType");
            reportDataEntityTransType.setHeaderValue(transmissionType);

            ReportDataEntity reportDataEntityExportFormat = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityExportFormat);
            reportDataEntityExportFormat.setHeaderName("ExportFormat");
            reportDataEntityExportFormat.setHeaderValue(exportFormat);

            ReportDataEntity reportDataEntityMailedTo = new ReportDataEntity();
            reportDataEntities.add(reportDataEntityMailedTo);
            reportDataEntityMailedTo.setHeaderName("ToEmailId");
            reportDataEntityMailedTo.setHeaderValue(toEmailId);

        } else {
            reportEntity = byFileName.get(0);
            List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
            for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
                ReportDataEntity reportDataEntity = iterator.next();
                if (reportDataEntity.getHeaderName().equals(ReCAPConstants.FAILED_BIBS)) {
                    Integer exitingRecords = Integer.valueOf(reportDataEntity.getHeaderValue());
                    reportDataEntity.setHeaderValue(String.valueOf(exitingRecords + Integer.valueOf(numRecords)));
                }
                if(reportDataEntity.getHeaderName().equals(ReCAPConstants.FAILURE_CAUSE)){
                    String existingfailureCause = reportDataEntity.getHeaderValue();
                    failureCause = existingfailureCause +" * "+failureCause;
                }
            }
            ReportDataEntity reportDataEntity1 = new ReportDataEntity();
            reportDataEntity1.setHeaderName(ReCAPConstants.FAILURE_CAUSE);
            reportDataEntity1.setHeaderValue(failureCause);
            reportDataEntities.add(reportDataEntity1);
        }

        getReportDetailRepository().save(reportEntity);

        return reportEntity;
    }

    private ReportDetailRepository getReportDetailRepository() {
        return reportDetailRepository;
    }

    public void setReportDetailRepository(ReportDetailRepository reportDetailRepository) {
        this.reportDetailRepository = reportDetailRepository;
    }

}
