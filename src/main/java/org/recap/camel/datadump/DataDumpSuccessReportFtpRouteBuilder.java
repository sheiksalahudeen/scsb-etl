package org.recap.camel.datadump;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.ReCAPConstants;
import org.recap.camel.FileNameProcessorForSuccessRecord;
import org.recap.model.csv.DataDumpSuccessReport;
import org.recap.model.csv.ReCAPCSVSuccessRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by premkb on 01/10/16.
 */

@Component
public class DataDumpSuccessReportFtpRouteBuilder {

    @Autowired
    public DataDumpSuccessReportFtpRouteBuilder(CamelContext context,
                                                @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.datadump.report.remote.server}") String ftpRemoteServer,
                                                @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DATADUMP_SUCCESS_REPORT_FTP_Q)
                            .routeId(ReCAPConstants.DATADUMP_SUCCESS_CSV_FTP_ROUTE_ID)
                            .process(new FileNameProcessorForDataDumpSuccess())
                            .marshal().bindy(BindyType.Csv, DataDumpSuccessReport.class)
                            .to("sftp://" + ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile=" + ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.directoryName}/${in.header.fileName}-${in.header.reportType}-${date:now:ddMMMyyyy}.csv&fileExist=append");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
