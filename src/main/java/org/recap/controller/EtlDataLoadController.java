package org.recap.controller;

import org.recap.model.etl.EtlLoadRequest;
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

    @RequestMapping("/")
    public String etlDataLoader(Model model) {
        model.addAttribute("etlLoadRequest", new EtlLoadRequest());
        return "etlDataLoader";
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/bulkIngest", method = RequestMethod.POST)
    public String bulkIngest(@Valid @ModelAttribute("etlLoadRequest") EtlLoadRequest etlLoadRequest,
                            BindingResult result,
                            Model model) {
        String inputDirectoryPath = etlLoadRequest.getInputDirectoryPath();
        Integer numberOfThreads = etlLoadRequest.getNumberOfThreads();
        Integer batchSize = etlLoadRequest.getBatchSize();

        System.out.println("Directory Path : " + inputDirectoryPath + "\n" +
                "Number of threads : " + numberOfThreads + "\n" +
                "Batch Size : " + batchSize);
        return etlDataLoader(model);
    }

    @ResponseBody
    @RequestMapping(value = "/etlDataLoader/report", method = RequestMethod.GET)
    public String report() {
        String status = "Process Started";
        return status;
    }
}
