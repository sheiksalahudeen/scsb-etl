package org.recap.camel;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.jaxb.marc.BibRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Created by premkb on 15/9/16.
 */

public class DataDumpZipFileToFtpRouteBuilderUT extends BaseTestCase {

    @Autowired
    ProducerTemplate producer;

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Value("${ftp.userName}")
    String ftpUserName;

    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    @Value("${ftp.datadump.remote.server}")
    String ftpDataDumpRemoteServer;

    @Test
    public void testZipAndFtp() throws Exception {
        Map<String,String> routeMap = new HashMap<>();
        routeMap.put(ReCAPConstants.CAMELFILENAME,ReCAPConstants.DATA_DUMP_FILE_NAME);
        String requestingInstituionCode = "NYPL";
        routeMap.put(ReCAPConstants.REQUESTING_INST_CODE,requestingInstituionCode);
        BibRecords bibRecords = new BibRecords();
        String dateTimeString = getDateTimeString();
        routeMap.put(ReCAPConstants.DATETIME_FOLDER,dateTimeString);
        producer.sendBodyAndHeader(ReCAPConstants.DATA_DUMP_ZIP_FILE_TO_FTP_Q,bibRecords,"routeMap",routeMap);
        String ftpFileName = ReCAPConstants.DATA_DUMP_FILE_NAME+requestingInstituionCode+"-"+dateTimeString+ReCAPConstants.ZIP_FILE_FORMAT;
        ftpDataDumpRemoteServer = ftpDataDumpRemoteServer+ File.separator+"NYPL"+File.separator+dateTimeString;
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:testZipFtp")
                        .pollEnrich("sftp://" +ftpUserName + "@" + ftpDataDumpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName="+ftpFileName);
            }
        });
        String response = producer.requestBody("seda:testZipFtp", "", String.class);
        Thread.sleep(1000);
        assertNotNull(response);
    }

    private String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);

    }
}
