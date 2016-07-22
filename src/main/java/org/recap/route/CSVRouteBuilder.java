package org.recap.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.model.csv.ReCAPCSVRecord;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;

/**
 * Created by chenchulakshmig on 4/7/16.
 */
public class CSVRouteBuilder extends RouteBuilder {

    private String reportDirectoryPath;

    public String getReportDirectoryPath() {
        return reportDirectoryPath;
    }

    public void setReportDirectoryPath(String reportDirectoryPath) {
        this.reportDirectoryPath = reportDirectoryPath;
    }

    @Override
    public void configure() throws Exception {
        from("seda:etlFailureReportQ")
                .routeId("failureReportQRoute")
                .process(new CSVFileNameProcessor()).marshal().bindy(BindyType.Csv, ReCAPCSVRecord.class)
                .to("file:"+reportDirectoryPath+"?fileName=${in.header.reportFileName}-${date:now:ddMMMyyyy}&fileExist=append");

        from("seda:etlSuccessReportQ")
                .routeId("successReportQRoute")
                .marshal().bindy(BindyType.Csv, SuccessReportReCAPCSVRecord.class)
                .to("file:"+reportDirectoryPath+"?fileName=SuccessReport-${date:now:ddMMMyyyy}&fileExist=append");
    }
}
