package org.recap.controller;

import org.apache.camel.CamelContext;
import org.recap.model.etl.EtlLoadRequest;
import org.recap.repository.BibliographicDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * Created by rajeshbabuk on 22/6/16.
 */

@Controller
public class EtlDataLoadController {

    @Autowired
    CamelContext camelContext;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Value("${etl.number.of.threads}")
    private Integer numberOfThreads;

    @Value("${etl.load.batchSize}")
    private Integer batchSize;

    @Value("${etl.load.directory}")
    private String inputDirectoryPath;

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
}
