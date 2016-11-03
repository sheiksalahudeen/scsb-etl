package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.recap.camel.FileNameProcessorForFailureRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by peris on 11/1/16.
 */

@Component
public class SolrSearchResultsProcessorForExport implements Processor {

    Logger logger = LoggerFactory.getLogger(SolrSearchResultsProcessorForExport.class);

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;
    private ExecutorService executorService;

    @Override
    public void process(Exchange exchange) throws Exception {

        long startTime = System.currentTimeMillis();

        Map results = (Map) exchange.getIn().getBody();
        List<HashMap> dataDumpSearchResults = (List<HashMap>) results.get("dataDumpSearchResults");

        List<BibliographicEntity> bibliographicEntities = new ArrayList<>();

        List<Integer> bibIdList = new ArrayList<>();
        Map<Integer,List<Integer>> bibItemMap = new HashMap<>();
        for (Iterator<HashMap> iterator = dataDumpSearchResults.iterator(); iterator.hasNext(); ) {
            HashMap hashMap = iterator.next();
            Integer bibId = (Integer) hashMap.get("bibId");
            bibIdList.add(bibId);
            List<Integer> itemIds = (List<Integer>) hashMap.get("itemIds");
            bibItemMap.put(bibId,itemIds);
        }

        List<BibliographicEntity> bibliographicEntityList = bibliographicDetailsRepository.getBibliographicEntityList(bibIdList);

        List<Callable<BibliographicEntity>> callables = new ArrayList<>();

        for (Iterator<BibliographicEntity> iterator = bibliographicEntityList.iterator(); iterator.hasNext(); ) {
            BibliographicEntity bibliographicEntity = iterator.next();
            BibEntityPreparerCallable bibEntityPreparerCallable = new BibEntityPreparerCallable(bibliographicEntity, bibItemMap.get(bibliographicEntity.getBibliographicId()));
            callables.add(bibEntityPreparerCallable);
        }

        List<Future<BibliographicEntity>> futureList = getExecutorService().invokeAll(callables);
        futureList.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                });


        for(Future future:futureList){
            bibliographicEntities.add((BibliographicEntity) future.get());
        }

        long endTime = System.currentTimeMillis();

        logger.info("Time taken to prepare " + bibliographicEntities.size() + " bib entities is : " + (endTime-startTime)/1000 + " ms ");

        getExecutorService().shutdown();

        exchange.getOut().setBody(bibliographicEntities);
        exchange.getOut().setHeader("fileName", exchange.getIn().getHeader("fileName"));
    }

    public ExecutorService getExecutorService() {
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(50);
        }
        if(executorService.isShutdown()){
            executorService = Executors.newFixedThreadPool(50);
        }
        return executorService;
    }
}
