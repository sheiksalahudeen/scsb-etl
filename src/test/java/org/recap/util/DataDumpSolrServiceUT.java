package org.recap.util;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.service.DataDumpSolrService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Created by peris on 10/26/16.
 */
public class DataDumpSolrServiceUT extends BaseTestCase {

    @Autowired
    DataDumpSolrService dataDumpSolrService;

    @Test
    public void fetchResultsFromSolrForDataDump() throws Exception {

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(Arrays.asList("PUL","CUL"));
        searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList("Shared"));
        searchRecordsRequest.setTotalRecordsCount("1");
        searchRecordsRequest.setTotalPageCount(1);
        searchRecordsRequest.setPageSize(10);

        Map results = dataDumpSolrService.getResults(searchRecordsRequest);

        Integer totalPageCount = (Integer) results.get("totalPageCount");
        String totalBibsCount = (String) results.get("totalRecordsCount");
        List dataDumpSearchResults = (List) results.get("dataDumpSearchResults");
        assertNotNull(totalPageCount);
        assertNotNull(totalBibsCount);
        assertNotNull(dataDumpSearchResults);
        System.out.println("Total Pages : " + totalPageCount);
        System.out.println("Total Bibs : " + totalBibsCount);
    }

    @Test
    public void fetchResultsFromSolrForIncrementalDataDump() throws Exception{
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(Arrays.asList("PUL","CUL"));
        searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList("Shared"));
        searchRecordsRequest.setFieldName("BibLastUpdatedDate");
        searchRecordsRequest.setTotalRecordsCount("1");
        searchRecordsRequest.setTotalPageCount(1);
        searchRecordsRequest.setFieldValue(getFormattedString("2016-10-21 10:30"));
        searchRecordsRequest.setPageSize(10);
        Map results = dataDumpSolrService.getResults(searchRecordsRequest);

        Integer totalPageCount = (Integer) results.get("totalPageCount");
        String totalBibsCount = (String) results.get("totalRecordsCount");
        List dataDumpSearchResults = (List) results.get("dataDumpSearchResults");
        assertNotNull(totalPageCount);
        assertNotNull(totalBibsCount);
        assertNotNull(dataDumpSearchResults);
        System.out.println("Total Pages : " + totalPageCount);
        System.out.println("Total Bibs : " + totalBibsCount);
    }

    public String getFormattedString(String dateStr){
        String formattedString = dateStr.substring(0,10)+"T"+dateStr.substring(11,16)+":00Z TO NOW";
        return formattedString;
    }
}