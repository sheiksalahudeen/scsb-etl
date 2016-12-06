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

    @Value("${etl.dump.directory}")
    private String fileSystemDataDumpDirectory;

    @Value("${ftp.datadump.remote.server}")
    private String ftpDataDumpDirectory;

    @Autowired
    private ProducerTemplate producer;

    public void sendEmail(List<String> institutionCodes, Integer totalRecordCount, Integer failedRecordCount, String transmissionType, String dateTimeStringForFolder, String toEmailAddress,String emailBodyFor) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setInstitutions(institutionCodes);
        emailPayLoad.setLocation(getLocation(transmissionType,dateTimeStringForFolder));
        emailPayLoad.setCount(totalRecordCount);
        emailPayLoad.setFailedCount(failedRecordCount);
        emailPayLoad.setTo(toEmailAddress);
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad,ReCAPConstants.DATADUMP_EMAILBODY_FOR,emailBodyFor);
    }

    private String getLocation(String transmissionType,String dateTimeStringForFolder) {
        String location = null;
        if (transmissionType.equals("0")) {
            location = "FTP location - " + ftpDataDumpDirectory + File.separator + dateTimeStringForFolder;
        } else if (transmissionType.equals("2")) {
            location = "File System - " + fileSystemDataDumpDirectory + File.separator + dateTimeStringForFolder;
        }
        return location;
    }

}