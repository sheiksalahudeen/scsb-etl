package org.recap.camel;

import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import java.io.File;

/**
 * Created by premkb on 11/9/16.
 */
@Component
public class DataDumpReportFileSystemRouteBuilder extends RouteBuilder {

    @Value("${etl.dump.directory}")
    private String dumpDirectoryPath;

    @Override
    public void configure() throws Exception {
        from(ReCAPConstants.DATA_DUMP_REPORT_FILE_SYSTEM_Q).marshal().string().to("file:" + dumpDirectoryPath + File.separator +"?fileName=${header.reportMap[requestingInstitutionCode]}/${date:now:ddMMMyyyy}/${header.reportMap[fileName]}-${date:now:ddMMMyyyy}.txt")
                .end();
    }
}
