package org.recap.camel;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

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
    private MarcXmlDataFormatProcessor marcXmlDataFormatProcessor;

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
                from("scsbactivemq:queue:bibEntityForDataExportQ?destination.consumer.prefetchSize=200")
                        .process(marcXmlDataFormatProcessor)
                        .to(ReCAPConstants.DATADUMP_FILE_SYSTEM_Q);

            }
        });

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("scsbactivemq:queue:solrInputForDataExportQ")
                        .process(new SolrSearchResultsProcessorForExport(bibliographicDetailsRepository, producer));
            }
        });

        SearchRecordsRequest searchRecordsRequest = new SearchRecordsRequest();
        searchRecordsRequest.setOwningInstitutions(Arrays.asList("CUL"));
        searchRecordsRequest.setCollectionGroupDesignations(Arrays.asList("Shared"));
        searchRecordsRequest.setPageSize(100);
        Map results = dataDumpSolrService.getResults(searchRecordsRequest);
        producer.sendBody("scsbactivemq:queue:solrInputForDataExportQ", results);

        Thread.sleep(20000);
    }
}
