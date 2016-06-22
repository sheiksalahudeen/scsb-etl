package org.recap.route;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class ProcessETLTest extends BaseTestCase {

    int chunkSize = 1000;

    @Test
    public void process() throws Exception {
        String endPoint = getEndPoint();
        reCAPCamelContext.addDynamicRoute(reCAPCamelContext, endPoint, chunkSize);
        while (reCAPCamelContext.isRunning()) {
            Thread.sleep(10000);
        }
    }

    public String getEndPoint() throws URISyntaxException {
        URL resource = getClass().getResource("nypl-10k.xml");
        File file = new File(resource.toURI());
        return file.getParent();
    }
}
