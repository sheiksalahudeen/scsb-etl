package org.recap.service.transmission.datadump;

import org.recap.model.export.DataDumpRequest;

import java.util.Map;

/**
 * Created by premkb on 28/9/16.
 */
public interface DataDumpTransmissionInterface {

    /**
     * Is interested boolean.
     *
     * @param dataDumpRequest the data dump request
     * @return the boolean
     */
    boolean isInterested(DataDumpRequest dataDumpRequest);

    /**
     * Transmit data dump.
     *
     * @param routeMap the route map
     * @throws Exception the exception
     */
    void transmitDataDump(Map<String,String> routeMap) throws Exception;

}
