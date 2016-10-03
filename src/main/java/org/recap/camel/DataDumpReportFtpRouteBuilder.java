package org.recap.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.recap.ReCAPConstants;
import org.recap.model.jaxb.marc.BibRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import java.io.File;

/**
 * Created by chenchulakshmig on 10/8/16.
 */
@Component
public class DataDumpReportFtpRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpReportFtpRouteBuilder.class);

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
        JAXBContext context = JAXBContext.newInstance(BibRecords.class);
        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
        jaxbDataFormat.setContext(context);
        from(ReCAPConstants.DATADUMP_REPORT_FTP_Q).marshal().string().to("sftp://" +ftpUserName + "@" + ftpDataDumpRemoteServer+ File.separator+"?fileName=${header.reportMap[requestingInstitutionCode]}/${date:now:ddMMMyyyy}/${header.reportMap[fileName]}-${date:now:ddMMMyyyy}.txt" + "&privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost)
                .end();
    }
}
