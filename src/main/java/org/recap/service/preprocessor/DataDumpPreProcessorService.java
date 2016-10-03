package org.recap.service.preprocessor;

import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.service.executor.datadump.DataDumpExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by premkb on 27/9/16.
 */
@Service
public class DataDumpPreProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpPreProcessorService.class);

    @Autowired
    ApplicationContext appContext;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private DataDumpExecutorService dataDumpExecutorService;

    @Value("${datadump.threads}")
    private int noOfThreads;

    @Value("${datadump.batchsize}")
    private int batchSize;

    public ResponseEntity startDataDumpProcess(DataDumpRequest dataDumpRequest){
        ResponseEntity responseEntity;
        HttpHeaders responseHeaders = new HttpHeaders();
        String outputString = null;
        try {
            if ((!dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP))) {
                new Thread(() -> {
                    try {
                        dataDumpExecutorService.generateDataDump(dataDumpRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }else{
                outputString = dataDumpExecutorService.generateDataDump(dataDumpRequest);
            }
            responseEntity = getResponseEntity(outputString,dataDumpRequest);
        } catch (Exception e) {
            logger.error(e.getMessage());
            String date= new Date().toString();
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            responseEntity = new ResponseEntity(ReCAPConstants.DATADUMP_EXPORT_FAILURE,responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    private List<String> splitStringAndGetList(String inputString){
        String[] splittedString = inputString.split(",");
        List<String> stringList = Arrays.asList(splittedString);
        return stringList;
    }

    private List<Integer> getIntegerListFromStringList(List<String> stringList){
        List<Integer> integerList = new ArrayList<>();
        for(String stringValue : stringList){
            integerList.add(Integer.parseInt(stringValue));
        }
        return integerList;
    }

    private List<Integer> splitStringAndGetIntegerList(String inputString){
        List<Integer> integerList = getIntegerListFromStringList(splitStringAndGetList(inputString));
        return integerList;
    }

    public void setDataDumpRequest(DataDumpRequest dataDumpRequest, String fetchType, String institutionCodes, String date, String collectionGroupIds,
                                    String transmissionType, String requestingInstitutionCode, String noOfRecordsPerFile, String outputFormat){
        if (fetchType != null) {
            dataDumpRequest.setFetchType(fetchType);
        }
        if(institutionCodes!=null){
            List<String> institutionCodeList = splitStringAndGetList(institutionCodes);
            dataDumpRequest.setInstitutionCodes(institutionCodeList);
        }
        if(date != null) {
            dataDumpRequest.setDate(date);
        }
        String noOfThreadString = System.getProperty(ReCAPConstants.DATADUMP_THREADS);
        if(noOfThreadString!=null){
            noOfThreads = Integer.parseInt(noOfThreadString);
        }
        dataDumpRequest.setNoOfThreads(noOfThreads);
        if(noOfRecordsPerFile!=null){
            dataDumpRequest.setBatchSize(Integer.parseInt(noOfRecordsPerFile));
        }else{
            dataDumpRequest.setBatchSize(batchSize);
        }
        if(collectionGroupIds != null){
            List<Integer> collectionGroupIdList = splitStringAndGetIntegerList(collectionGroupIds);
            dataDumpRequest.setCollectionGroupIds(collectionGroupIdList);
        }else {
            List<Integer> collectionGroupIdList = new ArrayList<>();
            CollectionGroupEntity collectionGroupEntityShared = collectionGroupDetailsRepository.findByCollectionGroupCode(ReCAPConstants.COLLECTION_GROUP_SHARED);
            collectionGroupIdList.add(collectionGroupEntityShared.getCollectionGroupId());
            CollectionGroupEntity collectionGroupEntityOpen = collectionGroupDetailsRepository.findByCollectionGroupCode(ReCAPConstants.COLLECTION_GROUP_OPEN);
            collectionGroupIdList.add(collectionGroupEntityOpen.getCollectionGroupId());
            dataDumpRequest.setCollectionGroupIds(collectionGroupIdList);
        }
        if(transmissionType != null){
            dataDumpRequest.setTransmissionType(transmissionType);
        }else{
            dataDumpRequest.setTransmissionType(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP);
        }
        if(requestingInstitutionCode != null){
            dataDumpRequest.setRequestingInstitutionCode(requestingInstitutionCode);
        }
        if(outputFormat !=null){
            if(fetchType.equals(ReCAPConstants.DATADUMP_FETCHTYPE_DELETED)){
                dataDumpRequest.setOutputFormat(ReCAPConstants.DATADUMP_DELETED_JSON_FORMAT);
                dataDumpRequest.setFileFormat(ReCAPConstants.JSON_FILE_FORMAT);
            }else{
                dataDumpRequest.setOutputFormat(outputFormat);
                dataDumpRequest.setFileFormat(ReCAPConstants.XML_FILE_FORMAT);
            }
        }
        dataDumpRequest.setDateTimeString(getDateTimeString());
    }

    public ResponseEntity validateIncomingRequest(DataDumpRequest dataDumpRequest){
        ResponseEntity responseEntity = null;
        HttpHeaders responseHeaders = new HttpHeaders();
        Map<Integer,String> erroMessageMap = new HashMap<>();
        Integer errorcount = 1;
        if(dataDumpRequest.getInstitutionCodes().size()>0){
            for(String institutionCode : dataDumpRequest.getInstitutionCodes()){
                if(!institutionCode.equals(ReCAPConstants.COLUMBIA) && !institutionCode.equals(ReCAPConstants.PRINCETON)
                        && !institutionCode.equals(ReCAPConstants.NYPL)){
                    erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_VALID_INST_CODES_ERR_MSG);
                    errorcount++;
                }
            }
        }
        if(dataDumpRequest.getRequestingInstitutionCode() != null){
            if(!dataDumpRequest.getRequestingInstitutionCode().equals(ReCAPConstants.COLUMBIA) && !dataDumpRequest.getRequestingInstitutionCode().equals(ReCAPConstants.PRINCETON)
                    && !dataDumpRequest.getRequestingInstitutionCode().equals(ReCAPConstants.NYPL)){
                erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_VALID_REQ_INST_CODE_ERR_MSG);
                errorcount++;
            }
        }
        if (!dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_FULL) &&
                !dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_INCREMENTAL)
                && !dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_DELETED)){
            erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_VALID_FETCHTYPE_ERR_MSG);
            errorcount++;
        }
        if (dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_FULL) ) {
            if (dataDumpRequest.getInstitutionCodes() == null) {
                erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_INSTITUTIONCODE_ERR_MSG);
                errorcount++;
            }
        }
        if (dataDumpRequest.getFetchType().equals(ReCAPConstants.DATADUMP_FETCHTYPE_INCREMENTAL)) {
            if (dataDumpRequest.getDate() == null) {
                erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_DATE_ERR_MSG);
                errorcount++;
            }
            if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)
                    && dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP)
                    && dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM)
                    ){
                erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_TRANS_TYPE_ERR_MSG);
                errorcount++;
            }
        }
        if(erroMessageMap.size()>0){
            String date= new Date().toString();
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            responseEntity = new ResponseEntity(buildErrorMessage(erroMessageMap),responseHeaders, HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    private String buildErrorMessage(Map<Integer,String> erroMessageMap){
        StringBuilder errorMessageBuilder = new StringBuilder();
        erroMessageMap.entrySet().forEach(entry -> {
            errorMessageBuilder.append(entry.getKey()).append(". ").append(entry.getValue()).append("\n");
        });
        return errorMessageBuilder.toString();
    }

    private ResponseEntity getResponseEntity(String outputString, DataDumpRequest dataDumpRequest) throws Exception{
        HttpHeaders responseHeaders = new HttpHeaders();
        String date= new Date().toString();
        if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)
                ||dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM)){
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(ReCAPConstants.DATADUMP_PROCESS_STARTED,responseHeaders,HttpStatus.OK);
        }else if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP) && outputString != null){
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(outputString,responseHeaders, HttpStatus.OK);
        }else if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP) && !dataDumpRequest.isRecordsAvailable()){
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(ReCAPConstants.DATADUMP_NO_RECORD,responseHeaders, HttpStatus.OK);
        }else{
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(ReCAPConstants.DATADUMP_EXPORT_FAILURE,responseHeaders, HttpStatus.BAD_REQUEST);
        }
    }

    private String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);
    }

}
