package org.recap.controller;

import org.apache.camel.CamelContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.model.etl.EtlLoadRequest;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.XmlRecordRepository;
import org.recap.route.BibDataProcessor;
import org.recap.route.RecordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        etlLoadRequest.setBatchSize(batchSize);
        etlLoadRequest.setInputDirectoryPath(inputDirectoryPath);
        model.addAttribute("etlLoadRequest", etlLoadRequest);
        return "etlDataLoader";
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/bulkIngest", method = RequestMethod.POST)
    public String bulkIngest(@Valid @ModelAttribute("etlLoadRequest") EtlLoadRequest etlLoadRequest,
                            BindingResult result,
                            Model model) {
        report();
        return etlDataLoader(model);
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/etlStep1", method = RequestMethod.POST)
    public String loadXMLRecords() {
        String status = "Process Started";
        if (camelContext.getStatus().isStarted()) {
            status = "Running";
        }

        long totalDocCount = xmlRecordRepository.count();

        int quotient = Integer.valueOf(Long.toString(totalDocCount)) / (1000);
        int remainder = Integer.valueOf(Long.toString(totalDocCount)) % (1000);

        int loopCount = remainder == 0 ? quotient : quotient + 1;

        Page<XmlRecordEntity> xmlRecordEntities = null;
        long totalStartTime = System.currentTimeMillis();
        for(int i =0; i < loopCount; i++){
            long startTime = System.currentTimeMillis();
            xmlRecordEntities = xmlRecordRepository.findAll(new PageRequest(i, 1000));
            recordProcessor.process(xmlRecordEntities);
            long endTime = System.currentTimeMillis();
            logger.info("Time taken to save: " + xmlRecordEntities.getNumberOfElements() + " bib and related data is: " + (endTime - startTime) / 1000 + " seconds.");
        }

        long totalEndTime = System.currentTimeMillis();
        logger.info("Time taken to save: " + xmlRecordEntities.getTotalElements() + " bib and related data is: " + (totalEndTime - totalStartTime) / 1000 + " seconds.");

        return status;
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
