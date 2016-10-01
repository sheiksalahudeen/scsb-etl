package org.recap.service.transmission.datadump;

import org.recap.model.export.DataDumpRequest;

import java.util.Map;

/**
 * Created by premkb on 28/9/16.
 */
public interface DataDumpTransmissionInterface {

    public boolean isInterested(DataDumpRequest dataDumpRequest);

    public void transmitDataDump(Object object, Map<String,String> routeMap);

}
