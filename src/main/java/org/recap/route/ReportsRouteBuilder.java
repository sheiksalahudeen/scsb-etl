package org.recap.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by peris on 8/12/16.
 */

@Component
public class ReportsRouteBuilder {
    Logger logger = LoggerFactory.getLogger(ReportsRouteBuilder.class);

    @Autowired
    public ReportsRouteBuilder(CamelContext camelContext, ReportProcessor reportProcessor) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.REPORT_Q)
                            .routeId(ReCAPConstants.REPORT_ROUTE_ID)
                            .process(reportProcessor);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
