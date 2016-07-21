package org.recap;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.recap.route.JMSReportRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 21/7/16.
 */
@Component
public class ReCAPCamelContext {

    CamelContext context;

    ProducerTemplate producer;

    @Autowired
    public ReCAPCamelContext(CamelContext context, ProducerTemplate producer) {
        this.context = context;
        this.producer = producer;
        init();
    }

    private void init() {
        try {
            addDynamicRoute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addRoutes(RouteBuilder routeBuilder) throws Exception {
        context.addRoutes(routeBuilder);
    }

    public void addDynamicRoute() throws Exception {
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        addRoutes(new JMSReportRouteBuilder());
    }
}
