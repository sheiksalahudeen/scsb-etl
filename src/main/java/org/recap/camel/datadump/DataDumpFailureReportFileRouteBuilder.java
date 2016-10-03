package org.recap.camel.datadump;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.ReCAPConstants;
import org.recap.model.csv.DataDumpFailureReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by premkb on 01/10/16.
 */

@Component
public class DataDumpFailureReportFileRouteBuilder {

    @Autowired
    public DataDumpFailureReportFileRouteBuilder(CamelContext context, @Value("${etl.report.directory}") String reportsDirectory) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DATADUMP_FAILURE_REPORT_Q)
                            .routeId(ReCAPConstants.DATADUMP_FAILURE_CSV_ROUTE_ID)
                            .process(new FileNameProcessorForDataDumpFailure())
                            .marshal().bindy(BindyType.Csv, DataDumpFailureReport.class)
                            .to("file:" + reportsDirectory + File.separator + "?fileName=${in.header.fileName}-${in.header.reportType}-${date:now:ddMMMyyyy}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
