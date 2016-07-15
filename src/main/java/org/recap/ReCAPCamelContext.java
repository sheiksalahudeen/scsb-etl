package org.recap;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.recap.repository.*;
import org.recap.route.FileRouteBuilder;
import org.recap.route.JMSMessageRouteBuilder;
import org.recap.route.JMSReportRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by pvsubrah on 6/21/16.
 */

@Component
public class ReCAPCamelContext {

    CamelContext context;
    BibliographicDetailsRepository bibliographicDetailsRepository;
    InstitutionDetailsRepository institutionDetailsRepository;
    ItemStatusDetailsRepository itemStatusDetailsRepository;
    CollectionGroupDetailsRepository collectionGroupDetailsRepository;
    ProducerTemplate producer;
    private Integer numberOfThreads;

    private Integer batchSize;
    private String inputDirectoryPath;
    private String jmsComponentName;
    private String jmsComponentUrl;
    private String xmlTagName;
    private String levelDbFilePath;

    @Autowired
    public ReCAPCamelContext(@Value("${etl.number.of.threads}") Integer numberOfThreads,
                             @Value("${etl.load.batchSize}") Integer batchSize,
                             @Value("${etl.load.directory}") String inputDirectoryPath,
                             @Value("${etl.jms.component.name}") String jmsComponentName,
                             @Value("${etl.jms.component.url}") String jmsComponentUrl,
                             @Value("${etl.split.xml.tag.name}") String xmlTagName,
                             @Value("${levelDb.filePath}") String levelDbFilePath,
                             CamelContext context,
                             BibliographicDetailsRepository bibliographicDetailsRepository,
                             InstitutionDetailsRepository institutionDetailsRepository,
                             ItemStatusDetailsRepository itemStatusDetailsRepository,
                             CollectionGroupDetailsRepository collectionGroupDetailsRepository,
                             ProducerTemplate producer) {
        this.numberOfThreads = numberOfThreads;
        this.batchSize = batchSize;
        this.inputDirectoryPath = inputDirectoryPath;
        this.jmsComponentName = jmsComponentName;
        this.jmsComponentUrl = jmsComponentUrl;
        this.xmlTagName = xmlTagName;
        this.levelDbFilePath = levelDbFilePath;
        this.context = context;
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
        this.institutionDetailsRepository = institutionDetailsRepository;
        this.itemStatusDetailsRepository = itemStatusDetailsRepository;
        this.collectionGroupDetailsRepository = collectionGroupDetailsRepository;
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
        context.addComponent(jmsComponentName, ActiveMQComponent.activeMQComponent(jmsComponentUrl));
        addRoutes(new JMSMessageRouteBuilder());
        addRoutes(new JMSReportRouteBuilder());
        addRoutes(getFileRouteBuilder());
    }

    private FileRouteBuilder getFileRouteBuilder() {
        FileRouteBuilder fileRouteBuilder = new FileRouteBuilder(context);
        fileRouteBuilder.setLevlDbFilePath(levelDbFilePath);
        fileRouteBuilder.setFrom(inputDirectoryPath);
        fileRouteBuilder.setChunkSize(batchSize);
        fileRouteBuilder.setXmlTagName(xmlTagName);
        fileRouteBuilder.setBibliographicDetailsRepository(bibliographicDetailsRepository);
        fileRouteBuilder.setInstitutionDetailsRepository(institutionDetailsRepository);
        fileRouteBuilder.setItemStatusDetailsRepository(itemStatusDetailsRepository);
        fileRouteBuilder.setCollectionGroupDetailsRepository(collectionGroupDetailsRepository);
        fileRouteBuilder.setProducer(producer);
        fileRouteBuilder.setMaxThreads(50);
        fileRouteBuilder.setPoolSize(numberOfThreads);
        return fileRouteBuilder;
    }

    public boolean isRunning() {
        return context.getStatus().isStarted();
    }
}
