package org.recap.service.transmission.datadump;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by premkb on 28/9/16.
 */
@Service
public class DataDumpFileSystemTranmissionService implements DataDumpTransmissionInterface {

    @Autowired
    private ProducerTemplate producer;

    @Override
    public boolean isInterested(DataDumpRequest dataDumpRequest) {
        return dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FILESYSTEM) ? true : false;
    }

    @Override
    public void transmitDataDump(Object object, Map<String, String> routeMap) {
        producer.sendBodyAndHeader(ReCAPConstants.DATADUMP_FILE_SYSTEM_Q,  object, "routeMap", routeMap);
    }
}
