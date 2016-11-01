package org.recap.camel.datadump;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
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
 * Created by premkb on 10/9/16.
 */
@Component
public class DataDumpFileSystemRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpFileSystemRouteBuilder.class);

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Override
    public void configure() throws Exception {
        from(ReCAPConstants.DATADUMP_FILE_SYSTEM_Q)
                .to("file:"+
                        dumpDirectoryPath +
                        File.separator +
                        "?fileName=${header.fileName}-${date:now:ddMMMyyyyHHmm}");
    }
}
