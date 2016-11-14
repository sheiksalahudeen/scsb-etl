package org.recap.util.datadump;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.recap.ReCAPConstants;
import org.recap.camel.datadump.consumer.DataExportReportActiveMQConsumer;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.recap.repository.ReportDetailRepository;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by peris on 11/11/16.
 */
public class DataExportReportActiveMQConsumerTest {

    @Mock
    ReportDetailRepository mockReportDetailsRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processNewSuccessReportEntity() throws  Exception {
       DataExportReportActiveMQConsumer dataExportReportActiveMQConsumer = new DataExportReportActiveMQConsumer();
        HashMap values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, "PUL");
        values.put(ReCAPConstants.NUM_RECORDS, "12");
        values.put(ReCAPConstants.NUM_BIBS_EXPORTED, "Num Bibs Exported");
        values.put(ReCAPConstants.BATCH_EXPORT, "Batch Export");
        values.put(ReCAPConstants.REQUEST_ID, "PUL-2017-12-12 11");

        dataExportReportActiveMQConsumer.setReportDetailRepository(mockReportDetailsRepository);

        ReportEntity reportEntity = dataExportReportActiveMQConsumer.saveSuccessReportEntity(values);

        assertNotNull(reportEntity);


    }

    @Test
    public void processExistingSuccessReportEntity() throws  Exception {
       DataExportReportActiveMQConsumer dataExportReportActiveMQConsumer = new DataExportReportActiveMQConsumer();
        HashMap values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, "PUL");
        values.put(ReCAPConstants.NUM_RECORDS, "12");
        values.put(ReCAPConstants.NUM_BIBS_EXPORTED, "Num Bibs Exported");
        values.put(ReCAPConstants.BATCH_EXPORT, "Batch Export");
        values.put(ReCAPConstants.REQUEST_ID, "PUL-2017-12-12 11");
        dataExportReportActiveMQConsumer.setReportDetailRepository(mockReportDetailsRepository);

        ReportEntity savedReportEntity = dataExportReportActiveMQConsumer.saveSuccessReportEntity(values);


        Mockito.when(mockReportDetailsRepository.findByFileName("PUL-2017-12-12 11")).thenReturn(Arrays.asList(savedReportEntity));
        values.put(ReCAPConstants.NUM_RECORDS, "10");
        ReportEntity updatedReportEntity = dataExportReportActiveMQConsumer.saveSuccessReportEntity(values);
        assertNotNull(updatedReportEntity);
        List<ReportDataEntity> updatedReportDataEntities = updatedReportEntity.getReportDataEntities();
        for (Iterator<ReportDataEntity> iterator = updatedReportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity reportDataEntity = iterator.next();
            if(reportDataEntity.getHeaderName().equals("Num Bibs Exported")){
                assertEquals("22", reportDataEntity.getHeaderValue());
            }
        }
    }

    @Test
    public void processNewFailureReportEntity() throws  Exception {
       DataExportReportActiveMQConsumer dataExportReportActiveMQConsumer = new DataExportReportActiveMQConsumer();
        HashMap values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, "PUL");
        values.put(ReCAPConstants.NUM_RECORDS, "12");
        values.put(ReCAPConstants.FAILURE_CAUSE, "Bad happened");
        values.put(ReCAPConstants.BATCH_EXPORT, "Batch Export");
        values.put(ReCAPConstants.REQUEST_ID, "PUL-2017-12-12 11");

    }

    @Test
    public void processExistingFailureReportEntity() throws  Exception {
       DataExportReportActiveMQConsumer dataExportReportActiveMQConsumer = new DataExportReportActiveMQConsumer();
        HashMap values = new HashMap();
        values.put(ReCAPConstants.REQUESTING_INST_CODE, "PUL");
        values.put(ReCAPConstants.NUM_RECORDS, "12");
        values.put(ReCAPConstants.FAILURE_CAUSE, "Bad happened");
        values.put(ReCAPConstants.BATCH_EXPORT, "Batch Export");
        values.put(ReCAPConstants.REQUEST_ID, "PUL-2017-12-12 11");

//
//        values.put(ReCAPConstants.NUM_RECORDS, "12");
//        values.put(ReCAPConstants.FAILURE_CAUSE, "Bad happened");
//        ReportEntity updatedReportEntity = dataExportReportEntityHelper.processFailureReportEntity(Arrays.asList(reportEntity), values);
//        assertNotNull(updatedReportEntity);
//        List<ReportDataEntity> updatedReportDataEntities = updatedReportEntity.getReportDataEntities();
//        for (Iterator<ReportDataEntity> iterator = updatedReportDataEntities.iterator(); iterator.hasNext(); ) {
//            ReportDataEntity reportDataEntity = iterator.next();
//            if(reportDataEntity.getHeaderName().equals(ReCAPConstants.FAILED_BIBS)){
//                assertEquals("24", reportDataEntity.getHeaderValue());
//            }
//        }
    }

}