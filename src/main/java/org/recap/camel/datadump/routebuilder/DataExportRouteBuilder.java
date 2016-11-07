package org.recap.camel.datadump.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.recap.camel.DataDumpReportFtpRouteBuilder;
import org.recap.camel.datadump.consumer.MarcRecordFormatActiveMQConsumer;
import org.recap.camel.datadump.consumer.MarcXMLFormatActiveMQConsumer;
import org.recap.camel.datadump.DataExportAggregator;
import org.recap.camel.datadump.DataExportPredicate;
import org.recap.camel.datadump.consumer.SolrSearchResultsProcessorActiveMQConsumer;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by peris on 11/5/16.
 */
@Component
public class DataExportRouteBuilder {

    @Autowired
    public DataExportRouteBuilder(CamelContext camelContext,
                                  BibliographicDetailsRepository bibliographicDetailsRepository,
                                  MarcXmlFormatterService marcXmlFormatterService) {
        try {

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SOLR_INPUT_FOR_DATA_EXPORT_Q)
                            .bean(new SolrSearchResultsProcessorActiveMQConsumer(bibliographicDetailsRepository), "processBibEntities")
                            .to(ReCAPConstants.BIB_ENTITY_FOR_DATA_EXPORT_Q);
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.BIB_ENTITY_FOR_DATA_EXPORT_Q)
                            .bean(new MarcRecordFormatActiveMQConsumer(marcXmlFormatterService), "processRecords")
                            .to(ReCAPConstants.MARC_RECORD_FOR_DATA_EXPORT_Q);

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.MARC_RECORD_FOR_DATA_EXPORT_Q)
                            .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(4))
                            .bean(new MarcXMLFormatActiveMQConsumer(marcXmlFormatterService), "processMarcXmlString")
                            .to(ReCAPConstants.DATADUMP_ZIPFILE_FTP_Q);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
