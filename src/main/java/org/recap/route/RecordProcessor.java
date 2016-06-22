package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class RecordProcessor implements Processor {
    private static Logger logger = LoggerFactory.getLogger(RecordProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        if (exchange.getIn().getBody() instanceof List) {
            System.out.println("Record Size : " + ((List<String>) ((List) exchange.getIn().getBody())).size());
            for (String content : (List<String>) exchange.getIn().getBody()) {
                stringBuilder.append(content);
            }
        }
    }
}
