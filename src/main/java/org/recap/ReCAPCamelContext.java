package org.recap;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.recap.repository.*;
import org.recap.route.ETLRouteBuilder;
import org.recap.route.JMSMessageRouteBuilder;
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

    @Autowired
    public ReCAPCamelContext(@Value("${etl.number.of.threads}") Integer numberOfThreads,
                             @Value("${etl.load.batchSize}") Integer batchSize,
                             @Value("${etl.load.directory}") String inputDirectoryPath,
                             CamelContext context,
                             BibliographicDetailsRepository bibliographicDetailsRepository,
                             InstitutionDetailsRepository institutionDetailsRepository,
                             ItemStatusDetailsRepository itemStatusDetailsRepository,
                             CollectionGroupDetailsRepository collectionGroupDetailsRepository,
                             ProducerTemplate producer) {
        this.numberOfThreads = numberOfThreads;
        this.batchSize = batchSize;
        this.inputDirectoryPath = inputDirectoryPath;
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
        context.addComponent("activemq", ActiveMQComponent.activeMQComponent("vm://localhost?broker.persistent=false"));
        addRoutes(new JMSMessageRouteBuilder());
        addRoutes(getEtlRouteBuilder());
    }

    private ETLRouteBuilder getEtlRouteBuilder() {
        ETLRouteBuilder etlRouteBuilder = new ETLRouteBuilder(context);
        etlRouteBuilder.setFrom(inputDirectoryPath);
        etlRouteBuilder.setChunkSize(batchSize);
        etlRouteBuilder.setBibliographicDetailsRepository(bibliographicDetailsRepository);
        etlRouteBuilder.setInstitutionDetailsRepository(institutionDetailsRepository);
        etlRouteBuilder.setItemStatusDetailsRepository(itemStatusDetailsRepository);
        etlRouteBuilder.setCollectionGroupDetailsRepository(collectionGroupDetailsRepository);
        etlRouteBuilder.setProducer(producer);
        etlRouteBuilder.setMaxThreads(50);
        etlRouteBuilder.setPoolSize(numberOfThreads);
        return etlRouteBuilder;
    }

    public boolean isRunning() {
        return context.getStatus().isStarted();
    }
}
