package org.recap.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.model.csv.ReCAPCSVFailureRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by peris on 8/16/16.
 */

@Component
public class FtpFailureReportRouteBuilder {

    @Autowired
    public FtpFailureReportRouteBuilder(CamelContext context,
                                        @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.remote.server}") String ftpRemoteServer,
                                        @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey) {

        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from("seda:ftpFailureQ")
                            .routeId("ftpFailureQ")
                            .process(new FileNameProcessorForFailureRecord())
                            .marshal().bindy(BindyType.Csv, ReCAPCSVFailureRecord.class)
                            .to("sftp://" + ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile=" + ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.directoryName}/${in.header.fileName}-${in.header.reportType}-${date:now:ddMMMyyyy}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
