package org.recap.executors;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.etl.ExportDataDumpCallable;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.concurrent.*;

/**
 * Created by premkb on 19/8/16.
 */
@Service
public class ExportDataDumpExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(ExportDataDumpExecutorService.class);

    private ExecutorService executorService;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    private StopWatch stopWatch;

    public void exportDump(DataDumpRequest dataDumpRequest)throws InterruptedException,ExecutionException{
        startProcess();
        int noOfThreads = dataDumpRequest.getNoOfThreads();
        int batchSize = dataDumpRequest.getBatchSize();
        Long totalRecordCount = bibliographicDetailsRepository.count();
        if(logger.isInfoEnabled()){
            logger.info("Total no. of records to be exported - "+totalRecordCount);
            logger.info("Records per file - "+batchSize);
        }
        int loopCount = getLoopCount(totalRecordCount,batchSize);
        setExecutorService(noOfThreads);
        for(int pageNum=1;pageNum<=loopCount;pageNum++){
            StopWatch stopWatchPerFile = new StopWatch();
            stopWatchPerFile.start();
            Callable callable = getExportDataDumpCallable(pageNum,batchSize,bibliographicDetailsRepository);
            BibRecords bibRecords = getExecutorService().submit(callable) == null ? null : (BibRecords)getExecutorService().submit(callable).get();
            String fileName = ReCAPConstants.DATA_DUMP_FILE_NAME + pageNum + ReCAPConstants.XML_FILE_FORMAT;
            producer.sendBodyAndHeader("seda:dataDumpQ", bibRecords, "fileName", fileName);
            stopWatchPerFile.stop();
            if(logger.isInfoEnabled()){
                logger.info("Total time taken to export file no. "+pageNum+" is "+stopWatchPerFile.getTotalTimeMillis()/1000+" seconds");
                logger.info("File no. "+pageNum+" exported");
            }
        }
        getExecutorService().shutdownNow();
        getStopWatch().stop();
        if(logger.isInfoEnabled()){
            logger.info("Total time taken to export all data - "+stopWatch.getTotalTimeMillis()/1000+" seconds ("+stopWatch.getTotalTimeMillis()/60000+" minutes)");
        }
    }

    private int getLoopCount(Long totalRecordCount,int batchSize){
        int quotient = Integer.valueOf(Long.toString(totalRecordCount)) / (batchSize);
        int remainder = Integer.valueOf(Long.toString(totalRecordCount)) % (batchSize);
        int loopCount = remainder == 0 ? quotient : quotient + 1;
        return loopCount;
    }

    private Callable getExportDataDumpCallable(int pageNum,int batchSize,BibliographicDetailsRepository bibliographicDetailsRepository){
        return new ExportDataDumpCallable(pageNum,batchSize,bibliographicDetailsRepository);
    }

    private void setExecutorService(Integer numThreads) {
        if (null == executorService || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(numThreads);
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public StopWatch getStopWatch() {
        if (null == stopWatch) {
            stopWatch = new StopWatch();
        }
        return stopWatch;
    }

    private void startProcess() {
        getStopWatch().start();
    }
}
