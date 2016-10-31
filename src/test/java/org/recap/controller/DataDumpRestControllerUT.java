package org.recap.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.recap.ReCAPConstants;
import org.recap.controller.swagger.DataDumpRestController;
import org.recap.model.search.SearchRecordsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by premkb on 19/8/16.
 */
public class DataDumpRestControllerUT extends BaseControllerUT {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpRestControllerUT.class);

    @Autowired
    private DataDumpRestController dataDumpRestController;

    @Value("${solrclient.url}")
    String solrClientUrl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(dataDumpRestController).build();
    }

    @Test
    public void exportFullDataDumpMarcXmlFormat() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("institutionCodes","NYPL")
                .param("fetchType","0")
                .param("requestingInstitutionCode","NYPL")
                .param("outputFormat","0")
                .param("emailToAddress","hemalatha.s@htcindia.com")
                .param("collectionGroupIds","1,2"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(ReCAPConstants.DATADUMP_PROCESS_STARTED,mvcResult.getResponse().getContentAsString());
        assertTrue(status == 200);
    }


    @Test
    public void exportFullDataDumpScsbXmlFormat() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("institutionCodes","NYPL")
                .param("fetchType","0")
                .param("requestingInstitutionCode","NYPL")
                .param("outputFormat","1")
                .param("emailToAddress","hemalatha.s@htcindia.com")
                .param("collectionGroupIds","1,2"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(ReCAPConstants.DATADUMP_PROCESS_STARTED,mvcResult.getResponse().getContentAsString());
        assertTrue(status == 200);
    }

    @Test
    public void exportIncrementalDataDump() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("institutionCodes","NYPL,PUL")
                .param("fetchType","1")
                .param("requestingInstitutionCode","NYPL")
                .param("outputFormat","1")
                .param("emailToAddress","hemalatha.s@htcindia.com")
                .param("date","2016-08-30 11:20"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(ReCAPConstants.DATADUMP_PROCESS_STARTED,mvcResult.getResponse().getContentAsString());
        assertTrue(status == 200);
    }

    @Test
    public void invalidFetchTypeParameters()throws Exception{
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("institutionCodes","NYPL")
                .param("requestingInstitutionCode","NYPL")
                .param("outputFormat","1")
                .param("emailToAddress","hemalatha.s@htcindia.com")
                .param("fetchType","3"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals("1. "+ReCAPConstants.DATADUMP_VALID_FETCHTYPE_ERR_MSG+"\n",mvcResult.getResponse().getContentAsString());
        assertTrue(status == 400);
    }

    @Test
    public void invalidIncremenatlDumpParameters()throws Exception{
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("fetchType","1")
                .param("requestingInstitutionCode","NYPL")
                .param("outputFormat","1")
                .param("emailToAddress","hemalatha.s@htcindia.com")
                .param("institutionCodes","NYPL"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals("1. "+ReCAPConstants.DATADUMP_DATE_ERR_MSG+"\n",mvcResult.getResponse().getContentAsString());
        assertTrue(status == 400);
    }

    @Test
    public void getBibsFromSolr() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(Arrays.asList("PUL","CUL"));
        searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList("Shared"));
        searchRecordsRequest.setPageSize(10);
        RestTemplate restTemplate = new RestTemplate();
        String url = solrClientUrl + "searchService/searchRecords";
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_key","recap");
        HttpEntity<SearchRecordsRequest> requestEntity = new HttpEntity<>(searchRecordsRequest,headers);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestEntity, Map.class);
        assertTrue(responseEntity.getStatusCode().getReasonPhrase().equalsIgnoreCase("OK"));
        Map responseEntityBody = responseEntity.getBody();
        Integer totalPageCount = (Integer) responseEntityBody.get("totalPageCount");
        String totalBibsCount = (String) responseEntityBody.get("totalBibsCount");
        List dataDumpSearchResults = (List) responseEntityBody.get("dataDumpSearchResults");
        assertNotNull(totalPageCount);
        assertNotNull(totalBibsCount);
        assertNotNull(dataDumpSearchResults);
        System.out.println("Total Pages : " + totalPageCount);
        System.out.println("Total Bibs : " + totalBibsCount);
        stopWatch.stop();
        System.out.println("Total Time Taken : " + stopWatch.getTotalTimeSeconds());
    }

}
