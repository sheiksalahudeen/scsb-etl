package org.recap.camel.datadump.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.RecapConstants;
import org.recap.camel.datadump.DataExportAggregator;
import org.recap.camel.datadump.DataExportPredicate;
import org.recap.camel.datadump.FileFormatProcessorForDataExport;
import org.recap.camel.datadump.TransmissionTypeProcessorForDataExport;
import org.recap.camel.datadump.consumer.*;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;
import org.recap.util.XmlFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by peris on 11/5/16.
 */

public class DataExportRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataExportRouteBuilder.class);

    public DataExportRouteBuilder(CamelContext camelContext,
                                  BibliographicDetailsRepository bibliographicDetailsRepository,
                                  MarcXmlFormatterService marcXmlFormatterService,
                                  SCSBXmlFormatterService scsbXmlFormatterService,
                                  DeletedJsonFormatterService deletedJsonFormatterService,
                                  XmlFormatter xmlFormatter,
                                  @Value("${datadump.records.per.file}") String dataDumpRecordsPerFile) {
        try {

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.SOLR_INPUT_FOR_DATA_EXPORT_Q)
                            .routeId(RecapConstants.SOLR_INPUT_DATA_EXPORT_ROUTE_ID)
                            .threads(20)
                            .bean(new BibEntityGeneratorActiveMQConsumer(bibliographicDetailsRepository), "processBibEntities");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    interceptFrom(RecapConstants.BIB_ENTITY_FOR_DATA_EXPORT_Q)
                            .process(new FileFormatProcessorForDataExport())
                            .process(new TransmissionTypeProcessorForDataExport());

                    from(RecapConstants.BIB_ENTITY_FOR_DATA_EXPORT_Q)
                            .routeId(RecapConstants.BIB_ENTITY_DATA_EXPORT_ROUTE_ID)
                            .threads(20)
                            .choice()
                                .when(header(RecapConstants.EXPORT_FORMAT).isEqualTo(RecapConstants.DATADUMP_XML_FORMAT_MARC))
                                    .bean(new MarcRecordFormatActiveMQConsumer(marcXmlFormatterService), RecapConstants.PROCESS_RECORDS)
                                .when(header(RecapConstants.EXPORT_FORMAT).isEqualTo(RecapConstants.DATADUMP_XML_FORMAT_SCSB))
                                    .bean(new SCSBRecordFormatActiveMQConsumer(scsbXmlFormatterService), RecapConstants.PROCESS_RECORDS)
                                .when(header(RecapConstants.EXPORT_FORMAT).isEqualTo(RecapConstants.DATADUMP_DELETED_JSON_FORMAT))
                                    .bean(new DeletedRecordFormatActiveMQConsumer(deletedJsonFormatterService), RecapConstants.PROCESS_RECORDS)
                    ;

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    from(RecapConstants.MARC_RECORD_FOR_DATA_EXPORT_Q)
                            .routeId(RecapConstants.MARC_RECORD_DATA_EXPORT_ROUTE_ID)
                            .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(Integer.valueOf(dataDumpRecordsPerFile)))
                            .bean(new MarcXMLFormatActiveMQConsumer(marcXmlFormatterService), "processMarcXmlString")
                            .to(RecapConstants.DATADUMP_STAGING_Q);
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.SCSB_RECORD_FOR_DATA_EXPORT_Q)
                            .routeId(RecapConstants.SCSB_RECORD_DATA_EXPORT_ROUTE_ID)
                            .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(Integer.valueOf(dataDumpRecordsPerFile)))
                            .bean(new SCSBXMLFormatActiveMQConsumer(scsbXmlFormatterService, xmlFormatter), "processSCSBXmlString")
                            .to(RecapConstants.DATADUMP_STAGING_Q);
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.DELETED_JSON_RECORD_FOR_DATA_EXPORT_Q)
                            .routeId(RecapConstants.DELETED_JSON_RECORD_DATA_EXPORT_ROUTE_ID)
                            .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(Integer.valueOf(dataDumpRecordsPerFile)))
                            .bean(new DeletedJsonFormatActiveMQConsumer(deletedJsonFormatterService), "processDeleteJsonString")
                            .to(RecapConstants.DATADUMP_STAGING_Q);
                }
            });


            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.DATADUMP_STAGING_Q)
                            .routeId(RecapConstants.DATADUMP_STAGING_ROUTE_ID)
                            .choice()
                                .when(header("transmissionType").isEqualTo(RecapConstants.DATADUMP_TRANSMISSION_TYPE_FTP))
                                    .to(RecapConstants.DATADUMP_ZIPFILE_FTP_Q)
                                .when(header("transmissionType").isEqualTo(RecapConstants.DATADUMP_TRANSMISSION_TYPE_HTTP))
                                    .to(RecapConstants.DATADUMP_HTTP_Q);
                }
            });

        } catch (Exception e) {
            logger.error(RecapConstants.ERROR,e);
        }
    }
}
