package org.recap.camel.datadump.consumer;

import com.google.common.collect.Lists;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.DefaultFluentProducerTemplate;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
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

    Logger logger = LoggerFactory.getLogger(MarcRecordFormatActiveMQConsumer.class);

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
            Collection<? extends Record> successRecords = (Collection<? extends Record>) results.get(ReCAPConstants.SUCCESS);
            if (!CollectionUtils.isEmpty(successRecords)) {
                records.addAll(successRecords);
            }
            Collection failureRecords = (Collection) results.get(ReCAPConstants.FAILURE);
            if (!CollectionUtils.isEmpty(failureRecords)) {
                failures.addAll(failureRecords);
            }
        }

        String batchHeaders = (String) exchange.getIn().getHeader("batchHeaders");
        String requestId = getDataExportHeaderUtil().getValueFor(batchHeaders, "requestId");
        processFailures(exchange, failures, batchHeaders, requestId);

        long endTime = System.currentTimeMillis();

        logger.info("Time taken to prepare " + bibliographicEntities.size() + " marc records : " + (endTime - startTime) / 1000 + " seconds ");

        fluentProducerTemplate
                .to(ReCAPConstants.MARC_RECORD_FOR_DATA_EXPORT_Q)
                .withBody(records)
                .withHeader("batchHeaders", exchange.getIn().getHeader("batchHeaders"))
                .withHeader("exportFormat", exchange.getIn().getHeader("exportFormat"))
                .withHeader("transmissionType", exchange.getIn().getHeader("transmissionType"));
        fluentProducerTemplate.send();
    }

    private void processFailures(Exchange exchange, List failures, String batchHeaders, String requestId) {
        if (!CollectionUtils.isEmpty(failures)) {
            HashMap values = new HashMap();
            values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.REQUESTING_INST_CODE));
            values.put(ReCAPConstants.INSTITUTION_CODES, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.INSTITUTION_CODES));
            values.put(ReCAPConstants.FETCH_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.FETCH_TYPE));
            values.put(ReCAPConstants.COLLECTION_GROUP_IDS, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.COLLECTION_GROUP_IDS));
            values.put(ReCAPConstants.TRANSMISSION_TYPE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TRANSMISSION_TYPE));
            values.put(ReCAPConstants.EXPORT_FROM_DATE, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FROM_DATE));
            values.put(ReCAPConstants.EXPORT_FORMAT, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.EXPORT_FORMAT));
            values.put(ReCAPConstants.TO_EMAIL_ID, getDataExportHeaderUtil().getValueFor(batchHeaders, ReCAPConstants.TO_EMAIL_ID));
            values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(failures.size()));
            values.put(ReCAPConstants.FAILURE_CAUSE, failures.get(0));
            values.put(ReCAPConstants.FAILED_BIBS, ReCAPConstants.FAILED_BIBS);
            values.put(ReCAPConstants.BATCH_EXPORT, ReCAPConstants.BATCH_EXPORT_FAILURE);
            values.put(ReCAPConstants.REQUEST_ID, requestId);

            FluentProducerTemplate fluentProducerTemplate = new DefaultFluentProducerTemplate(exchange.getContext());
            fluentProducerTemplate
                    .to(ReCAPConstants.DATADUMP_FAILURE_REPORT_Q)
                    .withBody(values)
                    .withHeader("batchHeaders", exchange.getIn().getHeader("batchHeaders"))
                    .withHeader("exportFormat", exchange.getIn().getHeader("exportFormat"))
                    .withHeader("transmissionType", exchange.getIn().getHeader("transmissionType"));
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

