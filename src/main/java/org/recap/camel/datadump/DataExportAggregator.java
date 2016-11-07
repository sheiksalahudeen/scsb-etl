package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by peris on 11/5/16.
 */
public class DataExportAggregator implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            oldExchange = new DefaultExchange(newExchange);
            oldExchange.getIn().setHeaders(newExchange.getIn().getHeaders());
            List<Object> body = new ArrayList<>();
            oldExchange.getIn().setBody(body);
            oldExchange.getExchangeId();
        }
        List body = (List) newExchange.getIn().getBody();
        List oldBody = oldExchange.getIn().getBody(List.class);
        if (null != oldBody && null != body) {
            oldBody.addAll(body);
            Object oldBatchSize = oldExchange.getIn().getHeader("batchSize");
            Integer newBatchSize = 0;
            if (null != oldBatchSize) {
                newBatchSize = body.size() + (Integer) oldBatchSize;
            } else {
                newBatchSize = body.size();
            }
            oldExchange.getIn().setHeader("batchSize", newBatchSize);

            Map<String, Object> headersForNewExchange = newExchange.getIn().getHeaders();
            for (Iterator<String> iterator = headersForNewExchange.keySet().iterator(); iterator.hasNext(); ) {
                String header = iterator.next();
                oldExchange.getIn().setHeader(header, headersForNewExchange.get(header));
            }

            for (String key : newExchange.getProperties().keySet()) {
                oldExchange.setProperty(key, newExchange.getProperty(key));
            }
        }

        return oldExchange;
    }
}
