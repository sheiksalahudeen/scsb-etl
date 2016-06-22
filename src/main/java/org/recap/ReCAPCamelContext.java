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
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private CamelContext context;

    public void addRoutes(RouteBuilder routeBuilder) throws Exception {
        context.addRoutes(routeBuilder);
    }

    public void addDynamicRoute(ReCAPCamelContext camelContext, String endPointFrom, int chunkSize) throws Exception {
        camelContext.addRoutes(new ETLRouteBuilder(context, bibliographicDetailsRepository, endPointFrom, chunkSize));
    }

    public boolean isRunning() {
        return context.getStatus().isStarted();
    }
}
