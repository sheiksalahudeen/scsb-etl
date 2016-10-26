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

    public Map getResults(SearchRecordsRequest searchRecordsRequest) {
        RestTemplate restTemplate = new RestTemplate();
        String url = solrClientUrl + "searchService/searchRecords";
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_key","recap");
        HttpEntity<SearchRecordsRequest> requestEntity = new HttpEntity<>(searchRecordsRequest,headers);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, requestEntity, Map.class);

        return responseEntity.getBody();
    }
}
