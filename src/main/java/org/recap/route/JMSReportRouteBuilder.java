package org.recap.route;

import org.apache.camel.builder.RouteBuilder;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
public class JMSReportRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("activemq:queue:etlReportQ")
                .autoStartup(true)
                .bean(JMSReportProcessor.class, "processReport");
    }
}
