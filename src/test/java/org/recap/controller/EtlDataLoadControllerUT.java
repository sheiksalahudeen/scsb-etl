package org.recap.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.recap.BaseTestCase;
import org.recap.model.etl.EtlLoadRequest;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.XmlRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

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

        Page<XmlRecordEntity> xmlRecordEntities = xmlRecordRepository.findByXmlFileName(new PageRequest(0, 10), "SampleRecord.xml");
        assertNotNull(xmlRecordEntities);
        List<XmlRecordEntity> xmlRecordEntityList = xmlRecordEntities.getContent();
        assertNotNull(xmlRecordEntityList);
        assertTrue(xmlRecordEntityList.size() > 0);
    }

    @Test
    public void testBulkIngest() throws Exception {
        uploadFiles();
        EtlLoadRequest etlLoadRequest = new EtlLoadRequest();
        etlLoadRequest.setFileName("SampleRecord.xml");
        etlLoadRequest.setBatchSize(1000);
        etlDataLoadController.bulkIngest(etlLoadRequest, bindingResult, model);
        Thread.sleep(1000);

        BibliographicEntity bibliographicEntity = bibliographicDetailsRepository.findByOwningInstitutionIdAndOwningInstitutionBibId(3, ".b153286131");
        assertNotNull(bibliographicEntity);
        assertNotNull(bibliographicEntity.getHoldingsEntities());
        assertNotNull(bibliographicEntity.getItemEntities());
    }

}