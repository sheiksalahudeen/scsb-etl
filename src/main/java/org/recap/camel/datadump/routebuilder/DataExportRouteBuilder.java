package org.recap.camel.datadump.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.FileFormatProcessorForDataExport;
import org.recap.camel.datadump.TransmissionTypeProcessorForDataExport;
import org.recap.camel.datadump.consumer.*;
import org.recap.camel.datadump.DataExportAggregator;
import org.recap.camel.datadump.DataExportPredicate;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.ReportDetailRepository;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;
import org.recap.util.XmlFormatter;
import org.recap.camel.datadump.consumer.DataExportReportActiveMQConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by peris on 11/5/16.
 */

public class DataExportRouteBuilder {

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
                    from(ReCAPConstants.SOLR_INPUT_FOR_DATA_EXPORT_Q)
                            .routeId(ReCAPConstants.SOLR_INPUT_DATA_EXPORT_ROUTE_ID)
                            .threads(20)
                            .bean(new BibEntityGeneratorActiveMQConsumer(bibliographicDetailsRepository), "processBibEntities");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    interceptFrom(ReCAPConstants.BIB_ENTITY_FOR_DATA_EXPORT_Q)
                            .process(new FileFormatProcessorForDataExport())
                            .process(new TransmissionTypeProcessorForDataExport());

                    from(ReCAPConstants.BIB_ENTITY_FOR_DATA_EXPORT_Q)
                            .routeId(ReCAPConstants.BIB_ENTITY_DATA_EXPORT_ROUTE_ID)
                            .threads(20)
                            .choice()
                                .when(header("exportFormat").isEqualTo(ReCAPConstants.DATADUMP_XML_FORMAT_MARC))
                                    .bean(new MarcRecordFormatActiveMQConsumer(marcXmlFormatterService), "processRecords")
                                    .to(ReCAPConstants.MARC_RECORD_FOR_DATA_EXPORT_Q)
                                .when(header("exportFormat").isEqualTo(ReCAPConstants.DATADUMP_XML_FORMAT_SCSB))
                                    .bean(new SCSBRecordFormatActiveMQConsumer(scsbXmlFormatterService), "processRecords")
                                .when(header("exportFormat").isEqualTo(ReCAPConstants.DATADUMP_DELETED_JSON_FORMAT))
                                    .bean(new DeletedRecordFormatActiveMQConsumer(deletedJsonFormatterService), "processRecords")
                                    .to(ReCAPConstants.DELETED_JSON_RECORD_FOR_DATA_EXPORT_Q)
                    ;

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    from(ReCAPConstants.MARC_RECORD_FOR_DATA_EXPORT_Q)
                            .routeId(ReCAPConstants.MARC_RECORD_DATA_EXPORT_ROUTE_ID)
                            .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(Integer.valueOf(dataDumpRecordsPerFile)))
                            .bean(new MarcXMLFormatActiveMQConsumer(marcXmlFormatterService), "processMarcXmlString")
                            .to(ReCAPConstants.DATADUMP_STAGING_Q);
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SCSB_RECORD_FOR_DATA_EXPORT_Q)
                            .routeId(ReCAPConstants.SCSB_RECORD_DATA_EXPORT_ROUTE_ID)
                            .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(Integer.valueOf(dataDumpRecordsPerFile)))
                            .bean(new SCSBXMLFormatActiveMQConsumer(scsbXmlFormatterService, xmlFormatter), "processSCSBXmlString")
                            .to(ReCAPConstants.DATADUMP_STAGING_Q);
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DELETED_JSON_RECORD_FOR_DATA_EXPORT_Q)
                            .routeId(ReCAPConstants.DELETED_JSON_RECORD_DATA_EXPORT_ROUTE_ID)
                            .aggregate(constant(true), new DataExportAggregator()).completionPredicate(new DataExportPredicate(Integer.valueOf(dataDumpRecordsPerFile)))
                            .bean(new DeletedJsonFormatActiveMQConsumer(deletedJsonFormatterService), "processDeleteJsonString")
                            .to(ReCAPConstants.DATADUMP_STAGING_Q);
                }
            });


            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DATADUMP_STAGING_Q)
                            .routeId(ReCAPConstants.DATADUMP_STAGING_ROUTE_ID)
                            .choice()
                                .when(header("transmissionType").isEqualTo(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP))
                                    .to(ReCAPConstants.DATADUMP_ZIPFILE_FTP_Q)
                                .when(header("transmissionType").isEqualTo(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_HTTP))
                                    .to(ReCAPConstants.DATADUMP_HTTP_Q);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
