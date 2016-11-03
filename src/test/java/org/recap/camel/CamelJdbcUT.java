package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.recap.BaseTestCase;

import org.recap.ReCAPConstants;
import org.recap.camel.datadump.SolrSearchResultsProcessorForExport;
import org.recap.model.search.SearchRecordsRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.XmlRecordRepository;
import org.recap.service.DataDumpSolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by peris on 7/17/16.
 */

public class CamelJdbcUT extends BaseTestCase {

    @Value("${etl.split.xml.tag.name}")
    String xmlTagName;

    @Value("${etl.pool.size}")
    Integer etlPoolSize;

    @Value("${etl.pool.size}")
    Integer etlMaxPoolSize;

    @Value("${etl.max.pool.size}")
    String inputDirectoryPath;

    @Autowired
    XmlRecordRepository xmlRecordRepository;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    DataDumpSolrService dataDumpSolrService;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private MarcRecordFormatProcessor marcRecordFormatProcessor;

    @Autowired
    private MarcXMLFormatProcessor marcXMLFormatProcessor;

    @Test
    public void parseXmlAndInsertIntoDb() throws Exception {


        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                FileEndpoint fileEndpoint = endpoint("file:" + inputDirectoryPath, FileEndpoint.class);
                fileEndpoint.setFilter(new XmlFileFilter());

                from(fileEndpoint)
                        .split()
                        .tokenizeXML(xmlTagName)
                        .streaming()
                        .threads(etlPoolSize, etlMaxPoolSize, "xmlProcessingThread")
                        .process(new XmlProcessor(xmlRecordRepository))
                        .to("jdbc:dataSource");
            }
        });

        java.lang.Thread.sleep(10000);
    }

    class XmlFileFilter implements GenericFileFilter {
        @Override
        public boolean accept(GenericFile file) {
            return FilenameUtils.getExtension(file.getAbsoluteFilePath()).equalsIgnoreCase("xml");
        }
    }


    @Test
    public void exportDataDump() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("scsbactivemq:queue:solrInputForDataExportQ?concurrentConsumers=10")
                        .process(new SolrSearchResultsProcessorForExport(bibliographicDetailsRepository))
                        .to("scsbactivemq:queue:bibEntityForDataExportQ");
            }
        });

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("scsbactivemq:queue:bibEntityForDataExportQ?concurrentConsumers=10")
                        .process(marcRecordFormatProcessor)
                        .to("scsbactivemq:queue:MarcRecordForDataExportQ");

            }
        });

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("scsbactivemq:queue:MarcRecordForDataExportQ?concurrentConsumers=10")
                        .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(50000))
                        .process(marcXMLFormatProcessor)
                        .to(ReCAPConstants.DATADUMP_FILE_SYSTEM_Q);

            }
        });

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(Arrays.asList("CUL"));
        searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList("Shared"));
        searchRecordsRequest.setPageSize(10000);
        Map results = dataDumpSolrService.getResults(searchRecordsRequest);
        String fileName = "PUL"+ File.separator+getDateTimeString()+File.separator+ReCAPConstants.DATA_DUMP_FILE_NAME+ "PUL"+0;
        producer.sendBodyAndHeader("scsbactivemq:queue:solrInputForDataExportQ", results, "fileName", fileName);

        Integer totalPageCount = (Integer) results.get("totalPageCount");
        for(int pageNum = 1; pageNum < totalPageCount; pageNum++){
            searchRecordsRequest.setPageNumber(pageNum);
            dataDumpSolrService.getResults(searchRecordsRequest);
            fileName = "PUL"+ File.separator+new Date()+File.separator+ReCAPConstants.DATA_DUMP_FILE_NAME+ "PUL"+pageNum;
            producer.sendBodyAndHeader("scsbactivemq:queue:solrInputForDataExportQ", results, "fileName", fileName);
        }

        producer.sendBodyAndHeader("scsbactivemq:queue:MarcRecordForDataExportQ", null, "batchComplete", true);

        while (true) {

        }
    }

    public class DataExportAggregator implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                oldExchange = new DefaultExchange(newExchange);
                oldExchange.getIn().setHeaders(newExchange.getIn().getHeaders());
                List<Object> body = new ArrayList<>();
                oldExchange.getIn().setBody(body);
                oldExchange.getExchangeId();
            }
            List body = (List) newExchange.getIn().getBody();
            List oldBody = oldExchange.getIn().getBody(List.class);
            if (null!= oldBody && null!= body) {
                oldBody.addAll(body);
                Object oldBatchSize = oldExchange.getIn().getHeader("batchSize");
                Integer newBatchSize = 0;
                if(null != oldBatchSize){
                    newBatchSize= body.size() + (Integer)oldBatchSize;
                } else {
                    newBatchSize = body.size();
                }
                oldExchange.getIn().setHeader("batchSize", newBatchSize);

                for (String key : newExchange.getProperties().keySet()) {
                    oldExchange.setProperty(key, newExchange.getProperty(key));
                }
            }



            return oldExchange;
        }
    }

    public class DataExportPredicate implements Predicate {

        private Integer batchSize;

        public DataExportPredicate(Integer batchSize) {
            this.batchSize = batchSize;
        }

        @Override
        public boolean matches(Exchange exchange) {
           Integer batchSize = (Integer) exchange.getIn().getHeader("batchSize");
            boolean batchComplete = null!= exchange.getIn().getHeader("batchComplete") ? exchange.getIn().getHeader("batchComplete").equals(true) : false;
            if(this.batchSize.equals(batchSize) || batchComplete){
               exchange.getIn().setHeader("batchSize", 0);
               return true;
           }
           return false;
        }
    }

    private String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);
    }
}
