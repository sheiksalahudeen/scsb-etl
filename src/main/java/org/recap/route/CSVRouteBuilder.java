package org.recap.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;

import java.io.File;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
public class CSVRouteBuilder extends RouteBuilder {

    private String reportDirectoryPath;
    private String ftpPrivateKey;
    private String ftpKnownHost;
    private String ftpUserName;
    private String ftpRemoteServer;

    public String getReportDirectoryPath() {
        return reportDirectoryPath;
    }

    public void setReportDirectoryPath(String reportDirectoryPath) {
        this.reportDirectoryPath = reportDirectoryPath;
    }

    public String getFtpPrivateKey() {
        return ftpPrivateKey;
    }

    public void setFtpPrivateKey(String ftpPrivateKey) {
        this.ftpPrivateKey = ftpPrivateKey;
    }

    public String getFtpKnownHost() {
        return ftpKnownHost;
    }

    public void setFtpKnownHost(String ftpKnownHost) {
        this.ftpKnownHost = ftpKnownHost;
    }

    public String getFtpUserName() {
        return ftpUserName;
    }

    public void setFtpUserName(String ftpUserName) {
        this.ftpUserName = ftpUserName;
    }

    public String getFtpRemoteServer() {
        return ftpRemoteServer;
    }

    public void setFtpRemoteServer(String ftpRemoteServer) {
        this.ftpRemoteServer = ftpRemoteServer;
    }

    @Override
    public void configure() throws Exception {
//        from("seda:etlFailureReportQ")
//                .routeId("failureReportQRoute")
//                .process(new CSVFailureFileNameProcessor()).marshal().bindy(BindyType.Csv, ReCAPCSVRecord.class)
//                .to("file:"+reportDirectoryPath + File.separator + "?fileName=${in.header.reportFileName}-Failure-${date:now:ddMMMyyyy}.csv&fileExist=append")
//                .onCompletion().to("sftp://" +ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.institutionName}/${in.header.reportFileName}-Failure-${date:now:ddMMMyyyy}.csv&fileExist=append");
//
//        from("seda:etlSuccessReportQ")
//                .routeId("successReportQRoute")
//                .process(new CSVSuccessFileNameProcessor()).marshal().bindy(BindyType.Csv, SuccessReportReCAPCSVRecord.class)
//                .to("file:"+reportDirectoryPath + File.separator + "?fileName=${in.header.reportFileName}-Success-${date:now:ddMMMyyyy}.csv&fileExist=append")
//                .onCompletion().to("sftp://" +ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.institutionName}/${in.header.reportFileName}-Success-${date:now:ddMMMyyyy}.csv&fileExist=append");

    }
}
