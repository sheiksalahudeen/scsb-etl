package org.recap.util;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by angelind on 18/8/16.
 */
public class ReCAPCSVSuccessRecordGenerator {

    public SuccessReportReCAPCSVRecord prepareSuccessReportReCAPCSVRecord(ReportEntity reportEntity) {

        List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();

        SuccessReportReCAPCSVRecord successReportReCAPCSVRecord = new SuccessReportReCAPCSVRecord();

        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity report =  iterator.next();
            String headerName = report.getHeaderName();
            String headerValue = report.getHeaderValue();
            Method setterMethod = getSetterMethod(headerName);
            if(null != setterMethod){
                try {
                    setterMethod.invoke(successReportReCAPCSVRecord, headerValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return successReportReCAPCSVRecord;
    }

    public Method getSetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            Method writeMethod = propertyUtilsBean.getWriteMethod(new PropertyDescriptor(propertyName, SuccessReportReCAPCSVRecord.class));
            return writeMethod;
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Method getGetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            Method writeMethod = propertyUtilsBean.getReadMethod(new PropertyDescriptor(propertyName, SuccessReportReCAPCSVRecord.class));
            return writeMethod;
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
