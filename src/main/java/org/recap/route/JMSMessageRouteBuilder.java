package org.recap.route;

import org.apache.camel.builder.RouteBuilder;

/**
 * Created by rajeshbabuk on 30/6/16.
 */
public class JMSMessageRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("activemq:queue:etlLoadQ")
                .autoStartup(true)
                .routeId("jmsLoadRoute")
                .bean(JMSMessageProcessor.class, "processMessage");
    }
}
