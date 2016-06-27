package org.recap.route;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.apache.camel.util.jndi.JndiContext;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.jpa.BibliographicEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by pvsubrah on 6/24/16.
 */
public class JMSTest extends BaseTestCase {

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    ConsumerTemplate consumer;

    @Autowired
    CamelContext camelContext;

    @Test
    public void produceAndConsumeSEDA() throws Exception {
        assertNotNull(producer);
        assertNotNull(consumer);

        producer.sendBody("seda:start", "Hello World");

        String body = consumer.receiveBody("seda:start", String.class);

        assertEquals("Hello World", body);

    }

    @Test
    public void produceAndConsumeActiveMQQueue() throws Exception {

        camelContext.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:queue:testQ?asyncConsumer=true&concurrentConsumers=10")
                        .bean(JMSMessageProcessor.class,"processMessage");
            }
        });

        for(int i = 0; i < 10; i++){
            BibliographicEntity bibliographicEntity = new BibliographicEntity();
            bibliographicEntity.setBibliographicId(i);
            producer.sendBody("activemq:queue:testQ", "Hello World");
        }

        Thread.sleep(1000);
    }
}
