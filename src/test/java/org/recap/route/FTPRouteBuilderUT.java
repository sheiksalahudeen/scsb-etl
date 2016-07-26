package org.recap.route;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by angelind on 21/7/16.
 */
public class FTPRouteBuilderUT extends BaseTestCase{

    @Value("${ftp.userName}")
    String ftpUserName;

    @Value("${ftp.remote.server}")
    String ftpRemoteServer;

    @Value("${ftp.knownHost}")
    String ftpKnownHost;

    @Value("${ftp.privateKey}")
    String ftpPrivateKey;

    @Value("${etl.report.directory}")
    private String reportDirectoryPath;

    @Autowired
    ProducerTemplate producer;

    @Test
    public void uploadFileToFTP() throws Exception {
        File file = new File(reportDirectoryPath + File.separator + "uploadTestFileToFTP.csv");
        if(!file.exists()) {
            file.createNewFile();
        }
        assertTrue(file.exists());
        String fileName = reportDirectoryPath + File.separator + "uploadTestFileToFTP.csv";
        camelContext.addRoutes((new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .process(new FTPUploadFileProcessor())
                        .to("sftp://" +ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.fileNameToUpload}");
            }
        }));
        producer.sendBody("direct:start", fileName);

        Thread.sleep(1000);
    }
}