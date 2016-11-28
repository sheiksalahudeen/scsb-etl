package org.recap.camel.datadump.routebuilder;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.recap.util.datadump.DataExportHeaderUtil;
import org.recap.camel.datadump.FileNameProcessorForDataExport;
import org.recap.camel.datadump.ZipFileProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by chenchulakshmig on 10/8/16.
 */

@Component
public class DataDumpFtpRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DataDumpFtpRouteBuilder.class);

    @Value("${etl.dump.ftp.staging.directory}")
    private String ftpStagingDir;

    @Autowired
    FileNameProcessorForDataExport fileNameProcessorForDataExport;

    @Autowired
    ZipFileProcessor zipFileProcessor;

    @Override
    public void configure() throws Exception {
        interceptFrom(ReCAPConstants.DATADUMP_ZIPFILE_FTP_Q)
                .process(fileNameProcessorForDataExport);

        from(ReCAPConstants.DATADUMP_ZIPFILE_FTP_Q)
                .onCompletion()
                .onWhen(new ExportFileDumpComplete())
                .process(zipFileProcessor)
                .end()
        .to("file:" + ftpStagingDir);
    }

    private class ExportFileDumpComplete implements Predicate {
        @Override
        public boolean matches(Exchange exchange) {
            String batchHeaders = (String) exchange.getIn().getHeader("batchHeaders");
            String totalPageCount = getValueFor(batchHeaders, "totalPageCount");
            String currentPageCount = getValueFor(batchHeaders, "currentPageCount");
            return totalPageCount.equals(currentPageCount);
        }
    }

    private String getValueFor(String batchHeaderString, String key) {
        String valueFor = new DataExportHeaderUtil().getValueFor(batchHeaderString, key);
        return valueFor;
    }

}
