package org.recap.camel;

import com.google.common.collect.Lists;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.marc4j.marc.Record;
import org.recap.camel.datadump.BibEntityPreparerCallable;
import org.recap.camel.datadump.MarcRecordPreparerCallable;
import org.recap.camel.datadump.SolrSearchResultsProcessorForExport;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
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
public class MarcRecordFormatProcessor implements Processor {

    Logger logger = LoggerFactory.getLogger(MarcRecordFormatProcessor.class);

    @Autowired
    MarcXmlFormatterService marcXmlFormatterService;

    private ExecutorService executorService;

    @Override
    public void process(Exchange exchange) throws Exception {
        List<Record> records = new ArrayList<>();

        long startTime = System.currentTimeMillis();


        List<BibliographicEntity> bibliographicEntities = (List<BibliographicEntity>) exchange.getIn().getBody();

        List<Callable<Record>> callables = new ArrayList<>();

        List<List<BibliographicEntity>> partitionList = Lists.partition(bibliographicEntities, 1000);

        for (Iterator<List<BibliographicEntity>> iterator = partitionList.iterator(); iterator.hasNext(); ) {
            List<BibliographicEntity> bibliographicEntityList = iterator.next();
            MarcRecordPreparerCallable marcRecordPreparerCallable =
                    new MarcRecordPreparerCallable(bibliographicEntityList, marcXmlFormatterService);
            callables.add(marcRecordPreparerCallable);
        }

        List<Future<Record>> futureList = getExecutorService().invokeAll(callables);
        futureList.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                });

        for (Future future : futureList) {
            records.addAll((Collection<? extends Record>) future.get());
        }

        long endTime = System.currentTimeMillis();

        logger.info("Time taken to prepare " + bibliographicEntities.size() + " marc records : " + (endTime - startTime) / 1000 + " ms ");

        exchange.getOut().setBody(records);
    }


    public ExecutorService getExecutorService() {
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(50);
        }
        if (executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(50);
        }
        return executorService;
    }

}

