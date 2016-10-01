package org.recap.camel.datadump;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.ZipFileDataFormat;
import org.recap.ReCAPConstants;
import org.recap.model.jaxb.marc.BibRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import java.io.File;

/**
 * Created by premkb on 15/9/16.
 */
@Component
public class DataDumpZipFileFtpRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpZipFileFtpRouteBuilder.class);

    @Value("${ftp.userName}")
    String ftpUserName;

    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    @Value("${ftp.datadump.remote.server}")
    String ftpDataDumpRemoteServer;

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Override
    public void configure() throws Exception {
        ZipFileDataFormat zip = new ZipFileDataFormat();
        from(ReCAPConstants.DATADUMP_ZIPFILE_FTP_Q)
                .to("sftp://" +ftpUserName + "@" + ftpDataDumpRemoteServer+ File.separator+"?fileName=${header.routeMap[requestingInstitutionCode]}/${header.routeMap[dateTimeFolder]}/${header.routeMap[fileName]}-${date:now:ddMMMyyyyHHmm}${header.routeMap[fileFormat]}" + "&privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost)
        ;
    }
}
