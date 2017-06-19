package org.recap.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.RecapConstants;
import org.recap.model.csv.ReCAPCSVFailureRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
@Component
public class CSVFailureReportRouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CSVFailureReportRouteBuilder.class);

    /**
     * Instantiates a new Csv failure report route builder.
     *
     * @param context          the context
     * @param reportsDirectory the reports directory
     */
    @Autowired
    public CSVFailureReportRouteBuilder(CamelContext context, @Value("${etl.report.directory}") String reportsDirectory) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.CSV_FAILURE_Q)
                            .routeId(RecapConstants.CSV_FAILURE_ROUTE_ID)
                            .process(new FileNameProcessorForFailureRecord())
                            .marshal().bindy(BindyType.Csv, ReCAPCSVFailureRecord.class)
                            .to("file:" + reportsDirectory + File.separator + "?fileName=${in.header.fileName}-${in.header.reportType}-${date:now:ddMMMyyyy}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            logger.error(RecapConstants.ERROR,e);
        }
    }
}