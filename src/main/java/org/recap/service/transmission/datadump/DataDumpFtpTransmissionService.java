package org.recap.service.transmission.datadump;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.zipfile.ZipAggregationStrategy;
import org.recap.ReCAPConstants;
import org.recap.model.export.DataDumpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

/**
 * Created by premkb on 29/9/16.
 */
@Service
public class DataDumpFtpTransmissionService implements DataDumpTransmissionInterface {

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Value("${ftp.userName}")
    String ftpUserName;

    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    @Value("${ftp.datadump.remote.server}")
    String ftpDataDumpRemoteServer;

    @Autowired
    private CamelContext camelContext;

    @Override
    public boolean isInterested(DataDumpRequest dataDumpRequest) {
        return dataDumpRequest.getTransmissionType().equals(ReCAPConstants.DATADUMP_TRANSMISSION_TYPE_FTP) ? true : false;
    }

    @Override
    public void transmitDataDump(Map<String, String> routeMap) throws Exception {
        String requestingInstitutionCode = routeMap.get(ReCAPConstants.REQUESTING_INST_CODE);
        String dateTimeFolder = routeMap.get(ReCAPConstants.DATETIME_FOLDER);
        String fileName = routeMap.get(ReCAPConstants.FILENAME);
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:"+ dumpDirectoryPath + File.separator + requestingInstitutionCode + File.separator + dateTimeFolder + "?antInclude=*.xml")
                        .routeId(ReCAPConstants.DATADUMP_ZIPFTP_ROUTE_ID)
                        .aggregate(new ZipAggregationStrategy())
                        .constant(true)
                        .completionFromBatchConsumer()
                        .eagerCheckCompletion()
                        .to("sftp://" +ftpUserName + "@" + ftpDataDumpRemoteServer+ File.separator+"?fileName="+requestingInstitutionCode + File.separator + dateTimeFolder + File.separator + fileName + ".zip" + "&privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost)
                ;
            }
        });
    }
}
