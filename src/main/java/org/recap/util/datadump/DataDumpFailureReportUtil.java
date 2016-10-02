package org.recap.util.datadump;

import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by premkb on 30/9/16.
 */
@Component
public class DataDumpFailureReportUtil {

    public List<ReportDataEntity> generateDataDumpFailureReport(List<Map<String,Object>> successAndFailureFormattedFullList, DataDumpRequest dataDumpRequest){
        int totalNoOfBibsFailedExported = 0;
        StringBuffer formatError = new StringBuffer();
        int count = 0;
        for(Map<String,Object>  successAndFailureFormattedList:successAndFailureFormattedFullList){
            List<BibliographicEntity> failureList = (List<BibliographicEntity>)successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_SUCCESSLIST);
            totalNoOfBibsFailedExported = totalNoOfBibsFailedExported+failureList.size();
            if(count>0 && formatError.length()>0){
                formatError.append(",");
            }
            if(successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_FORMATERROR) != null){
                formatError.append((String)successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_FORMATERROR));
            }
            count++;
        }
        List<ReportDataEntity> reportEntities = new ArrayList<>();
        if (dataDumpRequest.getInstitutionCodes()!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("InstitutionCodes");
            reportDataEntity.setHeaderValue(getStringFromStringList(dataDumpRequest.getInstitutionCodes()));
            reportEntities.add(reportDataEntity);
        }
        if (dataDumpRequest.getRequestingInstitutionCode()!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("RequestingInstitution");
            reportDataEntity.setHeaderValue(dataDumpRequest.getRequestingInstitutionCode());
            reportEntities.add(reportDataEntity);
        }
        if (dataDumpRequest.getFetchType()!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("FetchType");
            reportDataEntity.setHeaderValue(dataDumpRequest.getFetchType());
            reportEntities.add(reportDataEntity);
        }
        if (dataDumpRequest.getDate()!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("ExportFromdate");
            reportDataEntity.setHeaderValue(dataDumpRequest.getDate());
            reportEntities.add(reportDataEntity);
        }
        if (dataDumpRequest.getCollectionGroupIds()!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("CollectionGroupIds");
            reportDataEntity.setHeaderValue(getStringFromIntegerList(dataDumpRequest.getCollectionGroupIds()));
            reportEntities.add(reportDataEntity);
        }
        if (dataDumpRequest.getTransmissionType()!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("TransmissionType");
            reportDataEntity.setHeaderValue(dataDumpRequest.getTransmissionType());
            reportEntities.add(reportDataEntity);
        }
        if (String.valueOf(dataDumpRequest.getBatchSize())!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("NoOfRecordsPerFile");
            reportDataEntity.setHeaderValue(String.valueOf(dataDumpRequest.getBatchSize()));
            reportEntities.add(reportDataEntity);
        }
        if (successAndFailureFormattedFullList!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("NoOfBibsFailedToExport");
            reportDataEntity.setHeaderValue(String.valueOf(totalNoOfBibsFailedExported));
            reportEntities.add(reportDataEntity);
        }
        if (formatError!=null) {
            ReportDataEntity reportDataEntity = new ReportDataEntity();
            reportDataEntity.setHeaderName("Error");
            reportDataEntity.setHeaderValue(String.valueOf(formatError));
            reportEntities.add(reportDataEntity);
        }
        return reportEntities;
    }

    private String getStringFromStringList(List<String> stringList){
        StringBuilder stringFromList = new StringBuilder();
        int count = 0;
        for(String value : stringList){
            if(count>0){
                stringFromList.append(",");
            }
            stringFromList.append(value);
            count++;
        }
        return stringFromList.toString();
    }

    private String getStringFromIntegerList(List<Integer> integerList){
        int count = 0;
        StringBuilder stringFromList = new StringBuilder();
        for(Integer value : integerList){
            if(count>0){
                stringFromList.append(",");
            }
            stringFromList.append(value);
        }
        return stringFromList.toString();
    }
}
