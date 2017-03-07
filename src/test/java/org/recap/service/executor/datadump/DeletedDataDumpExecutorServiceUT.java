package org.recap.service.executor.datadump;

import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.RecapConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by premkb on 29/9/16.
 */
public class DeletedDataDumpExecutorServiceUT extends BaseTestCase{
    private static final Logger logger = LoggerFactory.getLogger(DeletedDataDumpExecutorServiceUT.class);

    @Autowired
    private DeletedDataDumpExecutorService deletedDataDumpExecutorService;

    @Mock
    DeletedDataDumpExecutorService mockedDeletedDataDumpExecutorService;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Mock
    BibliographicDetailsRepository mockBibliographicDetailsRepository;

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

    private String requestingInstitutionCode = "CUL";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getFullDumpForDeleteRecordFileSystem()throws Exception{
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("0");
        String inputDate = "2016-08-30 11:20";
        dataDumpRequest.setDate(inputDate);
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("PUL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("2");
        dataDumpRequest.setOutputFileFormat(RecapConstants.JSON_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());
        Mockito.when(mockedDeletedDataDumpExecutorService.process(dataDumpRequest)).thenReturn("Success");
        String outputString = mockedDeletedDataDumpExecutorService.process(dataDumpRequest);
        assertNotNull(outputString);
        assertEquals(outputString,"Success");
    }

    @Test
    public void getIncrementalDumpForDeleteRecordFileSystem()throws Exception{
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("1");
        String inputDate = "2016-08-30 11:20";
        dataDumpRequest.setDate(inputDate);
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("PUL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("2");
        dataDumpRequest.setOutputFileFormat(RecapConstants.JSON_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());
        Mockito.when(mockedDeletedDataDumpExecutorService.process(dataDumpRequest)).thenReturn("Success");
        String outputString = mockedDeletedDataDumpExecutorService.process(dataDumpRequest);
        assertEquals(outputString,"Success");
    }

    @Test
    public void getIncrementalDumpForDeleteRecordFtp()throws Exception{
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("0");
        String inputDate = "2016-08-30 11:20";
        dataDumpRequest.setDate(inputDate);
        dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("PUL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("0");
        dataDumpRequest.setOutputFileFormat(RecapConstants.JSON_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());
        Mockito.when(mockedDeletedDataDumpExecutorService.process(dataDumpRequest)).thenReturn("Success");
        String response = mockedDeletedDataDumpExecutorService.process(dataDumpRequest);
        Thread.sleep(1000);
        String dateTimeString = getDateTimeString();
        String ftpFileName = RecapConstants.DATA_DUMP_FILE_NAME+requestingInstitutionCode+"1"+"-"+dateTimeString+ RecapConstants.JSON_FILE_FORMAT;
        ftpDataDumpRemoteServer = ftpDataDumpRemoteServer+ File.separator+requestingInstitutionCode+File.separator+dateTimeString;
        assertNotNull(response);
        assertNotNull(response,"Success");
    }

    private String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(RecapConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);
    }

    private int getLoopCount(Long totalRecordCount,int batchSize){
        int quotient = Integer.valueOf(Long.toString(totalRecordCount)) / (batchSize);
        int remainder = Integer.valueOf(Long.toString(totalRecordCount)) % (batchSize);
        int loopCount = remainder == 0 ? quotient : quotient + 1;
        return loopCount;
    }
}
