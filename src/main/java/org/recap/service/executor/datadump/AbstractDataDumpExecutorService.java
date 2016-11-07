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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

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
    private ProducerTemplate producer;

    @Value("${datadump.httpresponse.record.limit}")
    private String httpResonseRecordLimit;

    @Value("${solrclient.url}")
    String solrClientUrl;

    @Override
    public String process(DataDumpRequest dataDumpRequest) throws ExecutionException, InterruptedException {
        String outputString = null;

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(dataDumpRequest.getInstitutionCodes());
        searchRecordsRequest.setCollectionGroupDesignations(getCodesForIds(dataDumpRequest.getCollectionGroupIds()));
        searchRecordsRequest.setPageSize(10000);

        Map results = dataDumpSolrService.getResults(searchRecordsRequest);
        Integer totalPageCount = (Integer) results.get("totalPageCount");
        Integer totalBibsCount = Integer.valueOf((String) results.get("totalBibsCount"));

        boolean canProcess = canProcessRecords(totalBibsCount, dataDumpRequest.getTransmissionType());
        if (canProcess) {
            String fileName = getFileName(dataDumpRequest, 0);
            String headerString = getBatchHeaderString(totalPageCount, 1, fileName);
            producer.sendBodyAndHeader(ReCAPConstants.SOLR_INPUT_FOR_DATA_EXPORT_Q, results, "batchHeaders", headerString.toString());

            for (int pageNum = 1; pageNum < totalPageCount; pageNum++) {
                searchRecordsRequest.setPageNumber(pageNum);
                Map results1 = dataDumpSolrService.getResults(searchRecordsRequest);
                fileName = getFileName(dataDumpRequest, pageNum + 1);
                headerString = getBatchHeaderString(totalPageCount, pageNum + 1, fileName);
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

    private String getBatchHeaderString(Integer totalPageCount, Integer currentPageCount, String fileName) {
        StringBuilder headerString = new StringBuilder();
        headerString.append("totalPageCount")
                .append("-")
                .append(totalPageCount)
                .append(";")
                .append("currentPageCount")
                .append("-")
                .append(currentPageCount)
                .append(";")
                .append("fileName")
                .append("-")
                .append(fileName);

        return headerString.toString();
    }

    public abstract void populateSearchRequest(SearchRecordsRequest searchRecordsRequest, DataDumpRequest dataDumpRequest);

}
