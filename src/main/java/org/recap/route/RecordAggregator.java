package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class RecordAggregator implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            oldExchange = new DefaultExchange(newExchange);
            oldExchange.getIn().setHeaders(newExchange.getIn().getHeaders());
            List<Object> body = new ArrayList<>();
            oldExchange.getIn().setBody(body);
            oldExchange.getExchangeId();
        }
        oldExchange.getIn().getBody(List.class).add(newExchange.getIn().getBody());

        for (String key : newExchange.getProperties().keySet()) {
            oldExchange.setProperty(key, newExchange.getProperty(key));
        }

        return oldExchange;
    }
}
