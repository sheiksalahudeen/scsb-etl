package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.zipfile.ZipAggregationStrategy;
import org.recap.RecapConstants;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by peris on 11/6/16.
 */
@Component
public class ZipFileProcessor implements Processor {
    /**
     * The Ftp user name.
     */
    @Value("${ftp.userName}")
    String ftpUserName;

    /**
     * The Ftp known host.
     */
    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    /**
     * The Ftp private key.
     */
    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    /**
     * The Ftp data dump remote server.
     */
    @Value("${ftp.datadump.remote.server}")
    String ftpDataDumpRemoteServer;

    @Value("${etl.dump.ftp.staging.directory}")
    private String ftpStagingDir;

    /**
     * The Data export email processor.
     */
    @Autowired
    DataExportEmailProcessor dataExportEmailProcessor;

    /**
     * This method is invoked by route to zip the data dump files.
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        String batchHeaders = (String) exchange.getIn().getHeader("batchHeaders");
        String folderName = getValueFor(batchHeaders, "folderName");

        dataExportEmailProcessor.setInstitutionCodes(getInstitutionCodes(getValueFor(batchHeaders, "institutionCodes")));
        dataExportEmailProcessor.setTransmissionType(getValueFor(batchHeaders, "transmissionType"));
        dataExportEmailProcessor.setFolderName(folderName);
        dataExportEmailProcessor.setRequestingInstitutionCode(getValueFor(batchHeaders, "requestingInstitutionCode"));
        dataExportEmailProcessor.setToEmailId(getValueFor(batchHeaders, "toEmailId"));
        dataExportEmailProcessor.setRequestId(getValueFor(batchHeaders, "requestId"));
        dataExportEmailProcessor.setFetchType(getValueFor(batchHeaders, "fetchType"));

        Route ftpRoute = exchange.getContext().getRoute(RecapConstants.FTP_ROUTE);
        if (null != ftpRoute) {
            exchange.getContext().removeRoute(RecapConstants.FTP_ROUTE);
        }

        exchange.getContext().addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:" + ftpStagingDir + File.separator + folderName + "?noop=true&antInclude=*.xml,*.json")
                        .routeId(RecapConstants.FTP_ROUTE)
                        .aggregate(new ZipAggregationStrategy(true, true))
                        .constant(true)
                        .completionFromBatchConsumer()
                        .eagerCheckCompletion()
                        .process(dataExportEmailProcessor)
                        .to("sftp://" + ftpUserName + "@" + ftpDataDumpRemoteServer + File.separator + "?fileName=" + folderName + ".zip"
                                + "&privateKeyFile=" + ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost);
            }
        });
    }

    private String getValueFor(String batchHeaderString, String key) {
        return new DataExportHeaderUtil().getValueFor(batchHeaderString, key);
    }

    private List<String> getInstitutionCodes(String institutionCodes) {
        List codes = new ArrayList();
        StringTokenizer stringTokenizer = new StringTokenizer(institutionCodes, "*");
        while(stringTokenizer.hasMoreTokens()){
            codes.add(stringTokenizer.nextToken());
        }
        return codes;
    }

}
