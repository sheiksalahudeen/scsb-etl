package org.recap.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by premkb on 19/8/16.
 */
@EnableSwagger2
@RestController
@RequestMapping("/dataDump")
@Api(value="dataDump", description="Export data dump", position = 1)
public class DataDumpRestController {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpRestController.class);

    @Autowired
    private ExportDataDumpExecutorService exportDataDumpExecutorService;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Value("${datadump.threads}")
    private int noOfThreads;

    @Value("${datadump.batchsize}")
    private int batchSize;

    @RequestMapping(value="/exportDataDump", method = RequestMethod.GET)
    @ApiOperation(value = "exportDataDump",
            notes = "Export datadumps to institutions", nickname = "exportDataDump", position = 0)
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input") })
    @ResponseBody
    public ResponseEntity exportDataDump(@ApiParam(value = "Code of institutions whose shared collection updates are requested. Use PUL for Princeton, CUL for Columbia and NYPL for NYPL." , required = true, name = "institutionCodes") @RequestParam String institutionCodes,
                                         @ApiParam(value = "Type of export - Full (use 0) or Incremental (use 1)" , required = true , name = "fetchType") @RequestParam Integer fetchType,
                                         @ApiParam(value = "Get updates to middleware collection since the date provided. Default will be updates since the previous day. Date format will be a string (mm-dd-yyyy)", name = "date") @RequestParam(required=false) String date,
                                         @ApiParam(value = "Collection group id will get the relevant info based on the id provided. Default will get both shared and open information - Shared (use 1), Open (use 2), Both (use 1,2)", name = "collectionGroupIds") @RequestParam(required=false) String collectionGroupIds,
                                         @RequestParam(value="requestingInstitutionCode",required=false) String requestingInstitutionCode){
        if(fetchType == 0){
            if(institutionCodes == null){
                return new ResponseEntity(ReCAPConstants.DATADUMP_INSTITUTIONCODE_ERR_MSG, HttpStatus.BAD_REQUEST);
            }
        }else if(fetchType == 1){
            if(institutionCodes == null || date == null){
                return new ResponseEntity(ReCAPConstants.DATADUMP_INSTITUTIONCODE_DATE_ERR_MSG, HttpStatus.BAD_REQUEST);
            }
        }else{
            return new ResponseEntity(ReCAPConstants.DATADUMP_VALID_FETCHTYPE_ERR_MSG, HttpStatus.BAD_REQUEST);
        }
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        setDataDumpRequest(dataDumpRequest,fetchType,institutionCodes,date,collectionGroupIds);

        boolean successFlag = true;
        try {
            successFlag = exportDataDumpExecutorService.exportDump(dataDumpRequest);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            return new ResponseEntity(ReCAPConstants.DATADUMP_EXPORT_FAILURE, HttpStatus.BAD_REQUEST);
        }
        if (successFlag && dataDumpRequest.isRecordsAvailable()) {
            return new ResponseEntity(ReCAPConstants.DATADUMP_EXPORT_SUCCESS, HttpStatus.OK);
        }else if(successFlag && !dataDumpRequest.isRecordsAvailable()){
            return new ResponseEntity(ReCAPConstants.DATADUMP_NO_RECORD, HttpStatus.OK);
        }else {
            return new ResponseEntity(ReCAPConstants.DATADUMP_EXPORT_FAILURE, HttpStatus.BAD_REQUEST);
        }
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

    private void setDataDumpRequest(DataDumpRequest dataDumpRequest, int fetchType, String institutionCodes, String date, String collectionGroupIds){
        dataDumpRequest.setFetchType(fetchType);
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
        String batchSizeString = System.getProperty(ReCAPConstants.DATADUMP_BATCHSIZE);
        if(batchSizeString!=null){
            batchSize = Integer.parseInt(batchSizeString);
        }
        dataDumpRequest.setBatchSize(batchSize);
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

    }
}
