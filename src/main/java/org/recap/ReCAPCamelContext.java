package org.recap;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.recap.repository.XmlRecordRepository;
import org.recap.route.JMSReportRouteBuilder;
import org.recap.route.XmlRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 21/7/16.
 */
@Component
public class ReCAPCamelContext {

    Logger logger = LoggerFactory.getLogger(ReCAPCamelContext.class);

    CamelContext context;
    XmlRecordRepository xmlRecordRepository;

    private String xmlTagName;
    private String inputDirectoryPath;
    private Integer poolSize;
    private Integer maxPoolSize;


    @Autowired
    public ReCAPCamelContext(CamelContext context, XmlRecordRepository xmlRecordRepository,
                             @Value("${etl.split.xml.tag.name}") String xmlTagName,
                             @Value("${etl.load.directory}") String inputDirectoryPath,
                             @Value("${etl.pool.size}") Integer poolSize, @Value("${etl.max.pool.size}") Integer maxPoolSize) {
        this.context = context;
        this.xmlRecordRepository = xmlRecordRepository;
        this.xmlTagName = xmlTagName;
        this.inputDirectoryPath = inputDirectoryPath;
        this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;
        init();
    }

    private void init() {
        try {
            addDynamicRoute();
        } catch (Exception e) {
            logger.error("Exception : " + e.getMessage());
        }
    }

    public void addRoutes(RouteBuilder routeBuilder) throws Exception {
        context.addRoutes(routeBuilder);
    }

    public void addDynamicRoute() throws Exception {
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        addRoutes(getXmlRouteBuilder());
        addRoutes(new JMSReportRouteBuilder());
    }

    public RouteBuilder getXmlRouteBuilder() {
        XmlRouteBuilder xmlRouteBuilder = new XmlRouteBuilder();
        xmlRouteBuilder.setXmlTagName(xmlTagName);
        xmlRouteBuilder.setInputDirectoryPath(inputDirectoryPath);
        xmlRouteBuilder.setPoolSize(poolSize);
        xmlRouteBuilder.setMaxPoolSize(maxPoolSize);
        xmlRouteBuilder.setXmlRecordRepository(xmlRecordRepository);
        return xmlRouteBuilder;
    }
}
