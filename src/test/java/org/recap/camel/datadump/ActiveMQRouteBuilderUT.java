package org.recap.camel.datadump;

import org.apache.camel.ProducerTemplate;
import org.junit.Ignore;
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
 * Created by premkb on 31/10/16.
 */
@Ignore
public class ActiveMQRouteBuilderUT extends BaseTestCase{

    @Autowired
    ProducerTemplate producer;

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Test
    public void createFile() throws Exception {
        Map<String, String> routeMap = new HashMap<>();
        String requestingInstituionCode = "NYPL";
        routeMap.put(ReCAPConstants.FILENAME, ReCAPConstants.DATA_DUMP_FILE_NAME + requestingInstituionCode);
        routeMap.put(ReCAPConstants.REQUESTING_INST_CODE, requestingInstituionCode);
        String dummyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><collection xmlns:marcxml=\"http://www.loc.gov/MARC21/slim\"></collection>";
        String dateTimeString = getDateTimeString();
        routeMap.put(ReCAPConstants.DATETIME_FOLDER, dateTimeString);
        routeMap.put("fileFormat",ReCAPConstants.XML_FILE_FORMAT);

        String filename = dumpDirectoryPath + File.separator + requestingInstituionCode+ File.separator +dateTimeString+ File.separator +ReCAPConstants.DATA_DUMP_FILE_NAME+requestingInstituionCode+"-"+dateTimeString+ ReCAPConstants.XML_FILE_FORMAT;
        producer.sendBodyAndHeader(ReCAPConstants.DATADUMP_FILE_SYSTEM_Q, dummyXml, "fileName", "tst");

        File file = new File(filename);
        boolean fileExists = file.exists();
        assertTrue(fileExists);
        file.delete();
    }
    public String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);

    }
}
