package org.recap.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;

/**
 * Created by angelind on 21/7/16.
 */
public class FTPRouteBuilder extends RouteBuilder {

    private String ftpPrivateKey;
    private String ftpKnownHost;
    private String ftpUserName;
    private String ftpRemoteServer;
    private String reportsDirectory;

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

    public String getReportsDirectory() {
        return reportsDirectory;
    }

    public void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    @Override
    public void configure() throws Exception {
        FileEndpoint fileEndpoint = endpoint("file:" + reportsDirectory, FileEndpoint.class);
        String uri = "sftp://" +ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost;
        from(fileEndpoint)
                .to(uri);
    }
}
