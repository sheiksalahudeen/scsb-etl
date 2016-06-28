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
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

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



        Random random = new Random();
        for(int i = 0; i < 10; i++){
            BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
            bibliographicEntity1.setBibliographicId(random.nextInt());
            bibliographicEntity1.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
            bibliographicEntity1.setOwningInstitutionId(1);
            bibliographicEntity1.setContent("mock content");
            bibliographicEntity1.setCreatedDate(new Date());

            HoldingsEntity holdingsEntity = new HoldingsEntity();
            holdingsEntity.setContent("mock holding content");
            holdingsEntity.setCreatedDate(new Date());
            ItemEntity itemEntity = new ItemEntity();
            itemEntity.setBarcode("b.123");
            itemEntity.setCollectionGroupId(1);
            itemEntity.setCreatedDate(new Date());
            itemEntity.setOwningInstitutionId(1);
            itemEntity.setCustomerCode("PA");
            itemEntity.setOwningInstitutionItemId("123");
            holdingsEntity.setItemEntities(Arrays.asList(itemEntity));
            bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));

            BibliographicEntity bibliographicEntity2 = new BibliographicEntity();
            bibliographicEntity2.setBibliographicId(random.nextInt());
            bibliographicEntity2.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
            bibliographicEntity2.setContent("mock conent");
            bibliographicEntity2.setCreatedDate(new Date());
            bibliographicEntity2.setOwningInstitutionId(1);
            bibliographicEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));

            producer.sendBody("activemq:queue:testQ", Arrays.asList(bibliographicEntity1, bibliographicEntity2));
        }

        Thread.sleep(1000);
    }
}
