package org.recap.service.transmission.datadump;

import org.recap.model.export.DataDumpRequest;

import java.util.Map;

/**
 * Created by premkb on 28/9/16.
 */
public interface DataDumpTransmissionInterface {

    boolean isInterested(DataDumpRequest dataDumpRequest);

    void transmitDataDump(Map<String,String> routeMap) throws Exception;

}
