package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by angelind on 21/7/16.
 */
public class FTPRouteBuilderUT extends BaseTestCase{

    @Value("${ftp.userName}")
    String ftpUserName;

    @Value("${ftp.remote.server}")
    String ftpRemoteServer;

    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    @Value("${etl.report.directory}")
    private String reportDirectoryPath;

    @Autowired
    ProducerTemplate producer;

    @Test
    public void uploadFileToFTP() throws Exception {

        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new FailureReportReCAPCSVRecord();
        failureReportReCAPCSVRecord.setOwningInstitution("PUL");
        failureReportReCAPCSVRecord.setOwningInstitutionBibId("1111");
        failureReportReCAPCSVRecord.setOwningInstitutionHoldingsId("2222");
        failureReportReCAPCSVRecord.setLocalItemId("333333333");
        failureReportReCAPCSVRecord.setItemBarcode("4444");
        failureReportReCAPCSVRecord.setCustomerCode("PA");
        failureReportReCAPCSVRecord.setTitle("History, of Science");
        failureReportReCAPCSVRecord.setCollectionGroupDesignation("open");
        failureReportReCAPCSVRecord.setCreateDateItem(new SimpleDateFormat("mm-dd-yyyy").format(new Date()));
        failureReportReCAPCSVRecord.setLastUpdatedDateItem(new SimpleDateFormat("mm-dd-yyyy").format(new Date()));
        failureReportReCAPCSVRecord.setExceptionMessage("exception");
        failureReportReCAPCSVRecord.setErrorDescription("error");

        ReCAPCSVRecord reCAPCSVRecord = new ReCAPCSVRecord();
        reCAPCSVRecord.setFileName("test.xml");
        reCAPCSVRecord.setInstitutionName("PUL");
        reCAPCSVRecord.setReportType("failure");
        assertNotNull(failureReportReCAPCSVRecord.getCreateDateItem());
        assertNotNull(failureReportReCAPCSVRecord.getLastUpdatedDateItem());
        reCAPCSVRecord.setFailureReportReCAPCSVRecordList(Arrays.asList(failureReportReCAPCSVRecord));

        producer.sendBody("seda:ftpQForCSV", reCAPCSVRecord);

        Thread.sleep(2000);
    }
}