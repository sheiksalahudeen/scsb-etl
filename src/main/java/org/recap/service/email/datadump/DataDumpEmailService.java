package org.recap.service.email.datadump;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * Created by premkb on 21/9/16.
 */
@Service
public class DataDumpEmailService {

    @Value("${data.dump.email.nypl.to}")
    private String dataDumpEmailNyplTo;

    @Value("${data.dump.email.pul.to}")
    private String dataDumpEmailPulTo;

    @Value("${data.dump.email.cul.to}")
    private String dataDumpEmailCulTo;

    @Value("${etl.dump.directory}")
    private String fileSystemDataDumpDirectory;

    @Value("${ftp.datadump.remote.server}")
    private String ftpDataDumpDirectory;

    @Autowired
    private ProducerTemplate producer;

    public void sendEmail(List<String> institutionCodes, Long totalRecordCount, String requestingInstitutionCode, String transmissionType, String dateTimeStringForFolder) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setInstitutions(institutionCodes);
        emailPayLoad.setLocation(getLocation(transmissionType, requestingInstitutionCode,dateTimeStringForFolder));
        emailPayLoad.setCount(totalRecordCount);
        emailPayLoad.setTo(getEmailTo(requestingInstitutionCode));
        producer.sendBody(ReCAPConstants.EMAIL_Q, emailPayLoad);
    }

    private String getLocation(String transmissionType, String requestingInstitutionCode,String dateTimeStringForFolder) {
        String location = null;
        if (transmissionType.equals("0")) {
            location = "FTP location - " + ftpDataDumpDirectory + File.separator + requestingInstitutionCode + File.separator + dateTimeStringForFolder;
        } else if (transmissionType.equals("2")) {
            location = "File System - " + fileSystemDataDumpDirectory + File.separator + requestingInstitutionCode + File.separator + dateTimeStringForFolder;
        }
        return location;
    }

    private String getEmailTo(String requestingInstitutionCode) {
        String emailTo = null;
        if (requestingInstitutionCode.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            emailTo = dataDumpEmailCulTo;
        } else if (requestingInstitutionCode.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            emailTo = dataDumpEmailPulTo;
        } else if (requestingInstitutionCode.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            emailTo = dataDumpEmailNyplTo;
        }
        return emailTo;
    }
}