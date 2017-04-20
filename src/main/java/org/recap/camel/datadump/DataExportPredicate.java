package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.recap.util.datadump.BatchCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by peris on 11/5/16.
 */
public class DataExportPredicate implements Predicate {
    private static final Logger logger = LoggerFactory.getLogger(DataExportPredicate.class);
    private Integer batchSize;

    public DataExportPredicate(Integer batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public boolean matches(Exchange exchange) {
        Integer batchSize = (Integer) exchange.getIn().getHeader("batchSize");

        Integer totalPageCount = BatchCounter.getTotalPages();
        Integer currentPageCount = BatchCounter.getCurrentPage();

        logger.info("Total page count: {} and Current page count: {}, configured batch size-> {}, current batch size-> {}" , totalPageCount ,  currentPageCount,this.batchSize,batchSize);

        if (this.batchSize.equals(batchSize) || batchSize > this.batchSize || (currentPageCount.equals(totalPageCount))) {
            exchange.getIn().setHeader("batchSize", 0);
            return true;
        }
        return false;
    }
}
