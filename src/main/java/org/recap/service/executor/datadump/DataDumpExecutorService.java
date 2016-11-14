package org.recap.service.executor.datadump;

import org.recap.model.export.DataDumpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by premkb on 27/9/16.
 */
@Service
public class DataDumpExecutorService {

    private Logger logger = LoggerFactory.getLogger(DataDumpExecutorService.class);

    private List<DataDumpExecutorInterface> dataDumpExecutorInterfaceList;

    @Autowired
    private IncrementalDataDumpExecutorService incrementalDataDumpExecutorService;

    @Autowired
    private FullDataDumpExecutorService fullDataDumpExecutorService;

    @Autowired
    private DeletedDataDumpExecutorService deletedDataDumpExecutorService;

    public String generateDataDump(DataDumpRequest dataDumpRequest) throws ExecutionException, InterruptedException {
        String outputString = null;
        for(DataDumpExecutorInterface dataDumpExecutorInterface:getExecutor()){
            if(dataDumpExecutorInterface.isInterested(dataDumpRequest.getFetchType())){
                outputString = dataDumpExecutorInterface.process(dataDumpRequest);
            }
        }
        return outputString;
    }

    public List<DataDumpExecutorInterface> getExecutor(){
        if(CollectionUtils.isEmpty(dataDumpExecutorInterfaceList)){
            dataDumpExecutorInterfaceList = new ArrayList<>();
            dataDumpExecutorInterfaceList.add(fullDataDumpExecutorService);
            dataDumpExecutorInterfaceList.add(incrementalDataDumpExecutorService);
            dataDumpExecutorInterfaceList.add(deletedDataDumpExecutorService);
        }
        return dataDumpExecutorInterfaceList;
    }

}
