package org.recap.controller;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.recap.model.etl.EtlLoadRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.HoldingsDetailsRepository;
import org.recap.repository.ItemDetailsRepository;
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
    HoldingsDetailsRepository holdingsDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    XmlRecordRepository xmlRecordRepository;

    @Value("${etl.load.batchSize}")
    private Integer batchSize;

    @Value("${etl.load.directory}")
    private String inputDirectoryPath;

    @Autowired
    RecordProcessor recordProcessor;

    @Autowired
    ProducerTemplate producer;

    @RequestMapping("/")
    public String etlDataLoader(Model model) {
        EtlLoadRequest etlLoadRequest = new EtlLoadRequest();
        model.addAttribute("etlLoadRequest", etlLoadRequest);
        return "etlDataLoader";
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/bulkIngest", method = RequestMethod.POST)
    public String bulkIngest(@Valid @ModelAttribute("etlLoadRequest") EtlLoadRequest etlLoadRequest,
                            BindingResult result,
                            Model model) {
        EtlDataLoadProcessor etlDataLoadProcessor = new EtlDataLoadProcessor();
        long oldBibsCount = bibliographicDetailsRepository.count();
        long oldHoldingsCount = holdingsDetailsRepository.count();
        long oldItemsCount = itemDetailsRepository.count();
        String fileName = etlLoadRequest.getFileName();
        etlDataLoadProcessor.setBatchSize(etlLoadRequest.getBatchSize());
        etlDataLoadProcessor.setFileName(fileName);
        etlDataLoadProcessor.setXmlRecordRepository(xmlRecordRepository);
        etlDataLoadProcessor.setRecordProcessor(recordProcessor);
        etlDataLoadProcessor.startLoadProcess();
        if(StringUtils.isNotBlank(fileName)) {
            generateSuccessReport(oldBibsCount, oldHoldingsCount, oldItemsCount, fileName);
        }
        return etlDataLoader(model);
    }

    private void generateSuccessReport(long oldBibsCount, long oldHoldingsCount, long oldItemsCount, String fileName) {
        SuccessReportReCAPCSVRecord successReportReCAPCSVRecord = new SuccessReportReCAPCSVRecord();
        long newBibsCount = bibliographicDetailsRepository.count();
        long newHoldingsCount = holdingsDetailsRepository.count();
        long newItemsCount = itemDetailsRepository.count();
        Integer processedBibsCount = Integer.valueOf(new Long(newBibsCount).toString()) - Integer.valueOf(new Long(oldBibsCount).toString());
        Integer processedHoldingsCount = Integer.valueOf(new Long(newHoldingsCount).toString()) - Integer.valueOf(new Long(oldHoldingsCount).toString());
        Integer processedItemsCount = Integer.valueOf(new Long(newItemsCount).toString()) - Integer.valueOf(new Long(oldItemsCount).toString());
        Integer totalRecordsInfile = Integer.valueOf(new Long(xmlRecordRepository.countByXmlFileName(fileName)).toString());
        successReportReCAPCSVRecord.setFileName(fileName);
        successReportReCAPCSVRecord.setTotalRecordsInFile(totalRecordsInfile);
        successReportReCAPCSVRecord.setTotalBibsLoaded(processedBibsCount);
        successReportReCAPCSVRecord.setTotalHoldingsLoaded(processedHoldingsCount);
        successReportReCAPCSVRecord.setTotalItemsLoaded(processedItemsCount);
        producer.sendBody("seda:etlSuccessReportQ", successReportReCAPCSVRecord);
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
