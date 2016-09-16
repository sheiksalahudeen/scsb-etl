package org.recap.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.ZipFileDataFormat;
import org.recap.ReCAPConstants;
import org.recap.model.jaxb.marc.BibRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import java.io.File;

/**
 * Created by premkb on 15/9/16.
 */
@Component
public class DataDumpZipFileRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpZipFileRouteBuilder.class);

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Override
    public void configure() throws Exception {
        JAXBContext context = JAXBContext.newInstance(BibRecords.class);
        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
        jaxbDataFormat.setContext(context);
        ZipFileDataFormat zip = new ZipFileDataFormat();
        from(ReCAPConstants.DATA_DUMP_ZIP_FILE_Q).marshal(jaxbDataFormat).marshal(zip)
                .to("file:"+dumpDirectoryPath + File.separator + "?fileName=${header.routeMap[requestingInstitutionCode]}/${date:now:ddMMMyyyy}/${header.routeMap[CamelFileName]}-${date:now:ddMMMyyyy}.zip");
        ;
    }

}
