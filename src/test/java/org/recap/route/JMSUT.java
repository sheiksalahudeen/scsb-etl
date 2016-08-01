package org.recap.route;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.camel.*;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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

    @Test
    public void produceAndConsumeSEDA() throws Exception {
        assertNotNull(producer);
        assertNotNull(consumer);

        producer.sendBody("seda:start", "Hello World");

        String body = consumer.receiveBody("seda:start", String.class);

        assertEquals("Hello World", body);

    }

    @Test
    public void generateFailureReport() throws Exception {
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new FailureReportReCAPCSVRecord();
        failureReportReCAPCSVRecord.setOwningInstitution("PUL");
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
        failureReportReCAPCSVRecord.setFileName("testReport1.xml");

        ReCAPCSVRecord reCAPCSVRecord = new ReCAPCSVRecord();
        assertNotNull(failureReportReCAPCSVRecord.getCreateDateItem());
        assertNotNull(failureReportReCAPCSVRecord.getLastUpdatedDateItem());
        reCAPCSVRecord.setFailureReportReCAPCSVRecordList(Arrays.asList(failureReportReCAPCSVRecord));

        producer.sendBody("seda:etlFailureReportQ", reCAPCSVRecord);

        Thread.sleep(1000);

        DateFormat df = new SimpleDateFormat("ddMMMyyyy");
        String fileName = FilenameUtils.removeExtension(failureReportReCAPCSVRecord.getFileName()) + "-Failure-" + df.format(new Date());
        File file = new File(reportDirectoryPath + File.separator + fileName + ".csv");
        assertTrue(file.exists());
        String fileContents = Files.toString(file, Charsets.UTF_8);
        assertNotNull(fileContents);
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getOwningInstitution()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getOwningInstitutionBibId()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getOwningInstitutionHoldingsId()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getLocalItemId()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getItemBarcode()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getCustomerCode()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getTitle()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getCollectionGroupDesignation()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getExceptionMessage()));
        assertTrue(fileContents.contains(failureReportReCAPCSVRecord.getErrorDescription()));
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
        successReportReCAPCSVRecord.setFileName("test,Report.xml");
        successReportReCAPCSVRecord.setTotalRecordsInFile(1000);
        successReportReCAPCSVRecord.setTotalBibsLoaded(900);;
        successReportReCAPCSVRecord.setTotalHoldingsLoaded(800);
        successReportReCAPCSVRecord.setTotalItemsLoaded(1000);
        successReportReCAPCSVRecord.setTotalBibHoldingsLoaded(900);
        successReportReCAPCSVRecord.setTotalBibItemsLoaded(1100);
        successReportReCAPCSVRecord.setOwningInstitution("PUL");
        producer.sendBody("seda:etlSuccessReportQ", successReportReCAPCSVRecord);
        Thread.sleep(1000);

        DateFormat df = new SimpleDateFormat("ddMMMyyyy");
        String fileName = FilenameUtils.removeExtension(successReportReCAPCSVRecord.getFileName()) + "-Success-" + df.format(new Date());
        File file = new File(reportDirectoryPath + File.separator + fileName + ".csv");
        assertTrue(file.exists());
        String fileContents = Files.toString(file, Charsets.UTF_8);
        assertNotNull(fileContents);
        assertTrue(fileContents.contains(successReportReCAPCSVRecord.getTotalBibsLoaded().toString()));
        assertTrue(fileContents.contains(successReportReCAPCSVRecord.getTotalHoldingsLoaded().toString()));
        assertTrue(fileContents.contains(successReportReCAPCSVRecord.getTotalRecordsInFile().toString()));
        assertTrue(fileContents.contains(successReportReCAPCSVRecord.getTotalItemsLoaded().toString()));
        assertTrue(fileContents.contains(String.valueOf(successReportReCAPCSVRecord.getTotalBibItemsLoaded())));
        assertTrue(fileContents.contains(String.valueOf(successReportReCAPCSVRecord.getTotalBibHoldingsLoaded())));
    }
}
