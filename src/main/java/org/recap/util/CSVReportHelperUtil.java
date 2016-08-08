package org.recap.util;

import org.recap.model.csv.FailureReportReCAPCSVRecord;
import org.recap.model.jpa.ReportEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by SheikS on 8/8/2016.
 */
public class CSVReportHelperUtil {

    public FailureReportReCAPCSVRecord prepareFailureReportReCAPCSVRecord(List<ReportEntity> reportEntities) {
        FailureReportReCAPCSVRecord failureReportReCAPCSVRecord = new FailureReportReCAPCSVRecord();
        for (Iterator<ReportEntity> iterator = reportEntities.iterator(); iterator.hasNext(); ) {
            ReportEntity report =  iterator.next();
            String headerName = report.getHeaderName();
            String headerValue = report.getHeaderValue();
            try {
                Field field = failureReportReCAPCSVRecord.getClass().getDeclaredField(headerName);
                Class<?> type = field.getType();
                Method prepareAuthenticationType = failureReportReCAPCSVRecord.getClass().getMethod("set" + field.getName()
                        .replaceFirst(field.getName().substring(0, 1), field.getName()
                                .substring(0, 1).toUpperCase()), type);
                if(null != prepareAuthenticationType) {
                    prepareAuthenticationType.invoke(failureReportReCAPCSVRecord, headerValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return failureReportReCAPCSVRecord;
    }
}
