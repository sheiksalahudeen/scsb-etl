package org.recap.route;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.etl.LoadReportEntity;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by pvsubrah on 6/24/16.
 */
public class JMSTest extends BaseTestCase {

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
    public void uploadToFTP() throws Exception {
        assertNotNull(producer);
        assertNotNull(consumer);

        producer.sendBody("activemq:queue:ftpQ", "Hello World");

        String body = consumer.receiveBody("seda:start", String.class);

        assertEquals("Hello World", body);

    }

    @Test
    public void csv() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:start").process(new FileNameProcessor()).marshal().bindy(BindyType.Csv, ReCAPCSVRecord.class).to("file:"+reportDirectoryPath+"?fileName=${in.header.reportFileName}-${date:now:ddMMMyyyy}&fileExist=append");
            }
        });

        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new FailureReportReCAPCSVRecord();
        failureReportReCAPCSVRecord.setTitle("History of Science");
        failureReportReCAPCSVRecord.setOwningInstitutionHoldingsId("1231");
        failureReportReCAPCSVRecord.setOwningInstitution("PUL");
        failureReportReCAPCSVRecord.setOwningInstitutionBibId("123");
        failureReportReCAPCSVRecord.setCollectionGroupDesignation("Open");
        failureReportReCAPCSVRecord.setCreateDateItem(new Date());
        failureReportReCAPCSVRecord.setExceptionMessage("Holdings Id is null");
        failureReportReCAPCSVRecord.setFileName("recap_records2.xml");


        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord1 = new FailureReportReCAPCSVRecord();
        failureReportReCAPCSVRecord1.setTitle("History of Science");
        failureReportReCAPCSVRecord1.setOwningInstitutionHoldingsId("1231");
        failureReportReCAPCSVRecord1.setOwningInstitution("PUL");
        failureReportReCAPCSVRecord1.setOwningInstitutionBibId("123");
        failureReportReCAPCSVRecord1.setCollectionGroupDesignation("Open");
        failureReportReCAPCSVRecord1.setCreateDateItem(new Date());
        failureReportReCAPCSVRecord1.setExceptionMessage("Item Id is null");
        failureReportReCAPCSVRecord1.setFileName("recap_records2.xml");


        ReCAPCSVRecord reCAPCSVRecord = new ReCAPCSVRecord();
        reCAPCSVRecord.setFailureReportReCAPCSVRecordList(Arrays.asList(failureReportReCAPCSVRecord, failureReportReCAPCSVRecord1));

        producer.sendBody("seda:start", reCAPCSVRecord);

        Thread.sleep(3000);

    }

    @Test
    public void produceAndConsumeEtlLoadQ() throws Exception {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
            bibliographicEntity1.setBibliographicId(random.nextInt());
            bibliographicEntity1.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
            bibliographicEntity1.setOwningInstitutionId(1);
            bibliographicEntity1.setContent("mock Content".getBytes());
            bibliographicEntity1.setCreatedDate(new Date());
            bibliographicEntity1.setCreatedBy("etl-test");
            bibliographicEntity1.setLastUpdatedBy("etl-test");
            bibliographicEntity1.setLastUpdatedDate(new Date());


            HoldingsEntity holdingsEntity = new HoldingsEntity();
            holdingsEntity.setContent("mock holding content".getBytes());
            holdingsEntity.setCreatedDate(new Date());
            holdingsEntity.setCreatedBy("etl-test");
            holdingsEntity.setLastUpdatedDate(new Date());
            holdingsEntity.setLastUpdatedBy("etl-test");

            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setBarcode("b.123");
            itemEntity.setCollectionGroupId(1);
            itemEntity.setCreatedDate(new Date());
            itemEntity.setCreatedBy("etl-test");
            itemEntity.setLastUpdatedBy("etl-test");
            itemEntity.setLastUpdatedDate(new Date());
            itemEntity.setOwningInstitutionId(1);
            itemEntity.setCustomerCode("PA");
            itemEntity.setOwningInstitutionItemId("123");
            itemEntity.setItemAvailabilityStatusId(1);
            itemEntity.setHoldingsEntity(holdingsEntity);

            holdingsEntity.setItemEntities(Arrays.asList(itemEntity));

            bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));
            bibliographicEntity1.setItemEntities(Arrays.asList(itemEntity));

            ETLExchange etlExchange = new ETLExchange();
            etlExchange.setBibliographicEntities(Arrays.asList(bibliographicEntity1));
            etlExchange.setInstitutionEntityMap(new HashMap());
            etlExchange.setCollectionGroupMap(new HashMap());

            producer.sendBody("activemq:queue:etlLoadQ", etlExchange);
        }

        Thread.sleep(5000);
    }

    @Test
    public void produceAndConsumeEtlReportQ() throws Exception {
        Random random = new Random();

        LoadReportEntity loadReportEntity = new LoadReportEntity();
        String owningInstitution = String.valueOf(random.nextInt());
        loadReportEntity.setOwningInstitution(owningInstitution);
        loadReportEntity.setOwningInstitutionBibId("1111");
        loadReportEntity.setOwningInstitutionHoldingsId("2222");
        loadReportEntity.setLocalItemId("333333333");
        loadReportEntity.setItemBarcode("4444");
        loadReportEntity.setCustomerCode("PA");
        loadReportEntity.setTitle("title");
        loadReportEntity.setCollectionGroupDesignation("open");
        loadReportEntity.setCreateDateItem(new Date());
        loadReportEntity.setLastUpdatedDateItem(new Date());
        loadReportEntity.setExceptionMessage("exception");
        loadReportEntity.setErrorDescription("error");

        producer.sendBody("activemq:queue:etlReportQ", Arrays.asList(loadReportEntity));

        Thread.sleep(1000);

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String fileName = reportDirectoryPath + File.separator + owningInstitution + "_" + df.format(new Date()) + ".csv";
        assertTrue(new File(fileName).exists());
    }

   public class FileNameProcessor implements Processor {
       @Override
       public void process(Exchange exchange) throws Exception {
           ReCAPCSVRecord reCAPCSVRecord = (ReCAPCSVRecord) exchange.getIn().getBody();
           String fileName = FilenameUtils.removeExtension(reCAPCSVRecord.getFailureReportReCAPCSVRecordList().get(0).getFileName());
           exchange.getIn().setHeader("reportFileName", fileName);
       }
   }
}
