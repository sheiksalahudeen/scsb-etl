package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.ZipFileDataFormat;
import org.apache.camel.processor.aggregate.zipfile.ZipAggregationStrategy;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by premkb on 15/9/16.
 */
@Component
public class DataDumpZipFileSystemRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpZipFileSystemRouteBuilder.class);

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Override
    public void configure() throws Exception {
        ZipFileDataFormat zip = new ZipFileDataFormat();
        from(ReCAPConstants.DATADUMP_ZIPFILE_FILESYSTEM_Q)
                .to("file:"+dumpDirectoryPath + File.separator + "?fileName=${header.routeMap[requestingInstitutionCode]}/${header.routeMap[dateTimeFolder]}/${header.routeMap[fileName]}-${date:now:ddMMMyyyyHHmm}${header.routeMap[fileFormat]}")
        ;
    }

}
