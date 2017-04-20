package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.junit.Test;
import org.mockito.Mock;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 19/4/17.
 */
public class DataExportEmailProcessorUT extends BaseTestCase{

    @Autowired
    DataExportEmailProcessor dataExportEmailProcessor;

    @Mock
    Exchange exchange;

    @Test
    public void testDataExportEmailProcess() throws Exception {
        dataExportEmailProcessor.setTransmissionType("2");
        dataExportEmailProcessor.setInstitutionCodes(Arrays.asList("PUL","CUL"));
        dataExportEmailProcessor.setRequestingInstitutionCode("NYPL");
        dataExportEmailProcessor.setFolderName("test");
        dataExportEmailProcessor.setToEmailId("hemalatha.s@htcindia.com");
        dataExportEmailProcessor.setRequestId("1");
        dataExportEmailProcessor.setFetchType("1");
        dataExportEmailProcessor.process(exchange);
        assertNotNull(dataExportEmailProcessor.getTransmissionType());
        assertNotNull(dataExportEmailProcessor.getInstitutionCodes());
        assertNotNull(dataExportEmailProcessor.getRequestingInstitutionCode());
        assertNotNull(dataExportEmailProcessor.getFolderName());
        assertNotNull(dataExportEmailProcessor.getToEmailId());
        assertNotNull(dataExportEmailProcessor.getRequestId());
        assertNotNull(dataExportEmailProcessor.getFetchType());
    }

}