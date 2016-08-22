package org.recap.controller;

import org.recap.executors.ExportDataDumpExecutorService;
import org.recap.model.export.DataDumpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value="/exportDataDump", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity exportDataDump(@RequestParam(value="institutionCodes") String institutionCodes,
                                         @RequestParam(value="fetchType") Integer fetchType,
                                         @RequestParam(value="date") String date){
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        List<String> institutionCodeList = getInstitutionCode(institutionCodes);
        dataDumpRequest.setInstitutionCodes(institutionCodeList);
        dataDumpRequest.setFetchType(fetchType);
        dataDumpRequest.setDate(date);
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
}
