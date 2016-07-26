package org.recap.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.File;

/**
 * Created by angelind on 26/7/16.
 */
public class FTPUploadFileProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String filename = (String) exchange.getIn().getBody();
        File file = new File(filename);
        exchange.getIn().setBody(file);
        exchange.getIn().setHeader("fileNameToUpload", file.getName());
    }
}
