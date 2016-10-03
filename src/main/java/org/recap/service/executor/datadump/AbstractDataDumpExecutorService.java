package org.recap.service.executor.datadump;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.service.email.datadump.DataDumpEmailService;
import org.recap.service.formatter.datadump.DataDumpFormatterService;
import org.recap.service.transmission.datadump.DataDumpTransmissionService;
import org.recap.util.datadump.DataDumpFailureReportUtil;
import org.recap.util.datadump.DataDumpSuccessReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by premkb on 27/9/16.
 */
public abstract class AbstractDataDumpExecutorService implements DataDumpExecutorInterface{

    private static final Logger logger = LoggerFactory.getLogger(AbstractDataDumpExecutorService.class);

    private ExecutorService executorService;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private DataDumpFormatterService dataDumpFormatterService;

    @Autowired
    private DataDumpTransmissionService dataDumpTransmissionService;

    @Autowired
    private DataDumpSuccessReportUtil dataDumpSuccessReportUtil;

    @Autowired
    private DataDumpFailureReportUtil dataDumpFailureReportUtil;

    @Autowired
    private DataDumpEmailService dataDumpEmailService;

    @Value("${datadump.httpresponse.record.limit}")
    private String httpResonseRecordLimit;

    @Override
    public String process(DataDumpRequest dataDumpRequest) throws ExecutionException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        setExecutorService(dataDumpRequest.getNoOfThreads());
        int batchSize = dataDumpRequest.getBatchSize();
        Long totalRecordCount = getTotalRecordsCount(dataDumpRequest);
        setRecordsAvailability(totalRecordCount,dataDumpRequest);
        int loopCount = getLoopCount(totalRecordCount,batchSize);
        String outputString = null;
        List<Map<String,Object>> successAndFailureFormattedFullList = new ArrayList<>();
        List<Callable<List<BibliographicEntity>>> callables = new ArrayList<>();
        boolean canProcess = canProcessRecords(totalRecordCount,dataDumpRequest.getTransmissionType());
        if(logger.isInfoEnabled()) {
            logger.info("Total no. of records " + totalRecordCount);
        }
        if (canProcess) {
            for(int pageNum = 0;pageNum < loopCount;pageNum++){
                Callable callable = getCallable(pageNum,batchSize,dataDumpRequest,bibliographicDetailsRepository);
                callables.add(callable);
            }
            List<Future<List<BibliographicEntity>>> futureList = getExecutorService().invokeAll(callables);
            futureList.stream()
                      .map(future -> {
                          try {
                              return future.get();
                          } catch (InterruptedException | ExecutionException e) {
                              logger.error(e.getMessage());
                              throw new RuntimeException(e);
                          }
                      });
            int count=0;
            for(Future future:futureList){
                StopWatch stopWatchPerFile = new StopWatch();
                stopWatchPerFile.start();
                List<BibliographicEntity> bibliographicEntityList = (List<BibliographicEntity>)future.get();
                Object formattedObject = dataDumpFormatterService.getFormattedObject(bibliographicEntityList,dataDumpRequest.getOutputFormat());
                Map<String,Object> successAndFailureFormattedList = (Map<String,Object>) formattedObject;
                outputString = (String) successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_FORMATTEDSTRING);
                dataDumpTransmissionService.starTranmission(outputString,dataDumpRequest,getRouteMap(dataDumpRequest, count));
                successAndFailureFormattedFullList.add(successAndFailureFormattedList);
                count++;
                stopWatchPerFile.stop();
                if(logger.isInfoEnabled()){
                    logger.info("Total time taken to export file no. "+(count)+" is "+stopWatchPerFile.getTotalTimeMillis()/1000+" seconds");
                    logger.info("File no. "+(count)+" exported");
                }
            }
            processEmail(dataDumpRequest,totalRecordCount,dataDumpRequest.getDateTimeString());

        }else{
            outputString = ReCAPConstants.DATADUMP_HTTP_REPONSE_RECORD_LIMIT_ERR_MSG;
        }
        generateReport(successAndFailureFormattedFullList,dataDumpRequest);
        getExecutorService().shutdownNow();
        stopWatch.stop();
        if(logger.isInfoEnabled()){
            logger.info("Total time taken to export all data - "+stopWatch.getTotalTimeMillis()/1000+" seconds ("+stopWatch.getTotalTimeMillis()/60000+" minutes)");
        }
        return outputString;
    }

    private void processEmail(DataDumpRequest dataDumpRequest,Long totalRecordCount,String dateTimeStringForFolder){
        if (dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP)
                || dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM)) {
            dataDumpEmailService.sendEmail(dataDumpRequest.getInstitutionCodes(), totalRecordCount, dataDumpRequest.getRequestingInstitutionCode(), dataDumpRequest.getTransmissionType(),dateTimeStringForFolder, dataDumpRequest.getToEmailAddress());
        }
    }

    private void setRecordsAvailability(Long totalRecordCount,DataDumpRequest dataDumpRequest){
        if(totalRecordCount > 0 ){
            dataDumpRequest.setRecordsAvailable(true);
        }else{
            dataDumpRequest.setRecordsAvailable(false);
        }
    }

    private boolean canProcessRecords(Long totalRecordCount, String transmissionType ){
        boolean canProcess = true;
        if(totalRecordCount > Integer.parseInt(httpResonseRecordLimit) && transmissionType.equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP)){
            canProcess = false;
        }
        return canProcess;
    }
    private void generateReport(List<Map<String,Object>> successAndFailureFormattedFullList ,DataDumpRequest dataDumpRequest){
        List<BibliographicEntity> successList = new ArrayList<>();
        List<BibliographicEntity> failureList = new ArrayList<>();
        int errorCount = 0;
        for(Map<String,Object>  successAndFailureFormattedList:successAndFailureFormattedFullList){
            successList.addAll((List<BibliographicEntity>)successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_SUCCESSLIST));
            failureList.addAll((List<BibliographicEntity>)successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_FAILURELIST));
            if(successAndFailureFormattedList.get(ReCAPConstants.DATADUMP_FORMATERROR) != null){
                errorCount++;
            }
        }
        if(successList.size()>0){
            generateSuccessReport(successAndFailureFormattedFullList,dataDumpRequest);
        }
        if(failureList.size()>0 || errorCount > 0){
            generateFailureReport(successAndFailureFormattedFullList,dataDumpRequest);
        }
    }

    private void generateSuccessReport(List<Map<String,Object>> successAndFailureFormattedFullList ,DataDumpRequest dataDumpRequest){
        ReportEntity reportEntity = new ReportEntity();
        List<ReportDataEntity> reportDataEntities = dataDumpSuccessReportUtil.generateDataDumpSuccessReport(successAndFailureFormattedFullList,dataDumpRequest);
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setFileName(ReCAPConstants.OPERATION_TYPE_DATADUMP+"-"+dataDumpRequest.getDateTimeString());
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(ReCAPConstants.OPERATION_TYPE_DATADUMP_SUCCESS);
        reportEntity.setInstitutionName(dataDumpRequest.getRequestingInstitutionCode());
        producer.sendBody(ReCAPConstants.REPORT_Q, reportEntity);
    }

    private void generateFailureReport(List<Map<String,Object>> successAndFailureFormattedFullList ,DataDumpRequest dataDumpRequest){
        ReportEntity reportEntity = new ReportEntity();
        List<ReportDataEntity> reportDataEntities = dataDumpFailureReportUtil.generateDataDumpFailureReport(successAndFailureFormattedFullList,dataDumpRequest);
        reportEntity.setReportDataEntities(reportDataEntities);
        reportEntity.setFileName(ReCAPConstants.OPERATION_TYPE_DATADUMP+"-"+dataDumpRequest.getDateTimeString());
        reportEntity.setCreatedDate(new Date());
        reportEntity.setType(ReCAPConstants.OPERATION_TYPE_DATADUMP_FAILURE);
        reportEntity.setInstitutionName(dataDumpRequest.getRequestingInstitutionCode());
        producer.sendBody(ReCAPConstants.REPORT_Q, reportEntity);
    }

    private void setExecutorService(Integer numThreads) {
        if (null == executorService || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(numThreads);
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Map<String,String> getRouteMap(DataDumpRequest dataDumpRequest, int pageNum){
        Map<String,String> routeMap = new HashMap<>();
        String fileName = ReCAPConstants.DATA_DUMP_FILE_NAME+ dataDumpRequest.getRequestingInstitutionCode() + (pageNum+1);
        routeMap.put(ReCAPConstants.FILENAME,fileName);
        routeMap.put(ReCAPConstants.DATETIME_FOLDER, dataDumpRequest.getDateTimeString());
        routeMap.put(ReCAPConstants.REQUESTING_INST_CODE,dataDumpRequest.getRequestingInstitutionCode());
        routeMap.put(ReCAPConstants.FILE_FORMAT,dataDumpRequest.getFileFormat());
        return routeMap;
    }

    public int getLoopCount(Long totalRecordCount,int batchSize){
        int quotient = Integer.valueOf(Long.toString(totalRecordCount)) / (batchSize);
        int remainder = Integer.valueOf(Long.toString(totalRecordCount)) % (batchSize);
        int loopCount = remainder == 0 ? quotient : quotient + 1;
        return loopCount;
    }

   public abstract Long getTotalRecordsCount(DataDumpRequest dataDumpRequest);

    public abstract Callable getCallable(int pageNum, int batchSize, DataDumpRequest dataDumpRequest, BibliographicDetailsRepository bibliographicDetailsRepository);
}
