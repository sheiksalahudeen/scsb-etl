package org.recap.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.model.csv.ReCAPCSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by chenchulakshmig on 4/7/16.
 */

@Component
public class CSVFailureReportRouteBuilder {

    @Autowired
    public CSVFailureReportRouteBuilder(CamelContext context, @Value("${etl.report.directory}") String reportsDirectory) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("seda:csvQ")
                            .routeId("csvQ")
                            .process(new CSVFileNameProcessorForFileSystem())
                            .marshal().bindy(BindyType.Csv, ReCAPCSVRecord.class)
                            .to("file:" + reportsDirectory + File.separator + "?fileName=${in.header.fileName}-${in.header.reportType}-${date:now:ddMMMyyyy}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
