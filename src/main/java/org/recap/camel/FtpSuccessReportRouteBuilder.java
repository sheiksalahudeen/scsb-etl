package org.recap.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.ReCAPConstants;
import org.recap.model.csv.ReCAPCSVSuccessRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by angelind on 18/8/16.
 */

@Component
public class FtpSuccessReportRouteBuilder {
    Logger logger = LoggerFactory.getLogger(FtpFailureReportRouteBuilder.class);

    @Autowired
    public FtpSuccessReportRouteBuilder(CamelContext context,
                                        @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.remote.server}") String ftpRemoteServer,
                                        @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.FTP_FAILURE_Q)
                            .routeId(ReCAPConstants.FTP_FAILURE_ROUTE_ID)
                            .process(new FileNameProcessorForSuccessRecord())
                            .marshal().bindy(BindyType.Csv, ReCAPCSVSuccessRecord.class)
                            .to("sftp://" + ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile=" + ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.directoryName}/${in.header.fileName}-${in.header.reportType}-${date:now:ddMMMyyyy}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            logger.error(ReCAPConstants.ERROR,e);
        }
    }
}
