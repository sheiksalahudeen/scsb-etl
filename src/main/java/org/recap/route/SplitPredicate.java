package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class SplitPredicate implements Predicate {
    private Logger logger = LoggerFactory.getLogger(SplitPredicate.class);
    private Integer batchSize = 1000;

    public SplitPredicate(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public boolean matches(Exchange exchange) {

        Object camelSplitComplete = exchange.getProperty("CamelSplitComplete");
        if (camelSplitComplete != null && Boolean.TRUE
                .equals(camelSplitComplete)) {
            System.out.println("Processing End Of File: " + exchange.getProperty("CamelFileExchangeFile"));
            return true;
        }
        if (exchange.getProperty("CamelAggregatedSize").equals(batchSize)) {
            return true;
        }

        return false;
    }
}
