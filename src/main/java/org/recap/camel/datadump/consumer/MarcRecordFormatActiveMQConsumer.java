package org.recap.camel.datadump.consumer;

import com.google.common.collect.Lists;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.marc4j.marc.Record;
import org.recap.RecapConstants;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.recap.camel.datadump.callable.MarcRecordPreparerCallable;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by peris on 11/1/16.
 */

public class MarcRecordFormatActiveMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MarcRecordFormatActiveMQConsumer.class);

    MarcXmlFormatterService marcXmlFormatterService;
    private ExecutorService executorService;
    private DataExportHeaderUtil dataExportHeaderUtil;

    public MarcRecordFormatActiveMQConsumer(MarcXmlFormatterService marcXmlFormatterService) {
        this.marcXmlFormatterService = marcXmlFormatterService;
    }

    public void processRecords(Exchange exchange) throws Exception {
        FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());


        List<Record> records = new ArrayList<>();

        long startTime = System.currentTimeMillis();


        List<BibliographicEntity> bibliographicEntities = (List<BibliographicEntity>) exchange.getIn().getBody();

        List<Callable<Map<String, Object>>> callables = new ArrayList<>();

        List<List<BibliographicEntity>> partitionList = Lists.partition(bibliographicEntities, 1000);

        for (Iterator<List<BibliographicEntity>> iterator = partitionList.iterator(); iterator.hasNext(); ) {
            List<BibliographicEntity> bibliographicEntityList = iterator.next();
            MarcRecordPreparerCallable marcRecordPreparerCallable =
                    new MarcRecordPreparerCallable(bibliographicEntityList, marcXmlFormatterService);
            callables.add(marcRecordPreparerCallable);
        }

        List<Future<Map<String, Object>>> futureList = getExecutorService().invokeAll(callables);
        futureList.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                });

        List failures = new ArrayList();
        for (Future future : futureList) {
            Map<String, Object> results = (Map<String, Object>) future.get();
            Collection<? extends Record> successRecords = (Collection<? extends Record>) results.get(RecapConstants.SUCCESS);
            if (!CollectionUtils.isEmpty(successRecords)) {
                records.addAll(successRecords);
            }
            Collection failureRecords = (Collection) results.get(RecapConstants.FAILURE);
            if (!CollectionUtils.isEmpty(failureRecords)) {
                failures.addAll(failureRecords);
            }
        }

        String batchHeaders = (String) exchange.getIn().getHeader(RecapConstants.BATCH_HEADERS);
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");
        processFailures(exchange, failures, batchHeaders, requestId);

        long endTime = System.currentTimeMillis();

        logger.info("Time taken to prepare {} marc records : {} seconds " , bibliographicEntities.size() , (endTime - startTime) / 1000);

        fluentProducerTemplate
                .to(RecapConstants.MARC_RECORD_FOR_DATA_EXPORT_Q)
                .withBody(records)
                .withHeader(RecapConstants.BATCH_HEADERS, exchange.getIn().getHeader(RecapConstants.BATCH_HEADERS))
                .withHeader(RecapConstants.EXPORT_FORMAT, exchange.getIn().getHeader(RecapConstants.EXPORT_FORMAT))
                .withHeader(RecapConstants.TRANSMISSION_TYPE, exchange.getIn().getHeader(RecapConstants.TRANSMISSION_TYPE));
        fluentProducerTemplate.send();
    }

    private void processFailures(Exchange exchange, List failures, String batchHeaders, String requestId) {
        if (!CollectionUtils.isEmpty(failures)) {
            HashMap values = new HashMap();
            values.put(RecapConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.REQUESTING_INST_CODE));
            values.put(RecapConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.INSTITUTION_CODES));
            values.put(RecapConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.FETCH_TYPE));
            values.put(RecapConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.COLLECTION_GROUP_IDS));
            values.put(RecapConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.TRANSMISSION_TYPE));
            values.put(RecapConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.EXPORT_FROM_DATE));
            values.put(RecapConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.EXPORT_FORMAT));
            values.put(RecapConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, RecapConstants.TO_EMAIL_ID));
            values.put(RecapConstants.NUM_RECORDS, String.valueOf(failures.size()));
            values.put(RecapConstants.FAILURE_CAUSE, failures.get(0));
            values.put(RecapConstants.FAILED_BIBS, RecapConstants.FAILED_BIBS);
            values.put(RecapConstants.BATCH_EXPORT, RecapConstants.BATCH_EXPORT_FAILURE);
            values.put(RecapConstants.REQUEST_ID, requestId);

            FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
            fluentProducerTemplate
                    .to(RecapConstants.DATADUMP_FAILURE_REPORT_Q)
                    .withBody(values)
                    .withHeader(RecapConstants.BATCH_HEADERS, exchange.getIn().getHeader(RecapConstants.BATCH_HEADERS))
                    .withHeader(RecapConstants.EXPORT_FORMAT, exchange.getIn().getHeader(RecapConstants.EXPORT_FORMAT))
                    .withHeader(RecapConstants.TRANSMISSION_TYPE, exchange.getIn().getHeader(RecapConstants.TRANSMISSION_TYPE));
            fluentProducerTemplate.send();

        }
    }

    public ExecutorService getExecutorService() {
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(10);
        }
        if (executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(10);
        }
        return executorService;
    }

    public DataExportHeaderUtil getDataExportHeaderUtil() {
        if (null == dataExportHeaderUtil) {
            dataExportHeaderUtil = new DataExportHeaderUtil();
        }
        return dataExportHeaderUtil;
    }

    public void setDataExportHeaderUtil(DataExportHeaderUtil dataExportHeaderUtil) {
        this.dataExportHeaderUtil = dataExportHeaderUtil;
    }

}

