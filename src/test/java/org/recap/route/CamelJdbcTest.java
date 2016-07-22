package org.recap.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.model.ThreadsDefinition;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.recap.BaseTestCase;

import java.io.File;
import java.net.URL;

import org.recap.ReCAPInitializer;
import org.recap.repository.XmlRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by peris on 7/17/16.
 */

@Ignore
public class CamelJdbcTest extends BaseTestCase {

    @Value("${etl.split.xml.tag.name}")
    String xmlTagName;

    @Value("${etl.load.directory}")
    Integer etlPoolSize;

    @Value("${etl.pool.size}")
    Integer etlMaxPoolSize;

    @Value("${etl.max.pool.size}")
    String inputDirectoryPath;

    @Autowired
    XmlRecordRepository xmlRecordRepository;

    @Test
    public void parseXmlAndInsertIntoDb() throws Exception {


        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                FileEndpoint fileEndpoint = endpoint("file:" + inputDirectoryPath, FileEndpoint.class);
                fileEndpoint.setFilter(new XmlFileFilter());

                from(fileEndpoint)
                        .split()
                        .tokenizeXML(xmlTagName)
                        .streaming()
                        .threads(etlPoolSize, etlMaxPoolSize, "xmlProcessingThread")
                        .process(new XmlProcessor(xmlRecordRepository))
                        .to("jdbc:dataSource");
            }
        });

        java.lang.Thread.sleep(10000);
    }

    class XmlFileFilter implements GenericFileFilter {
        @Override
        public boolean accept(GenericFile file) {
            return FilenameUtils.getExtension(file.getAbsoluteFilePath()).equalsIgnoreCase("xml");
        }
    }
}
