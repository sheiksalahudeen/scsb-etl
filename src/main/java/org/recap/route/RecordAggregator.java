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
            List<Object> body = new ArrayList<>();
            body.add(newExchange.getIn().getBody());
            newExchange.getIn().setBody(body);
            return newExchange;
        }

        List body = oldExchange.getIn().getBody(List.class);
        Object newBody = newExchange.getIn().getBody();
        body.add(newBody);

        for (String key : newExchange.getProperties().keySet()) {
            oldExchange.setProperty(key, newExchange.getProperty(key));
        }

        return oldExchange;
    }
}
