package org.recap.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.RecapConstants;
import org.recap.model.csv.ReCAPCSVSuccessRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by angelind on 18/8/16.
 */
@Component
public class CSVSuccessReportRouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CSVSuccessReportRouteBuilder.class);

    /**
     * Instantiates a new Csv success report route builder.
     *
     * @param context          the context
     * @param reportsDirectory the reports directory
     */
    @Autowired
    public CSVSuccessReportRouteBuilder(CamelContext context, @Value("${etl.report.directory}") String reportsDirectory) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(RecapConstants.CSV_SUCCESS_Q)
                            .routeId(RecapConstants.CSV_SUCCESS_ROUTE_ID)
                            .process(new FileNameProcessorForSuccessRecord())
                            .marshal().bindy(BindyType.Csv, ReCAPCSVSuccessRecord.class)
                            .to("file:" + reportsDirectory + File.separator + "?fileName=${in.header.fileName}-${in.header.reportType}-${date:now:ddMMMyyyy}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            logger.error(RecapConstants.ERROR,e);
        }
    }
}
