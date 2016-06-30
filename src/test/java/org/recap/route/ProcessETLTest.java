package org.recap.route;

import org.apache.camel.CamelContext;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by pvsubrah on 6/21/16.
 */
public class ProcessETLTest extends BaseTestCase {

    @Autowired
    CamelContext camelContext;

    @Test
    public void process() throws Exception {
        assertNotNull(camelContext);
        assertTrue(camelContext.getStatus().isStarted());
    }

    public String getEndPoint() throws URISyntaxException {
        URL resource = getClass().getResource("nypl-10k.xml");
        File file = new File(resource.toURI());
        return file.getParent();
    }
}
