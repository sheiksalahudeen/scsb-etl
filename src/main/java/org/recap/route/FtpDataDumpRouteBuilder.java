package org.recap.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.recap.model.jaxb.marc.BibRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import java.io.File;

/**
 * Created by chenchulakshmig on 10/8/16.
 */
@Component
public class FtpDataDumpRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(FtpDataDumpRouteBuilder.class);

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    public String getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    public void setDumpDirectoryPath(String dumpDirectoryPath) {
        this.dumpDirectoryPath = dumpDirectoryPath;
    }

    @Override
    public void configure() throws Exception {
        JAXBContext context = JAXBContext.newInstance(BibRecords.class);
        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
        jaxbDataFormat.setContext(context);
        from("seda:dataDumpQ").marshal(jaxbDataFormat).to("file:" + dumpDirectoryPath + File.separator + "?fileName=${in.header.fileName}")
                .end();
    }
}
