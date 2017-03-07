package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.recap.RecapConstants;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;
import org.recap.service.email.datadump.DataDumpEmailService;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by peris on 11/5/16.
 */

@Component
public class DataExportEmailProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(DataExportEmailProcessor.class);
    @Autowired
    DataDumpEmailService dataDumpEmailService;

    @Autowired
    ReportDetailRepository reportDetailRepository;

    @Autowired
    DataExportHeaderUtil dataExportHeaderUtil;

    @Value("${datadump.status.file.name}")
    String dataDumpStatusFileName;

    @Value("${datadump.fetchtype.full}")
    private String fetchTypeFull;

    private String transmissionType;
    private List<String> institutionCodes;
    private String requestingInstitutionCode;
    private String folderName;
    private String toEmailId;
    private String requestId;
    private String fetchType;


    @Override
    public void process(Exchange exchange) throws Exception {
        String totalRecordCount = "0";
        String failedBibs = "0";
        List<ReportEntity> byFileName = reportDetailRepository.findByFileName(requestId);
        for (ReportEntity reportEntity:byFileName) {
            List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();
            for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
                ReportDataEntity reportDataEntity = iterator.next();
                if(reportDataEntity.getHeaderName().equals(RecapConstants.NUM_BIBS_EXPORTED)){
                    totalRecordCount = reportDataEntity.getHeaderValue();
                }
                if(reportDataEntity.getHeaderName().equals(RecapConstants.FAILED_BIBS)){
                    failedBibs = reportDataEntity.getHeaderValue();
                }
            }
        }
        processEmail(totalRecordCount,failedBibs);
        if(fetchType.equals(fetchTypeFull)) {
            writeFullDumpStatusToFile();
        }
    }

    private void writeFullDumpStatusToFile() throws IOException {
        File file = new File(dataDumpStatusFileName);
        FileWriter fileWriter = new FileWriter(file, false);
        try {
            fileWriter.append(RecapConstants.COMPLETED);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            logger.error(RecapConstants.EXCEPTION,e);
        } finally {
            fileWriter.close();
        }
    }

    private void processEmail(String totalRecordCount,String failedBibs){
        if (transmissionType.equals(RecapConstants.DATADUMP_TRANSMISSION_TYPE_FTP)
                ||transmissionType.equals(RecapConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM)) {
            dataDumpEmailService.sendEmail(institutionCodes,
                    Integer.valueOf(totalRecordCount),
                    Integer.valueOf(failedBibs),
                    transmissionType,
                    this.folderName,
                    toEmailId,
                    RecapConstants.DATADUMP_DATA_AVAILABLE
            );
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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getToEmailId() {
        return toEmailId;
    }

    public void setToEmailId(String toEmailId) {
        this.toEmailId = toEmailId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFetchType() {
        return fetchType;
    }

    public void setFetchType(String fetchType) {
        this.fetchType = fetchType;
    }
}
