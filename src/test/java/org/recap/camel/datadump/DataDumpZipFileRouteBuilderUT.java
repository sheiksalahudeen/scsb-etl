package org.recap.camel.datadump;

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
        String requestingInstituionCode = "NYPL";
        routeMap.put(ReCAPConstants.FILENAME,ReCAPConstants.DATA_DUMP_FILE_NAME+requestingInstituionCode);
        routeMap.put(ReCAPConstants.REQUESTING_INST_CODE,requestingInstituionCode);
        BibRecords bibRecords = new BibRecords();
        String dateTimeString = getDateTimeString();
        routeMap.put(ReCAPConstants.DATETIME_FOLDER,dateTimeString);
        producer.sendBodyAndHeader(ReCAPConstants.DATADUMP_ZIPALLFILE_FILESYSTEM_Q,bibRecords,"routeMap",routeMap);;
        Thread.sleep(2000);
        String filename = dumpDirectoryPath + File.separator + requestingInstituionCode+ File.separator +dateTimeString+ File.separator +ReCAPConstants.DATA_DUMP_FILE_NAME+requestingInstituionCode+"-"+dateTimeString+ ReCAPConstants.ZIP_FILE_FORMAT;
        File file = new File(filename);
        boolean fileExists = file.exists();
        assertTrue(fileExists);
        file.delete();
    }

    private String getDateTimeString(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(ReCAPConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);
    }
}
