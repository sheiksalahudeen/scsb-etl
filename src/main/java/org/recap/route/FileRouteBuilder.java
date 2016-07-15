package org.recap.route;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.model.AggregateDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.model.ThreadsDefinition;
import org.apache.commons.io.FilenameUtils;
import org.recap.repository.*;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class FileRouteBuilder extends RouteBuilder {
    private String from = null;
    private FileEndpoint fEPoint = null;
    private int chunkSize = 1;
    private int poolSize = 10;
    private int maxThreads = 10;
    private String xmlTagName;
    private InstitutionDetailsRepository institutionDetailsRepository;
    private ItemStatusDetailsRepository itemStatusDetailsRepository;
    private BibliographicDetailsRepository bibliographicDetailsRepository;
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;
    private ProducerTemplate producer;
    private String levlDbFilePath;

    public String getLevlDbFilePath() {
        return levlDbFilePath;
    }

    public void setLevlDbFilePath(String levlDbFilePath) {
        this.levlDbFilePath = levlDbFilePath;
    }

    public FileRouteBuilder(CamelContext context) {
        super(context);
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public String getXmlTagName() {
        return xmlTagName;
    }

    public void setXmlTagName(String xmlTagName) {
        this.xmlTagName = xmlTagName;
    }

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public void setBibliographicDetailsRepository(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    public ItemStatusDetailsRepository getItemStatusDetailsRepository() {
        return itemStatusDetailsRepository;
    }

    public void setItemStatusDetailsRepository(ItemStatusDetailsRepository itemStatusDetailsRepository) {
        this.itemStatusDetailsRepository = itemStatusDetailsRepository;
    }

    public CollectionGroupDetailsRepository getCollectionGroupDetailsRepository() {
        return collectionGroupDetailsRepository;
    }

    public void setCollectionGroupDetailsRepository(CollectionGroupDetailsRepository collectionGroupDetailsRepository) {
        this.collectionGroupDetailsRepository = collectionGroupDetailsRepository;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public FileEndpoint getfEPoint() {
        return fEPoint;
    }


    public int getChunkSize() {
        return chunkSize;
    }

    public void setfEPoint(FileEndpoint fEPoint) {
        this.fEPoint = fEPoint;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public void configure() throws Exception {
        fEPoint = endpoint(
                "file:" + from + "?move=.done",
                FileEndpoint.class);
        fEPoint.setFilter(new FileFilter());

        RouteDefinition route = from(fEPoint);
        route.setId("fileRoute");

        SplitDefinition split = route.split().tokenizeXML(xmlTagName);
        split.streaming();

        AggregateDefinition aggregator = split.aggregate(constant(true), new RecordAggregator()).aggregationRepository(new ReCAPJDBCAggregationRepository("etl-repo", levlDbFilePath));
        aggregator.setParallelProcessing(true);
        aggregator.completionPredicate(new SplitPredicate(chunkSize));
        RecordProcessor processor = new RecordProcessor();
        processor.setBibliographicDetailsRepository(bibliographicDetailsRepository);
        processor.setInstitutionDetailsRepository(institutionDetailsRepository);
        processor.setItemStatusDetailsRepository(itemStatusDetailsRepository);
        processor.setCollectionGroupDetailsRepository(collectionGroupDetailsRepository);
        processor.setProducer(producer);

        ThreadsDefinition threads = aggregator.threads(poolSize, maxThreads);

        threads.process(processor);
        threads.setThreadName("etlProcessingThread");
    }

    public void setProducer(ProducerTemplate producer) {
        this.producer = producer;
    }

    public class FileFilter implements GenericFileFilter {
        @Override
        public boolean accept(GenericFile file) {
            return FilenameUtils.getExtension(file.getAbsoluteFilePath()).equalsIgnoreCase("xml");
        }
    }

}
