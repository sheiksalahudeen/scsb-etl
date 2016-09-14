package org.recap.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.recap.ReCAPConstants;
import org.recap.controller.swagger.DataDumpRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by premkb on 19/8/16.
 */
public class DataDumpRestControllerUT extends BaseControllerUT {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpRestControllerUT.class);

    @Autowired
    private DataDumpRestController dataDumpRestController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(dataDumpRestController).build();
    }

    @Test
    public void invalidFetchTypeParameters()throws Exception{
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("institutionCodes","NYPL")
                .param("fetchType","2"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(ReCAPConstants.DATADUMP_VALID_FETCHTYPE_ERR_MSG,mvcResult.getResponse().getContentAsString());
        assertTrue(status == 400);
    }

    @Test
    public void invalidIncremenatlDumpParameters()throws Exception{
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("fetchType","1")
                .param("institutionCodes","NYPL"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(ReCAPConstants.DATADUMP_DATE_ERR_MSG,mvcResult.getResponse().getContentAsString());
        assertTrue(status == 400);
    }

    @Test
    public void exportFullDataDump() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("institutionCodes","NYPL,PUL")
                .param("fetchType","0")
                .param("collectionGroupIds","1,2"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(ReCAPConstants.DATADUMP_PROCESS_STARTED,mvcResult.getResponse().getContentAsString());
        assertTrue(status == 200);
    }

    @Test
    public void exportIncrementalDataDump() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/dataDump/exportDataDump")
                .param("institutionCodes","NYPL,PUL")
                .param("fetchType","1")
                .param("date","2016-08-30 11:20"))
                .andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(ReCAPConstants.DATADUMP_PROCESS_STARTED,mvcResult.getResponse().getContentAsString());
        assertTrue(status == 200);
    }
}
