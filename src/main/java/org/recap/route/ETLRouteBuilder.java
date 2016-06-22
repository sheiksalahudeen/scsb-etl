package org.recap.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.model.AggregateDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.model.ThreadsDefinition;
import org.apache.commons.io.FilenameUtils;
import org.recap.model.BibliographicEntityGenerator;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.BibliographicHoldingsDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class ETLRouteBuilder extends RouteBuilder {
    private String from = null;
    private FileEndpoint fEPoint = null;
    private int chunkSize = 1;
    private BibliographicHoldingsDetailsRepository bibliographicHoldingsDetailsRepository;
    private InstitutionDetailsRepository institutionDetailsRepository;
    private int poolSize = 10;
    private int maxThreads = 10;
    private int numThreads;

    public ETLRouteBuilder(CamelContext context) {
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

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    public BibliographicHoldingsDetailsRepository getBibliographicHoldingsDetailsRepository() {
        return bibliographicHoldingsDetailsRepository;
    }

    public void setBibliographicHoldingsDetailsRepository(BibliographicHoldingsDetailsRepository bibliographicHoldingsDetailsRepository) {
        this.bibliographicHoldingsDetailsRepository = bibliographicHoldingsDetailsRepository;
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
        System.out.println("Loading Bulk Ingest Process: @" + from);
        fEPoint = endpoint(
                "file:" + from + "?move=.done",
                FileEndpoint.class);
        fEPoint.setFilter(new FileFilter());
        RouteDefinition route = from(fEPoint);
        route.setId(from);
        SplitDefinition split = route.split().tokenizeXML("bibRecord");
        split.streaming();
        AggregateDefinition aggregator = split.aggregate(constant(true), new RecordAggregator());
        aggregator.setParallelProcessing(true);
        aggregator.completionPredicate(new SplitPredicate(chunkSize));
        RecordProcessor processor = new RecordProcessor();
        processor.setBibliographicHoldingsDetailsRepository(bibliographicHoldingsDetailsRepository);
        processor.setInstitutionDetailsRepository(institutionDetailsRepository);
        ThreadsDefinition threads = aggregator.threads(poolSize, maxThreads);
        threads.process(processor);
        threads.setThreadName("etlProcessingThread");
    }

    public class FileFilter implements GenericFileFilter {
        @Override
        public boolean accept(GenericFile file) {
            return FilenameUtils.getExtension(file.getAbsoluteFilePath()).equalsIgnoreCase("xml");
        }
    }

}
