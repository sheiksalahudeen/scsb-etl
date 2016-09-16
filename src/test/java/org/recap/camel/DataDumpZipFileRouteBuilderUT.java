package org.recap.camel;

import org.apache.camel.ProducerTemplate;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.jaxb.marc.BibRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by premkb on 15/9/16.
 */
public class DataDumpZipFileRouteBuilderUT extends BaseTestCase {

    @Autowired
    ProducerTemplate producer;

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Test
    public void testZipFile() throws InterruptedException {
        Map<String,String> routeMap = new HashMap<>();
        routeMap.put(ReCAPConstants.CAMELFILENAME,ReCAPConstants.DATA_DUMP_FILE_NAME);
        String requestingInstituionCode = "NYPL";
        routeMap.put(ReCAPConstants.REQUESTING_INST_CODE,requestingInstituionCode);
        BibRecords bibRecords = new BibRecords();
        producer.sendBodyAndHeader(ReCAPConstants.DATA_DUMP_ZIP_FILE_Q,bibRecords,"routeMap",routeMap);
        Thread.sleep(2000);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
        String day = sdf.format(date);
        File file = new File(dumpDirectoryPath + File.separator + requestingInstituionCode+ File.separator +day+ File.separator +ReCAPConstants.DATA_DUMP_FILE_NAME+requestingInstituionCode+"-"+day+ ReCAPConstants.ZIP_FILE_FORMAT);
        boolean fileExists = file.exists();
        assertTrue(fileExists);
        file.delete();
    }
}
