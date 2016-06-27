package org.recap.route;

import org.springframework.stereotype.Component;

/**
 * Created by pvsubrah on 6/26/16.
 */

@Component
public class JMSMessageProcessor {
    public void processMessage(String message){
        System.out.println(message);

    }
}
