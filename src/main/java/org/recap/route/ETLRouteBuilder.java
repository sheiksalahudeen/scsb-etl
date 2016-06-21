package org.recap.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.model.AggregateDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.SplitDefinition;
import org.apache.commons.io.FilenameUtils;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class ETLRouteBuilder extends RouteBuilder{
    private final String from;
    private FileEndpoint fEPoint = null;
    private final int BULK_INGEST_POLL_INTERVAL = 1500;
    private int chunkSize = 100;

    public ETLRouteBuilder(CamelContext context, String from, int chunkSize) {
        super(context);
        this.from = from;
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
        aggregator.completionPredicate(new SplitPredicate(chunkSize));
        aggregator.process(new RecordProcessor());
    }

    public class FileFilter implements GenericFileFilter {
        @Override
        public boolean accept(GenericFile file) {
            return FilenameUtils.getExtension(file.getAbsoluteFilePath()).equalsIgnoreCase("xml");
        }
    }

}
