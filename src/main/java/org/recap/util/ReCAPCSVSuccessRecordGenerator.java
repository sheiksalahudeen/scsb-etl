package org.recap.util;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.recap.ReCAPConstants;
import org.recap.model.csv.SuccessReportReCAPCSVRecord;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by angelind on 18/8/16.
 */
public class ReCAPCSVSuccessRecordGenerator {

    Logger logger = LoggerFactory.getLogger(ReCAPCSVSuccessRecordGenerator.class);

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
                    logger.error(ReCAPConstants.ERROR,e);
                }
            }
        }
        return successReportReCAPCSVRecord;
    }

    public Method getSetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getWriteMethod(new PropertyDescriptor(propertyName, SuccessReportReCAPCSVRecord.class));
        } catch (IntrospectionException e) {
            logger.error(ReCAPConstants.ERROR,e);
        }
        return null;
    }

    public Method getGetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getReadMethod(new PropertyDescriptor(propertyName, SuccessReportReCAPCSVRecord.class));
        } catch (IntrospectionException e) {
            logger.error(ReCAPConstants.ERROR,e);
        }
        return null;
    }

}
