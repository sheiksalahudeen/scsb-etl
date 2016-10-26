package org.recap.util;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.service.DataDumpSolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by peris on 10/26/16.
 */
public class DataDumpSolrServiceTest extends BaseTestCase {

    @Autowired
    DataDumpSolrService dataDumpSolrService;

    @Test
    public void fetchResultsFromSolrForDataDump() throws Exception {

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(Arrays.asList("PUL","CUL"));
        searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList("Shared"));
        searchRecordsRequest.setPageSize(10);

        Map results = dataDumpSolrService.getResults(searchRecordsRequest);

        Integer totalPageCount = (Integer) results.get("totalPageCount");
        String totalBibsCount = (String) results.get("totalBibsCount");
        List dataDumpSearchResults = (List) results.get("dataDumpSearchResults");
        assertNotNull(totalPageCount);
        assertNotNull(totalBibsCount);
        assertNotNull(dataDumpSearchResults);
        System.out.println("Total Pages : " + totalPageCount);
        System.out.println("Total Bibs : " + totalBibsCount);
    }
}
