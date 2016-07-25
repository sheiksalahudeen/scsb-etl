package org.recap.route;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by pvsubrah on 6/24/16.
 */
public class JMSUT extends BaseTestCase {

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    ConsumerTemplate consumer;

    @Autowired
    CamelContext camelContext;

    @Value("${etl.report.directory}")
    private String reportDirectoryPath;

    @Value("${ftp.userName}")
    String ftpUserName;

    @Value("${ftp.remote.server}")
    String ftpRemoteServer;

    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    @Test
    public void produceAndConsumeSEDA() throws Exception {
        assertNotNull(producer);
        assertNotNull(consumer);

        producer.sendBody("seda:start", "Hello World");

        String body = consumer.receiveBody("seda:start", String.class);

        assertEquals("Hello World", body);

    }

    @Test
    public void uploadToFTP() throws Exception {
        camelContext.addRoutes((new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:"+reportDirectoryPath)
                        .choice().when(simple("${in.header.CamelFileName} contains '*.xlsx'"))
                        .to("sftp://" +ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost);
            }
        }));
    }

    @Test
    public void produceAndConsumeEtlReportQ() throws Exception {
        Random random = new Random();

        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new FailureReportReCAPCSVRecord();
        String owningInstitution = String.valueOf(random.nextInt());
        failureReportReCAPCSVRecord.setOwningInstitution(owningInstitution);
        failureReportReCAPCSVRecord.setOwningInstitutionBibId("1111");
        failureReportReCAPCSVRecord.setOwningInstitutionHoldingsId("2222");
        failureReportReCAPCSVRecord.setLocalItemId("333333333");
        failureReportReCAPCSVRecord.setItemBarcode("4444");
        failureReportReCAPCSVRecord.setCustomerCode("PA");
        failureReportReCAPCSVRecord.setTitle("title");
        failureReportReCAPCSVRecord.setCollectionGroupDesignation("open");
        failureReportReCAPCSVRecord.setCreateDateItem(new Date());
        failureReportReCAPCSVRecord.setLastUpdatedDateItem(new Date());
        failureReportReCAPCSVRecord.setExceptionMessage("exception");
        failureReportReCAPCSVRecord.setErrorDescription("error");
        failureReportReCAPCSVRecord.setFileName("recap_records1.xml");

        ReCAPCSVRecord reCAPCSVRecord = new ReCAPCSVRecord();
        reCAPCSVRecord.setFailureReportReCAPCSVRecordList(Arrays.asList(failureReportReCAPCSVRecord));

        producer.sendBody("seda:etlFailureReportQ", reCAPCSVRecord);

        Thread.sleep(1000);

        DateFormat df = new SimpleDateFormat("ddMMMyyyy");
        String fileName = "FailureReport-"+FilenameUtils.removeExtension(failureReportReCAPCSVRecord.getFileName()) + "-" + df.format(new Date());
        assertTrue(new File(reportDirectoryPath+fileName).exists());
    }

   public class FileNameProcessor implements Processor {
       @Override
       public void process(Exchange exchange) throws Exception {
           ReCAPCSVRecord reCAPCSVRecord = (ReCAPCSVRecord) exchange.getIn().getBody();
           String fileName = FilenameUtils.removeExtension(reCAPCSVRecord.getFailureReportReCAPCSVRecordList().get(0).getFileName());
           exchange.getIn().setHeader("reportFileName", fileName);
       }
   }

    @Test
    public void generateSuccessReport() throws Exception {
        SuccessReportReCAPCSVRecord successReportReCAPCSVRecord = new SuccessReportReCAPCSVRecord();
        successReportReCAPCSVRecord.setFileName("scsb-records1.xml");
        successReportReCAPCSVRecord.setTotalRecordsInFile(1000);
        successReportReCAPCSVRecord.setTotalBibsLoaded(900);;
        successReportReCAPCSVRecord.setTotalHoldingsLoaded(800);
        successReportReCAPCSVRecord.setTotalItemsLoaded(1000);
        producer.sendBody("seda:etlSuccessReportQ", successReportReCAPCSVRecord);
        Thread.sleep(1000);
    }
}
