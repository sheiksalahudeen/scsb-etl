package org.recap.camel.datadump.routebuilder;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.consumer.DataExportReportActiveMQConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by peris on 11/12/16.
 */
@Component
public class DataExportSuccessReportRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataExportSuccessReportRouteBuilder.class);

    @Autowired
    public DataExportSuccessReportRouteBuilder(CamelContext camelContext) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DATADUMP_SUCCESS_REPORT_Q)
                            .bean(DataExportReportActiveMQConsumer.class, "saveSuccessReportEntity");
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
