package org.recap.service;

import org.recap.model.search.SearchRecordsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Created by peris on 10/26/16.
 */

@Service
public class DataDumpSolrService {
    @Value("${solrclient.url}")
    String solrClientUrl;

    public String getSolrClientUrl() {
        return solrClientUrl;
    }

    public void setSolrClientUrl(String solrClientUrl) {
        this.solrClientUrl = solrClientUrl;
    }

    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    public Map getResults(SearchRecordsRequest searchRecordsRequest) {
        String url = getSolrClientUrl() + "searchService/searchRecords";
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_key","recap");
        HttpEntity<SearchRecordsRequest> requestEntity = new HttpEntity<>(searchRecordsRequest,headers);
        ResponseEntity<Map> responseEntity = getRestTemplate().postForEntity(url, requestEntity, Map.class);

        return responseEntity.getBody();
    }
}
