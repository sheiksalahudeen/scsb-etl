package org.recap.service.executor.datadump;

import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.service.DataDumpSolrService;
import org.recap.util.datadump.BatchCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by premkb on 27/9/16.
 */
public abstract class AbstractDataDumpExecutorService implements DataDumpExecutorInterface {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDataDumpExecutorService.class);

    @Autowired
    private DataDumpSolrService dataDumpSolrService;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private DataExportHeaderUtil dataExportHeaderUtil;

    @Value("${datadump.httpresponse.record.limit}")
    private String httpResonseRecordLimit;

    @Value("${solrclient.url}")
    private String solrClientUrl;

    @Value("${datadump.batch.size}")
    private String dataDumpBatchSize;

    @Override
    public String process(DataDumpRequest dataDumpRequest) throws ExecutionException, InterruptedException {
        String outputString;

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(dataDumpRequest.getInstitutionCodes());
        searchRecordsRequest.setCollectionGroupDesignations(getCodesForIds(dataDumpRequest.getCollectionGroupIds()));
        searchRecordsRequest.setPageSize(Integer.valueOf(dataDumpBatchSize));
        populateSearchRequest(searchRecordsRequest, dataDumpRequest);

        Map results = dataDumpSolrService.getResults(searchRecordsRequest);
        Integer totalPageCount = (Integer) results.get("totalPageCount");
        Integer totalBibsCount = Integer.valueOf((String) results.get("totalRecordsCount"));

        boolean isRecordsToProcess = totalBibsCount > 0 ? true : false;
        boolean canProcess = canProcessRecords(totalBibsCount, dataDumpRequest.getTransmissionType());
        if (isRecordsToProcess && canProcess) {
            outputString = ReCAPConstants.DATADUMP_RECORDS_AVAILABLE_FOR_PROCESS;
            sendBodyForIsRecordAvailableMessage(outputString);
            String fileName = getFileName(dataDumpRequest, 0);
            String folderName = getFolderName(dataDumpRequest);
            BatchCounter.reset();
            BatchCounter.setCurrentPage(1);
            BatchCounter.setTotalPages(totalPageCount);
            String headerString = dataExportHeaderUtil.getBatchHeaderString(totalPageCount, 1, folderName, fileName, dataDumpRequest);
            sendBodyAndHeader(results, headerString);

            for (int pageNum = 1; pageNum < totalPageCount; pageNum++) {
                Thread.sleep(10000);
                searchRecordsRequest.setPageNumber(pageNum);
                BatchCounter.setCurrentPage(pageNum + 1);
                Map results1 = dataDumpSolrService.getResults(searchRecordsRequest);
                fileName = getFileName(dataDumpRequest, pageNum + 1);
                headerString = dataExportHeaderUtil.getBatchHeaderString(totalPageCount, pageNum + 1, folderName, fileName, dataDumpRequest);
                sendBodyAndHeader(results1, headerString);
            }
            return "Success";

        } else {
            if (!isRecordsToProcess) {
                outputString = ReCAPConstants.DATADUMP_NO_RECORD;
                sendBodyForIsRecordAvailableMessage(outputString);
            } else {
                outputString = ReCAPConstants.DATADUMP_HTTP_REPONSE_RECORD_LIMIT_ERR_MSG;
                sendBodyForIsRecordAvailableMessage(outputString);
            }
        }
        return outputString;
    }

    private void sendBodyAndHeader(Map results, String headerString) {
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(camelContext);
        fluentProducerTemplate
                .to(ReCAPConstants.SOLR_INPUT_FOR_DATA_EXPORT_Q)
                .withBody(results)
                .withHeader("batchHeaders", headerString.toString());
        fluentProducerTemplate.send();
    }

    private void sendBodyForHttp(String outputString) {
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(camelContext);
        fluentProducerTemplate
                .to(ReCAPConstants.DATADUMP_HTTP_Q)
                .withBody(outputString);
        fluentProducerTemplate.send();
    }

    private void sendBodyForIsRecordAvailableMessage(String outputString) {
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(camelContext);
        fluentProducerTemplate
                .to(ReCAPConstants.DATADUMP_IS_RECORD_AVAILABLE_Q)
                .withBody(outputString);
        fluentProducerTemplate.send();
    }

    private String getFileName(DataDumpRequest dataDumpRequest, int pageNum) {
        return dataDumpRequest.getRequestingInstitutionCode()
                + File.separator
                + getOutputFormat(dataDumpRequest)
                + File.separator
                + dataDumpRequest.getDateTimeString()
                + File.separator
                + pageNum;
    }

    private String getOutputFormat(DataDumpRequest dataDumpRequest) {
        switch (dataDumpRequest.getOutputFileFormat()) {
            case "0":
                return "MarcXml";
            case "1":
                return "SCSBXml";
            case "2":
                return "Json";
        }
        return null;

    }

    private String getFolderName(DataDumpRequest dataDumpRequest) {
        return dataDumpRequest.getRequestingInstitutionCode()
                + File.separator
                + getOutputFormat(dataDumpRequest)
                + File.separator
                + dataDumpRequest.getDateTimeString();
    }

    private boolean canProcessRecords(Integer totalRecordCount, String transmissionType) {
        boolean canProcess = true;
        if (totalRecordCount > Integer.parseInt(httpResonseRecordLimit) && transmissionType.equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP)) {
            canProcess = false;
        }
        return canProcess;
    }

    private List<String> getCodesForIds(List<Integer> collectionGroupIds) {
        List codes = new ArrayList();
        Iterable<CollectionGroupEntity> all =
                collectionGroupDetailsRepository.findAll();

        for (Iterator<CollectionGroupEntity> iterator = all.iterator(); iterator.hasNext(); ) {
            CollectionGroupEntity collectionGroupEntity = iterator.next();
            if (collectionGroupIds.contains(collectionGroupEntity.getCollectionGroupId())) {
                codes.add(collectionGroupEntity.getCollectionGroupCode());
            }
        }
        return codes;
    }

    public abstract void populateSearchRequest(SearchRecordsRequest searchRecordsRequest, DataDumpRequest dataDumpRequest);

    public String getFormattedDateString(String inputDateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_YYYYMMDDHHMM);
        String utcStr = null;
        try {
            Date date = simpleDateFormat.parse(inputDateString);
            DateFormat format = new SimpleDateFormat(ReCAPConstants.UTC_DATE_FORMAT);
            format.setTimeZone(TimeZone.getTimeZone(ReCAPConstants.UTC));
            utcStr = format.format(date);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return utcStr + ReCAPConstants.SOLR_DATE_RANGE_TO_NOW;
    }
}