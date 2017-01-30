package org.recap.camel.datadump;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertNotNull;

/**
 * Created by premkb on 15/9/16.
 */

public class DataDumpZipFileFtpRouteBuilderUT extends BaseTestCase {

    @Autowired
    ProducerTemplate producer;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    HoldingsDetailsRepository holdingsDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

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

    @Ignore
    @Test
    public void testZipAndFtp() throws Exception {
        saveBibSingleHoldingsSingleItem();
        Map<String,String> routeMap = new HashMap<>();
        String requestingInstituionCode = "NYPL";
        routeMap.put(ReCAPConstants.FILENAME,ReCAPConstants.DATA_DUMP_FILE_NAME+requestingInstituionCode);
        routeMap.put(ReCAPConstants.REQUESTING_INST_CODE,requestingInstituionCode);
        BibRecords bibRecords = new BibRecords();
        String dateTimeString = getDateTimeString();
        routeMap.put(ReCAPConstants.DATETIME_FOLDER,dateTimeString);
        producer.sendBodyAndHeader(ReCAPConstants.DATADUMP_ZIPFILE_FTP_Q,bibRecords,"routeMap",routeMap);
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

    public void saveBibSingleHoldingsSingleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(1, String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = getHoldingsEntity(random, 1);

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("etl");
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setLastUpdatedBy("etl");
        itemEntity.setBarcode("123");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("1");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);

    }

    private HoldingsEntity getHoldingsEntity(Random random, Integer institutionId) {
        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionId(institutionId);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));
        return holdingsEntity;
    }

    private BibliographicEntity getBibliographicEntity(Integer institutionId, String owningInstitutionBibId1) {
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content".getBytes());
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setCreatedBy("etl");
        bibliographicEntity1.setLastUpdatedBy("etl");
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(institutionId);
        bibliographicEntity1.setOwningInstitutionBibId(owningInstitutionBibId1);
        return bibliographicEntity1;
    }
}
