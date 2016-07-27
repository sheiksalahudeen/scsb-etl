package org.recap.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.etl.EtlLoadRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.XmlRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by chenchulakshmig on 14/7/16.
 */
public class EtlDataLoadControllerUT extends BaseTestCase {

    @Autowired
    EtlDataLoadController etlDataLoadController;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    XmlRecordRepository xmlRecordRepository;

    @Mock
    Model model;

    @Mock
    BindingResult bindingResult;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void uploadFiles() throws Exception {
        assertNotNull(etlDataLoadController);

        URL resource = getClass().getResource("SampleRecord.xml");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        assertNotNull(file);

        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(), "text/plain", IOUtils.toByteArray(input));
        assertNotNull(multipartFile);

        EtlLoadRequest etlLoadRequest = new EtlLoadRequest();
        etlLoadRequest.setFile(multipartFile);

        etlDataLoadController.uploadFiles(etlLoadRequest, bindingResult, model);

        Thread.sleep(1000);

        Long recordCount =  xmlRecordRepository.countByXmlFileName("SampleRecord.xml");
        assertTrue(recordCount == 1);
    }

}