package org.recap.service.executor.datadump;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.DataExportHeaderUtil;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.service.DataDumpSolrService;
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
    DataDumpSolrService dataDumpSolrService;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private DataExportHeaderUtil dataExportHeaderUtil;

    @Autowired
    private ProducerTemplate producer;

    @Value("${datadump.httpresponse.record.limit}")
    private String httpResonseRecordLimit;

    @Value("${solrclient.url}")
    String solrClientUrl;

    @Value("${datadump.batch.size}")
    String dataDumpBatchSize;

    @Override
    public String process(DataDumpRequest dataDumpRequest) throws ExecutionException, InterruptedException {
        String outputString = null;

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(dataDumpRequest.getInstitutionCodes());
        searchRecordsRequest.setCollectionGroupDesignations(getCodesForIds(dataDumpRequest.getCollectionGroupIds()));
        searchRecordsRequest.setPageSize(Integer.valueOf(dataDumpBatchSize));
        populateSearchRequest(searchRecordsRequest,dataDumpRequest);

        Map results = dataDumpSolrService.getResults(searchRecordsRequest);
        Integer totalPageCount = (Integer) results.get("totalPageCount");
        Integer totalBibsCount = Integer.valueOf((String) results.get("totalBibsCount"));

        boolean canProcess = canProcessRecords(totalBibsCount, dataDumpRequest.getTransmissionType());
        if (canProcess) {
            String fileName = getFileName(dataDumpRequest, 0);
            String folderName = getFolderName(dataDumpRequest);
            String headerString = dataExportHeaderUtil.getBatchHeaderString(totalPageCount, 1, folderName, fileName, dataDumpRequest);
            producer.sendBodyAndHeader(ReCAPConstants.SOLR_INPUT_FOR_DATA_EXPORT_Q, results, "batchHeaders", headerString.toString());

            for (int pageNum = 1; pageNum < totalPageCount; pageNum++) {
                searchRecordsRequest.setPageNumber(pageNum);
                Map results1 = dataDumpSolrService.getResults(searchRecordsRequest);
                fileName = getFileName(dataDumpRequest, pageNum + 1);
                headerString = dataExportHeaderUtil.getBatchHeaderString(totalPageCount, pageNum + 1, folderName, fileName, dataDumpRequest);
                producer.sendBodyAndHeader(ReCAPConstants.SOLR_INPUT_FOR_DATA_EXPORT_Q, results1, "batchHeaders", headerString.toString());
            }
            return "Success";

        } else {
            outputString = ReCAPConstants.DATADUMP_HTTP_REPONSE_RECORD_LIMIT_ERR_MSG;
        }
        return outputString;
    }

    private String getFileName(DataDumpRequest dataDumpRequest, int pageNum) {
        return dataDumpRequest.getRequestingInstitutionCode()
                + File.separator
                + dataDumpRequest.getDateTimeString()
                + File.separator
                + pageNum;
    }

    private String getFolderName(DataDumpRequest dataDumpRequest) {
        return dataDumpRequest.getRequestingInstitutionCode()
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

    public String getFormattedDateString(String inputDateString){
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