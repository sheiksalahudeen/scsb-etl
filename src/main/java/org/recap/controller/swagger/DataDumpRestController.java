package org.recap.controller.swagger;

import io.swagger.annotations.*;
import org.recap.ReCAPConstants;
import org.recap.executors.ExportDataDumpExecutorService;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by premkb on 19/8/16.
 */

@RestController
@RequestMapping("/dataDump")
@Api(value="dataDump", description="Export data dump", position = 1)
public class DataDumpRestController {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpRestController.class);

    @Autowired
    ApplicationContext appContext;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Value("${datadump.threads}")
    private int noOfThreads;

    @Value("${datadump.batchsize}")
    private int batchSize;

    @RequestMapping(value="/exportDataDump", method = RequestMethod.GET)
    @ApiOperation(value = "exportDataDump",
            notes = "Export datadumps to institutions", nickname = "exportDataDump", position = 0)
    @ApiResponses(value = {@ApiResponse(code = 200, message = ReCAPConstants.DATADUMP_PROCESS_STARTED)})
    @ResponseBody
    public ResponseEntity exportDataDump(@ApiParam(value = "Code of institutions whose shared collection updates are requested. Use PUL for Princeton, CUL for Columbia and NYPL for NYPL." , required = true, name = "institutionCodes") @RequestParam String institutionCodes,
                                         @ApiParam(value = "Code of insitituion who is requesting. Use PUL for Princeton, CUL for Columbia and NYPL for NYPL. ",required=true, name = "requestingInstitutionCode") @RequestParam String requestingInstitutionCode,
                                         @ApiParam(value = "Type of export - Full (use 0) or Incremental (use 1)" , required = true , name = "fetchType") @RequestParam Integer fetchType,
                                         @ApiParam(value = "Get updates to middleware collection since the date provided. Default will be updates since the previous day. Date format will be a string (yyyy-MM-dd HH:mm)", name = "date") @RequestParam(required=false) String date,
                                         @ApiParam(value = "Collection group id will get the relevant info based on the id provided. Default will get both shared and open information - Shared (use 1), Open (use 2), Both (use 1,2)", name = "collectionGroupIds") @RequestParam(required=false) String collectionGroupIds,
                                         @ApiParam(value = "Type of transmission - FTP (use 0), HTTP Response (use 1) this parameter is not considered for full dump, File system (use 2). Default will be ftp ", name = "transmissionType")@RequestParam(required=false) Integer transmissionType,
                                         @ApiParam(value = "No. of records to be exported per file. Default will be 10000 records per file", name = "noOfRecordsPerFile")@RequestParam(required=false) String noOfRecordsPerFile
                                         ){
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        setDataDumpRequest(dataDumpRequest,fetchType,institutionCodes,date,collectionGroupIds,transmissionType,requestingInstitutionCode,noOfRecordsPerFile);

        ResponseEntity responseEntity = validateIncomingRequest(dataDumpRequest);
        if(responseEntity!=null) {
            return responseEntity;
        }

        responseEntity = startExportDumpProcess(dataDumpRequest,responseEntity);
        return responseEntity;
    }

    private ResponseEntity startExportDumpProcess(DataDumpRequest dataDumpRequest, ResponseEntity responseEntity){
        HttpHeaders responseHeaders = new HttpHeaders();
        ExportDataDumpExecutorService exportDataDumpExecutorService = appContext.getBean(ExportDataDumpExecutorService.class);
        String outputString = null;
        try {
            if ((dataDumpRequest.getTransmissionType() != ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP)) {
                new Thread(() -> {
                    try {
                        exportDataDumpExecutorService.exportDump(dataDumpRequest);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else if (dataDumpRequest.getTransmissionType() == ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP) {
                outputString = exportDataDumpExecutorService.exportDump(dataDumpRequest);
            }
            responseEntity = getResponseEntity(outputString,dataDumpRequest);
        }catch (Exception e) {
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

    private void setDataDumpRequest(DataDumpRequest dataDumpRequest, Integer fetchType, String institutionCodes, String date, String collectionGroupIds,
                                    Integer transmissionType, String requestingInstitutionCode, String noOfRecordsPerFile){
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
    }

    private  ResponseEntity validateIncomingRequest(DataDumpRequest dataDumpRequest){
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
        if (dataDumpRequest.getFetchType()!= ReCAPConstants.DATADUMP_FETCHTYPE_FULL &&
                dataDumpRequest.getFetchType()!= ReCAPConstants.DATADUMP_FETCHTYPE_INCREMENTAL){
            erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_VALID_FETCHTYPE_ERR_MSG);
            errorcount++;
        }
        if (dataDumpRequest.getFetchType() == ReCAPConstants.DATADUMP_FETCHTYPE_FULL ) {
            if (dataDumpRequest.getInstitutionCodes() == null) {
                erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_INSTITUTIONCODE_ERR_MSG);
                errorcount++;
            }
        }
        if (dataDumpRequest.getFetchType() == ReCAPConstants.DATADUMP_FETCHTYPE_INCREMENTAL) {
            if (dataDumpRequest.getDate() == null) {
                erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_DATE_ERR_MSG);
                errorcount++;
            }
            if(dataDumpRequest.getTransmissionType() != ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP
                    && dataDumpRequest.getTransmissionType() != ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP
                    && dataDumpRequest.getTransmissionType() != ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM
                    ){
                erroMessageMap.put(errorcount, ReCAPConstants.DATADUMP_TRANS_TYPE_ERR_MSG);
                errorcount++;
            }
        }
        if(erroMessageMap.size()>0){
            String date= new Date().toString();
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            responseEntity = new ResponseEntity(buildErrorMessage(erroMessageMap),responseHeaders,HttpStatus.BAD_REQUEST);
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
        if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)
                ||dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM)){
            String date= new Date().toString();
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(ReCAPConstants.DATADUMP_PROCESS_STARTED,responseHeaders,HttpStatus.OK);
        }else if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP) && outputString != null){
            String date= new Date().toString();
            responseHeaders.add("responseMessage", ReCAPConstants.DATADUMP_EXPORT_SUCCESS);
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(outputString,responseHeaders, HttpStatus.OK);
        }else if(dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP) && !dataDumpRequest.isRecordsAvailable()){
            String date= new Date().toString();
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(ReCAPConstants.DATADUMP_NO_RECORD,responseHeaders, HttpStatus.OK);
        }else{
            String date= new Date().toString();
            responseHeaders.add(ReCAPConstants.RESPONSE_DATE , date);
            return new ResponseEntity(ReCAPConstants.DATADUMP_EXPORT_FAILURE,responseHeaders, HttpStatus.BAD_REQUEST);
        }
    }
}
