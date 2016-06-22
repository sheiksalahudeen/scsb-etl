package org.recap;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.route.ETLRouteBuilder;
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

    public void addRoutes(RouteBuilder routeBuilder) throws Exception {
        context.addRoutes(routeBuilder);
    }

    public void addDynamicRoute(CamelContext camelContext, String endPointFrom, int chunkSize) throws Exception {
        ETLRouteBuilder etlRouteBuilder = new ETLRouteBuilder(camelContext);
        etlRouteBuilder.setFrom(endPointFrom);
        etlRouteBuilder.setChunkSize(chunkSize);
        etlRouteBuilder.setBibliographicDetailsRepository(bibliographicDetailsRepository);
        camelContext.addRoutes(etlRouteBuilder);
    }

    public boolean isRunning() {
        return context.getStatus().isStarted();
    }
}
