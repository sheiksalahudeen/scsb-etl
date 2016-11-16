package org.recap.camel.dynamicRouter;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.recap.camel.datadump.routebuilder.DataExportRouteBuilder;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.service.formatter.datadump.DeletedJsonFormatterService;
import org.recap.service.formatter.datadump.MarcXmlFormatterService;
import org.recap.service.formatter.datadump.SCSBXmlFormatterService;
import org.recap.util.XmlFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 15/11/16.
 */
@Component
public class DynamicRouteBuilder {

    @Autowired
    CamelContext camelContext;
    @Autowired
    ProducerTemplate producerTemplate;
    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;
    @Autowired
    MarcXmlFormatterService marcXmlFormatterService;
    @Autowired
    SCSBXmlFormatterService scsbXmlFormatterService;
    @Autowired
    DeletedJsonFormatterService deletedJsonFormatterService;
    @Autowired
    XmlFormatter xmlFormatter;
    @Value("${datadump.records.per.file}")
    String dataDumpRecordsPerFile;

    public void addDataDumpExportRoutes() {
        new DataExportRouteBuilder(camelContext, producerTemplate, bibliographicDetailsRepository, marcXmlFormatterService, scsbXmlFormatterService,
                deletedJsonFormatterService, xmlFormatter, dataDumpRecordsPerFile);
    }
}
