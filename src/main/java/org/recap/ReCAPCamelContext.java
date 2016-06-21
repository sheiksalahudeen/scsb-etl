package org.recap;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.recap.route.ETLRouteBuilder;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class ReCAPCamelContext {
    private static ReCAPCamelContext recapCamelContext;
    private CamelContext context;


    private ReCAPCamelContext() {
        context = new DefaultCamelContext();
        try {
            context.start();
        } catch (Exception e) {
            System.out.println("Camel Context not initialized");
            e.printStackTrace();
        }
    }

    public static ReCAPCamelContext getInstance() {
        if (null == recapCamelContext) {
            recapCamelContext = new ReCAPCamelContext();
        }
        return recapCamelContext;
    }


    public void addRoutes(RouteBuilder routeBuilder) throws Exception {
        getContext().addRoutes(routeBuilder);
    }

    public void addDynamicBibRecordStreamRoute(ReCAPCamelContext camelContext, String endPointFrom, int chunkSize) throws Exception {
        camelContext.addRoutes(new ETLRouteBuilder(camelContext.getContext(), endPointFrom, chunkSize));
    }

    public CamelContext getContext() {
        return context;
    }

    public void setContext(CamelContext context) {
        this.context = context;
    }

    public void stopReCAPCamelContext(){
        try {
            context.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return context.getStatus().isStarted();
    }
}
