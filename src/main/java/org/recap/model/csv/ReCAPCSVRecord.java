package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.Link;
import org.apache.camel.dataformat.bindy.annotation.OneToMany;

import java.util.List;

/**
 * Created by peris on 7/21/16.
 */

@CsvRecord(generateHeaderColumns = true, separator = ",", crlf = "UNIX")
public class ReCAPCSVRecord {

    @OneToMany(mappedTo = "org.recap.model.csv.FailureReportReCAPCSVRecord")
    List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecordList;

    public List<FailureReportReCAPCSVRecord> getFailureReportReCAPCSVRecordList() {
        return failureReportReCAPCSVRecordList;
    }

    public void setFailureReportReCAPCSVRecordList(List<FailureReportReCAPCSVRecord> failureReportReCAPCSVRecordList) {
        this.failureReportReCAPCSVRecordList = failureReportReCAPCSVRecordList;
    }
}
