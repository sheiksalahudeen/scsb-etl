package org.recap.service.executor.datadump;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.service.DataDumpSolrService;
import org.recap.service.email.datadump.DataDumpEmailService;
import org.recap.service.formatter.datadump.DataDumpFormatterService;
import org.recap.service.transmission.datadump.DataDumpTransmissionService;
import org.recap.util.datadump.DataDumpFailureReportUtil;
import org.recap.util.datadump.DataDumpSuccessReportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by premkb on 27/9/16.
 */
public abstract class AbstractDataDumpExecutorService implements DataDumpExecutorInterface{

    private static final Logger logger = LoggerFactory.getLogger(AbstractDataDumpExecutorService.class);

    private ExecutorService executorService;

    @Autowired
    DataDumpSolrService dataDumpSolrService;

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

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

    @Value("${solrclient.url}")
    String solrClientUrl;

    @Override
    public String process(DataDumpRequest dataDumpRequest) throws ExecutionException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        setExecutorService(dataDumpRequest.getNoOfThreads());

        String outputString = null;
        List<Map<String,Object>> successAndFailureFormattedFullList = new ArrayList<>();
        List<Callable<List<BibliographicEntity>>> callables = new ArrayList<>();

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(dataDumpRequest.getInstitutionCodes());
        searchRecordsRequest.setCollectionGroupDesignations(getCodesForIds(dataDumpRequest.getCollectionGroupIds()));
        searchRecordsRequest.setPageSize(10000);

        Map results = dataDumpSolrService.getResults(searchRecordsRequest);
        Integer totalPageCount = (Integer) results.get("totalPageCount");
        Integer totalBibsCount = Integer.valueOf((String) results.get("totalBibsCount"));

        boolean canProcess = canProcessRecords(totalBibsCount,dataDumpRequest.getTransmissionType());
        if(logger.isInfoEnabled()) {
            logger.info("Total no. of Bibs to export" + totalBibsCount);
        }

        if (canProcess) {

            List<LinkedHashMap> dataDumpSearchResults = (List<LinkedHashMap>) results.get("dataDumpSearchResults");
            callables.add(getImprovedFullDataDumpCallable(dataDumpSearchResults, bibliographicDetailsRepository));

            for(int pageNum = 1; pageNum < totalPageCount; pageNum++){
                searchRecordsRequest.setPageNumber(pageNum);
                results = dataDumpSolrService.getResults(searchRecordsRequest);
                dataDumpSearchResults = (List<LinkedHashMap>) results.get("dataDumpSearchResults");

                Callable callable = getImprovedFullDataDumpCallable(dataDumpSearchResults,bibliographicDetailsRepository);
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

                String fileName = dataDumpRequest.getRequestingInstitutionCode()+ File.separator+dataDumpRequest.getDateTimeString()+File.separator+ReCAPConstants.DATA_DUMP_FILE_NAME+ dataDumpRequest.getRequestingInstitutionCode()+count;
                logger.info("filename----->"+fileName);
                producer.sendBodyAndHeader(ReCAPConstants.DATADUMP_FILE_SYSTEM_Q,  outputString, "fileName", fileName);
                successAndFailureFormattedFullList.add(successAndFailureFormattedList);
                count++;
                stopWatchPerFile.stop();
                if(logger.isInfoEnabled()){
                    logger.info("Total time taken to export file no. "+(count)+" is "+stopWatchPerFile.getTotalTimeMillis()/1000+" seconds");
                    logger.info("File no. "+(count)+" exported");
                }
            }
            Map<String, String> routeMap = getRouteMap(dataDumpRequest, count);
            String fileName = ReCAPConstants.DATA_DUMP_FILE_NAME+ dataDumpRequest.getRequestingInstitutionCode()+"-"+dataDumpRequest.getDateTimeString();
            routeMap.put(ReCAPConstants.FILENAME,fileName);
            dataDumpTransmissionService.startTranmission(dataDumpRequest, routeMap);
            processEmail(dataDumpRequest,totalBibsCount,dataDumpRequest.getDateTimeString());

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

    private void processEmail(DataDumpRequest dataDumpRequest,Integer totalRecordCount,String dateTimeStringForFolder){
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

    private boolean canProcessRecords(Integer totalRecordCount, String transmissionType ){
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

    private List<String> getCodesForIds(List<Integer> collectionGroupIds) {
        List codes = new ArrayList();
        Iterable<CollectionGroupEntity> all =
                collectionGroupDetailsRepository.findAll();

        for (Iterator<CollectionGroupEntity> iterator = all.iterator(); iterator.hasNext(); ) {
            CollectionGroupEntity collectionGroupEntity = iterator.next();
            if(collectionGroupIds.contains(collectionGroupEntity.getCollectionGroupId())){
                codes.add(collectionGroupEntity.getCollectionGroupCode());
            }
        }
        return codes;
    }

    public abstract Long getTotalRecordsCount(DataDumpRequest dataDumpRequest);

    public abstract Callable getCallable(int pageNum, int batchSize, DataDumpRequest dataDumpRequest, BibliographicDetailsRepository bibliographicDetailsRepository);

    protected abstract Callable<List<BibliographicEntity>> getImprovedFullDataDumpCallable(List<LinkedHashMap> dataDumpSearchResults, BibliographicDetailsRepository bibliographicDetailsRepository);
}
