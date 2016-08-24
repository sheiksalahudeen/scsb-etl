package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.executors.ExportDataDumpExecutorService;
import org.recap.model.export.DataDumpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by premkb on 19/8/16.
 */
@RestController
public class DataDumpRestController {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpRestController.class);

    @Autowired
    private ExportDataDumpExecutorService exportDataDumpExecutorService;

    @Value("${datadump.threads}")
    private int noOfThreads;

    @Value("${datadump.batchsize}")
    private int batchSize;

    @RequestMapping(value="/exportDataDump", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity exportDataDump(@RequestParam(value="institutionCodes",required=false) String institutionCodes,
                                         @RequestParam(value="fetchType") Integer fetchType,
                                         @RequestParam(value="date",required=false) String date,
                                         @RequestParam(value="requestingInstitutionCode",required=false) String requestingInstitutionCode){
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType(fetchType);
        if(fetchType==1){
            if(institutionCodes == null && date == null){
                return new ResponseEntity("Either one of the parameter institutioncode or date is required", HttpStatus.BAD_REQUEST);
            }else{
                setDataDumpRequest(dataDumpRequest,institutionCodes,date);
            }
        }

        boolean successFlag = true;
        try {
            successFlag = exportDataDumpExecutorService.exportDump(dataDumpRequest);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            return new ResponseEntity("Data dump export failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (successFlag) {
            return new ResponseEntity("Data dump exported successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity("Data dump export failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List getInstitutionCode(String institutionCodes){
        String[] institutionArray = institutionCodes.split(",");
        List<String> intitionCodesList = Arrays.asList(institutionArray);
        return intitionCodesList;
    }

    private void setDataDumpRequest(DataDumpRequest dataDumpRequest,String institutionCodes,String date){
        if(institutionCodes!=null){
            List<String> institutionCodeList = getInstitutionCode(institutionCodes);
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
    }
}
