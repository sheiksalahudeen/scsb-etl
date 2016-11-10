package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.recap.service.email.datadump.DataDumpEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by peris on 11/5/16.
 */

@Component
public class DataExportEmailProcessor implements Processor {

    @Autowired
    DataDumpEmailService dataDumpEmailService;

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    DataExportHeaderUtil dataExportHeaderUtil;

    private String transmissionType;
    private List<String> institutionCodes;
    private String requestingInstitutionCode;
    private String dateTimeStringForFolder;
    private String toEmailId;


    @Override
    public void process(Exchange exchange) throws Exception {
        String totalRecordCount = null;
        List<ReportEntity> byFileName = reportDetailRepository.findByFileName(dataExportHeaderUtil.getValueFor((String) exchange.getIn().getHeader("batchHeaders"), "requestId"));
        List<ReportDataEntity> reportDataEntities = byFileName.get(0).getReportDataEntities();
        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity reportDataEntity = iterator.next();
            if(reportDataEntity.getHeaderName().equals("Num Bibs Exported")){
                totalRecordCount = reportDataEntity.getHeaderValue();
            }
        }
        processEmail(totalRecordCount);
    }

    private void processEmail(String totalRecordCount){
        if (transmissionType.equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)
                ||transmissionType.equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM)) {
            dataDumpEmailService.sendEmail(institutionCodes,
                    Integer.valueOf(totalRecordCount),
                    requestingInstitutionCode,
                    transmissionType,
                    this.dateTimeStringForFolder,
                    toEmailId);
        }
    }

    public DataDumpEmailService getDataDumpEmailService() {
        return dataDumpEmailService;
    }

    public void setDataDumpEmailService(DataDumpEmailService dataDumpEmailService) {
        this.dataDumpEmailService = dataDumpEmailService;
    }

    public String getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }

    public List<String> getInstitutionCodes() {
        return institutionCodes;
    }

    public void setInstitutionCodes(List<String> institutionCodes) {
        this.institutionCodes = institutionCodes;
    }

    public String getRequestingInstitutionCode() {
        return requestingInstitutionCode;
    }

    public void setRequestingInstitutionCode(String requestingInstitutionCode) {
        this.requestingInstitutionCode = requestingInstitutionCode;
    }

    public String getDateTimeStringForFolder() {
        return dateTimeStringForFolder;
    }

    public void setDateTimeStringForFolder(String dateTimeStringForFolder) {
        this.dateTimeStringForFolder = dateTimeStringForFolder;
    }

    public String getToEmailId() {
        return toEmailId;
    }

    public void setToEmailId(String toEmailId) {
        this.toEmailId = toEmailId;
    }
}
