package org.recap.camel.datadump.routebuilder;

import org.apache.camel.Exchange;

/**
 * Created by peris on 11/20/16.
 */
public class MockConsumerForQ2 {
    public void processMessage(String message, Exchange exchange) {
        System.out.println(Thread.currentThread().getId() + message);
        String updatedHeaders = (String) exchange.getIn().getHeader("batchHeaders");
        System.out.println(updatedHeaders);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
