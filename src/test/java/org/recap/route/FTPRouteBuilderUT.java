package org.recap.route;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.assertNotNull;
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
        String content = "File \n Uploaded \n Successfully";
        file.createNewFile();
        assertTrue(file.exists());
        new FileWriter(file).append(content).flush();
        String fileName = reportDirectoryPath + File.separator + "uploadTestFileToFTP.csv";
        camelContext.addRoutes((new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:uploadFile")
                        .process(new FTPUploadFileProcessor())
                        .to("sftp://" +ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=${in.header.fileNameToUpload}");
            }
        }));
        producer.sendBody("seda:uploadFile", fileName);

        Thread.sleep(1000);

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("seda:getFileContent")
                        .pollEnrich("sftp://" +ftpUserName + "@" + ftpRemoteServer + "?privateKeyFile="+ ftpPrivateKey + "&knownHostsFile=" + ftpKnownHost + "&fileName=uploadTestFileToFTP.csv");
            }
        });

        String response = producer.requestBody("seda:getFileContent", "", String.class);

        Thread.sleep(1000);

        assertNotNull(response);
        System.out.println(response);
        assertTrue(response.equals(content));
    }
}