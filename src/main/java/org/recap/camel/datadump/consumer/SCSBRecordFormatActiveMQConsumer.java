package org.recap.camel.datadump.consumer;

import com.google.common.collect.Lists;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.DataExportHeaderUtil;
import org.recap.camel.datadump.callable.BibRecordPreparerCallable;
import org.recap.camel.datadump.callable.MarcRecordPreparerCallable;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by peris on 11/1/16.
 */

public class SCSBRecordFormatActiveMQConsumer {

    Logger logger = LoggerFactory.getLogger(SCSBRecordFormatActiveMQConsumer.class);

    SCSBXmlFormatterService scsbXmlFormatterService;
    private ExecutorService executorService;
    private DataExportHeaderUtil dataExportHeaderUtil;
    private ProducerTemplate producerTemplate;

    public SCSBRecordFormatActiveMQConsumer(ProducerTemplate producerTemplate, SCSBXmlFormatterService scsbXmlFormatterService) {
        this.scsbXmlFormatterService = scsbXmlFormatterService;
        this.producerTemplate = producerTemplate;
    }

    public List<BibRecord> processRecords(Exchange exchange) throws Exception {
        List<BibRecord> records = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        List<BibliographicEntity> bibliographicEntities = (List<BibliographicEntity>) exchange.getIn().getBody();

        List<Callable<Map<String, Object>>> callables = new ArrayList<>();

        List<List<BibliographicEntity>> partitionList = Lists.partition(bibliographicEntities, 1000);

        for (Iterator<List<BibliographicEntity>> iterator = partitionList.iterator(); iterator.hasNext(); ) {
            List<BibliographicEntity> bibliographicEntityList = iterator.next();
            BibRecordPreparerCallable scsbRecordPreparerCallable =
                    new BibRecordPreparerCallable(bibliographicEntityList, scsbXmlFormatterService);
            callables.add(scsbRecordPreparerCallable);
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
            Collection<? extends BibRecord> successRecords = (Collection<? extends BibRecord>) results.get(ReCAPConstants.SUCCESS);
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
        processFailures(failures, batchHeaders, requestId);

        long endTime = System.currentTimeMillis();

        logger.info("Time taken to prepare " + bibliographicEntities.size() + " marc records : " + (endTime - startTime) / 1000 + " seconds ");

        return records;
    }

    private void processFailures(List failures, String batchHeaders, String requestId) {
        if (!CollectionUtils.isEmpty(failures)) {
            HashMap values = new HashMap();
            values.put(ReCAPConstants.REQUESTING_INST_CODE, getDataExportHeaderUtil().getValueFor(batchHeaders, "requestingInstitutionCode"));
            values.put(ReCAPConstants.NUM_RECORDS, String.valueOf(failures.size()));
            values.put(ReCAPConstants.FAILURE_CAUSE, failures.get(0));
            values.put(ReCAPConstants.BATCH_EXPORT, "Batch Export");
            values.put(ReCAPConstants.REQUEST_ID, requestId);

            producerTemplate.sendBody(ReCAPConstants.DATADUMP_FAILURE_REPORT_Q, values);
        }
    }

    public ExecutorService getExecutorService() {
        if (null == executorService) {
            executorService = Executors.newFixedThreadPool(500);
        }
        if (executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(500);
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

