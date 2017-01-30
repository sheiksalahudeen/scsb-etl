package org.recap.service.executor.datadump;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.export.ImprovedFullDataDumpCallable;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.service.DataDumpSolrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by premkb on 27/9/16.
 */
public class FullDataDumpExecutorServiceUT extends BaseTestCase {

    private Logger logger = LoggerFactory.getLogger(FullDataDumpExecutorServiceUT.class);

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    DataDumpSolrService dataDumpSolrService;

    @Autowired
    private FullDataDumpExecutorService fullDataDumpExecutorService;

    ImprovedFullDataDumpCallable improvedFullDataDumpCallable;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Value("${ftp.userName}")
    String ftpUserName;

    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    @Value("${ftp.datadump.remote.server}")
    String ftpDataDumpRemoteServer;

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Value("${datadump.batch.size}")
    private int batchSize;

    @Autowired
    ProducerTemplate producer;

    private String requestingInstitutionCode = "PUL";


    @Test
    public void exportFullDumpForNYPLAndCUL() throws Exception {
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("0");
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("CUL");
        institutionCodes.add("NYPL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("2");
        dataDumpRequest.setOutputFileFormat(ReCAPConstants.XML_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(Arrays.asList("PUL","CUL"));
        searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList("Shared"));
        searchRecordsRequest.setPageSize(10);

        Map results = dataDumpSolrService.getResults(searchRecordsRequest);
        List<LinkedHashMap> dataDumpSearchResults = (List<LinkedHashMap>) results.get("dataDumpSearchResults");

        improvedFullDataDumpCallable = appContext.getBean(ImprovedFullDataDumpCallable.class,dataDumpSearchResults,bibliographicDetailsRepository);
        assertNotNull(improvedFullDataDumpCallable);

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        Future future = executorService.submit(improvedFullDataDumpCallable);

        List<BibliographicEntity> resultFromFuture = (List<BibliographicEntity>) future.get();

        assertNotNull(resultFromFuture);

        for (Iterator<BibliographicEntity> iterator = resultFromFuture.iterator(); iterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = iterator.next();
            assertNotNull(bibliographicEntity);
            assertNotNull(bibliographicEntity.getItemEntities());

        }

        executorService.shutdown();
        Thread.sleep(4000);
    }

    @Test
    public void getFullDumpForMarcXmlFileSystem()throws Exception{
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("0");
        dataDumpRequest.setToEmailAddress("peri.subrahmanya@htcinc.com");
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("CUL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("2");
        dataDumpRequest.setOutputFileFormat(ReCAPConstants.XML_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());
        String response = fullDataDumpExecutorService.process(dataDumpRequest);
        Thread.sleep(1000);
        String day = getDateTimeString();
        File file;
        file = new File(dumpDirectoryPath+File.separator+ requestingInstitutionCode +File.separator+day+ File.separator  + ReCAPConstants.DATA_DUMP_FILE_NAME+ requestingInstitutionCode +"-"+day+ ReCAPConstants.ZIP_FILE_FORMAT);
        boolean fileExists = file.exists();
        assertEquals(response,"There is no data to export.");
    }


    @Test
    public void getFullDumpForScsbXmlFileSystem()throws Exception{
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("0");
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("CUL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("2");
        dataDumpRequest.setOutputFileFormat(ReCAPConstants.XML_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());
        String response = fullDataDumpExecutorService.process(dataDumpRequest);
        Long totalRecordCount = bibliographicDetailsRepository.countRecordsForFullDump(dataDumpRequest.getCollectionGroupIds(),dataDumpRequest.getInstitutionCodes());
        int loopCount = getLoopCount(totalRecordCount,batchSize);
        Thread.sleep(1000);
        String day = getDateTimeString();
        File file;
        logger.info("file count---->"+loopCount);
        file = new File(dumpDirectoryPath+File.separator+ requestingInstitutionCode +File.separator+day+ File.separator  + ReCAPConstants.DATA_DUMP_FILE_NAME+ requestingInstitutionCode +"-"+day+ ReCAPConstants.ZIP_FILE_FORMAT);
        boolean fileExists = file.exists();
        assertEquals(response,"There is no data to export.");
    }

    @Test
    public void getFullDumpForMarcXmlFtp()throws Exception{
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("0");
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("CUL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("0");
        dataDumpRequest.setOutputFileFormat(ReCAPConstants.XML_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());
        String response = fullDataDumpExecutorService.process(dataDumpRequest);
        Long totalRecordCount = bibliographicDetailsRepository.countRecordsForFullDump(dataDumpRequest.getCollectionGroupIds(),dataDumpRequest.getInstitutionCodes());
        int loopCount = getLoopCount(totalRecordCount,batchSize);
        Thread.sleep(1000);
        String dateTimeString = getDateTimeString();
        logger.info("file count---->"+loopCount);
        String ftpFileName = ReCAPConstants.DATA_DUMP_FILE_NAME+requestingInstitutionCode+"-"+dateTimeString+ReCAPConstants.ZIP_FILE_FORMAT;
        ftpDataDumpRemoteServer = ftpDataDumpRemoteServer+ File.separator+requestingInstitutionCode+File.separator+dateTimeString;
        assertNotNull(response);
    }

    private String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);
    }

    private int getLoopCount(Long totalRecordCount,int batchSize){
        int quotient = Integer.valueOf(Long.toString(totalRecordCount)) / (batchSize);
        int remainder = Integer.valueOf(Long.toString(totalRecordCount)) % (batchSize);
        int loopCount = remainder == 0 ? quotient : quotient + 1;
        return loopCount;
    }
}
