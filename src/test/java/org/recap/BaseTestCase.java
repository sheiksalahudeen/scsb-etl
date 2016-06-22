package org.recap;

import org.apache.camel.CamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Main.class)
@WebAppConfiguration
//@Transactional(rollbackForClassName={})
public class BaseTestCase {

//    @Autowired
//    protected ReCAPCamelContext reCAPCamelContext;

    @Autowired
    CamelContext camelContext;

    @Test
    public void contextLoads() {
        assertNotNull(camelContext);
       /* assertNotNull(reCAPCamelContext);
        assertTrue(reCAPCamelContext.isRunning());*/
    }
}
