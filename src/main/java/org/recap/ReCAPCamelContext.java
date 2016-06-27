package org.recap;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.BibliographicHoldingsDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.route.ETLRouteBuilder;
import org.recap.route.JMSMessageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by pvsubrah on 6/21/16.
 */

@Component
public class ReCAPCamelContext {

    @Autowired
    private CamelContext context;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    ProducerTemplate producer;

    public void addRoutes(RouteBuilder routeBuilder) throws Exception {
        context.addRoutes(routeBuilder);
    }

    public void addDynamicRoute(CamelContext camelContext, String endPointFrom, int chunkSize, int numThreads) throws Exception {
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=true"));
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:queue:testQ")
                        .bean(JMSMessageProcessor.class,"processMessage");
            }
        });

        ETLRouteBuilder etlRouteBuilder = new ETLRouteBuilder(camelContext);
        etlRouteBuilder.setFrom(endPointFrom);
        etlRouteBuilder.setChunkSize(chunkSize);
        etlRouteBuilder.setBibliographicDetailsRepository(bibliographicDetailsRepository);
        etlRouteBuilder.setInstitutionDetailsRepository(institutionDetailsRepository);
        etlRouteBuilder.setProducer(producer);
        etlRouteBuilder.setMaxThreads(50);
        etlRouteBuilder.setPoolSize(numThreads);
        camelContext.addRoutes(etlRouteBuilder);
    }

    public boolean isRunning() {
        return context.getStatus().isStarted();
    }
}
