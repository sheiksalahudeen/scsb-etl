package org.recap.camel.activemq;

import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.camel.datadump.SolrSearchResultsProcessorForExport;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import static org.junit.Assert.*;

/**
 * Created by peris on 11/4/16.
 */
public class JmxHelperTest extends BaseTestCase {

    @Autowired
    JmxHelper jmxHelper;

    @Autowired
    private ProducerTemplate producer;


    @Test
    public void testGetMBean() throws Exception {

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("scsbactivemq:queue:solrInputForDataExportQ")
                        .to("mock:result");
            }
        });

        DestinationViewMBean mbView = jmxHelper.getBeanForQueueName("solrInputForDataExportQ");
        assertNotNull(mbView);
    }

}