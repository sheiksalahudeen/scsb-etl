package org.recap.controller;

import org.apache.camel.CamelContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.model.etl.EtlLoadRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.XmlRecordRepository;
import org.recap.route.EtlDataLoadProcessor;
import org.recap.route.RecordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;

/**
 * Created by rajeshbabuk on 22/6/16.
 */

@Controller
public class EtlDataLoadController {

    Logger logger = LoggerFactory.getLogger(EtlDataLoadController.class);

    @Autowired
    CamelContext camelContext;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    XmlRecordRepository xmlRecordRepository;

    @Value("${etl.number.of.threads}")
    private Integer numberOfThreads;

    @Value("${etl.load.batchSize}")
    private Integer batchSize;

    @Value("${etl.load.directory}")
    private String inputDirectoryPath;

    @Autowired
    RecordProcessor recordProcessor;

    @RequestMapping("/")
    public String etlDataLoader(Model model) {
        EtlLoadRequest etlLoadRequest = new EtlLoadRequest();
        etlLoadRequest.setNumberOfThreads(numberOfThreads);
        model.addAttribute("etlLoadRequest", etlLoadRequest);
        return "etlDataLoader";
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/bulkIngest", method = RequestMethod.POST)
    public String bulkIngest(@Valid @ModelAttribute("etlLoadRequest") EtlLoadRequest etlLoadRequest,
                            BindingResult result,
                            Model model) {
        EtlDataLoadProcessor etlDataLoadProcessor = new EtlDataLoadProcessor();
        etlDataLoadProcessor.setBatchSize(etlLoadRequest.getBatchSize());
        etlDataLoadProcessor.setFileName(etlLoadRequest.getFileName());
        etlDataLoadProcessor.setXmlRecordRepository(xmlRecordRepository);
        etlDataLoadProcessor.setRecordProcessor(recordProcessor);
        etlDataLoadProcessor.startLoadProcess();
        return etlDataLoader(model);
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/report", method = RequestMethod.GET)
    public String report() {
        String status = "Process Started";
        if (camelContext.getStatus().isStarted()) {
            status = "Running";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Status  : " + status).append("\n");
        stringBuilder.append("Number of Bibs loaded : " + bibliographicDetailsRepository.count()).append("\n");
        return stringBuilder.toString();
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/uploadFiles", method = RequestMethod.POST)
    public String uploadFiles(@Valid @ModelAttribute("etlLoadRequest") EtlLoadRequest etlLoadRequest,
                             BindingResult result,
                             Model model) throws IOException {

        MultipartFile multipartFile = etlLoadRequest.getFile();
        if (null == multipartFile || StringUtils.isBlank(multipartFile.getOriginalFilename())) {
            return etlDataLoader(model);
        }
        File uploadFile = new File(multipartFile.getOriginalFilename());
        FileUtils.writeByteArrayToFile(uploadFile, etlLoadRequest.getFile().getBytes());
        FileUtils.copyFile(uploadFile, new File(inputDirectoryPath + File.separator + multipartFile.getOriginalFilename()));
        return etlDataLoader(model);
    }
}
