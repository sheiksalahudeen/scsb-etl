package org.recap.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by premkb on 19/8/16.
 */
public class DataDumpRestControllerUT extends BaseControllerUT{

    private static final Logger logger = LoggerFactory.getLogger(DataDumpRestControllerUT.class);

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Autowired
    private DataDumpRestController dataDumpRestController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(dataDumpRestController).build();
    }

    @Test
    public void exportDataDump() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get("/exportDataDump")
                .param("institutionCodes","NYPL")
                .param("fetchType","1")
                .param("date","08-18-2016"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals("Data dump exported successfully",mvcResult.getResponse().getContentAsString());
        assertTrue(status == 200);
    }
}
