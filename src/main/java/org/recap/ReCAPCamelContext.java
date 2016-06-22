package org.recap;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.route.ETLRouteBuilder;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class ReCAPCamelContext {
    private static ReCAPCamelContext recapCamelContext;
    private CamelContext context;


    private ReCAPCamelContext(CamelContext camelContext) {
     this.context = camelContext;
    }

    public static ReCAPCamelContext getInstance(CamelContext camelContext) {
        if (null == recapCamelContext) {
            recapCamelContext = new ReCAPCamelContext(camelContext);
        }
        return recapCamelContext;
    }

    public void addRoutes(RouteBuilder routeBuilder) throws Exception {
        context.addRoutes(routeBuilder);
    }

    public void addDynamicRoute(ReCAPCamelContext camelContext, String endPointFrom, int chunkSize) throws Exception {
        camelContext.addRoutes(new ETLRouteBuilder(context, endPointFrom, chunkSize));
    }

    public boolean isRunning() {
        return context.getStatus().isStarted();
    }
}
