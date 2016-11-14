package org.recap.camel.datadump.routebuilder;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.util.concurrent.ThreadHelper;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by peris on 11/12/16.
 */
public class DataExportSuccessReportRouteBuilderUT extends BaseTestCase {

    @Autowired
    ProducerTemplate producerTemplate;

    @Test
    public void testRoute() throws Exception {
        Map values = new HashMap<>();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, "PUL");
        values.put(ReCAPConstants.NUM_RECORDS, String.valueOf("12"));
        values.put(ReCAPConstants.NUM_BIBS_EXPORTED, ReCAPConstants.NUM_BIBS_EXPORTED);
        values.put(ReCAPConstants.BATCH_EXPORT, ReCAPConstants.BATCH_EXPORT);
        values.put(ReCAPConstants.REQUEST_ID, "112-1");
        producerTemplate.sendBody("scsbactivemq:queue:dataExportSuccessQ", values);

        Thread.sleep(4000);
    }

}