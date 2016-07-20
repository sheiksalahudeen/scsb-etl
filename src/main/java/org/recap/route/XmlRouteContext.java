package org.recap.route;

import org.apache.camel.CamelContext;
import org.apache.camel.LoggingLevel;
import org.apache.camel.PollingConsumer;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.GenericFileFilter;
import org.apache.camel.spi.ThreadPoolProfile;
import org.apache.commons.io.FilenameUtils;
import org.recap.repository.XmlRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;

import static org.apache.camel.component.xslt.XsltOutput.file;

/**
 * Created by peris on 7/17/16.
 */

@Component
public class XmlRouteContext {

    @Autowired
    public XmlRouteContext(@Value("${etl.split.xml.tag.name}")
                                   String xmlTagName, @Value("${etl.load.directory}")
                                   String inputDirectoryPath, @Value("${etl.pool.size}") Integer poolSize, @Value("${etl.max.pool.size}") Integer maxPoolSize, CamelContext camelContext, XmlRecordRepository xmlRecordRepository) throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                FileEndpoint fileEndpoint = endpoint("file:" + inputDirectoryPath, FileEndpoint.class);
                fileEndpoint.setFilter(new XmlFileFilter());

                from(fileEndpoint)
                        .split()
                        .tokenizeXML(xmlTagName)
                        .streaming()
                        .parallelProcessing().threads(100, 100)
                        .process(new XmlProcessor(xmlRecordRepository));
            }
        });
    }

    class XmlFileFilter implements GenericFileFilter {
        @Override
        public boolean accept(GenericFile file) {
            return FilenameUtils.getExtension(file.getAbsoluteFilePath()).equalsIgnoreCase("xml");
        }
    }
}
