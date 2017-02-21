package org.recap.util.datadump;

import info.freelibrary.util.LoggerFactory;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.recap.ReCAPConstants;
import org.recap.model.csv.DataDumpFailureReport;
import org.recap.model.jpa.ReportDataEntity;
import org.recap.model.jpa.ReportEntity;
import org.slf4j.Logger;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by premkb on 30/9/16.
 */
public class DataDumpFailureReportGenerator {

    Logger logger = LoggerFactory.getLogger(DataDumpFailureReportGenerator.class);

    public DataDumpFailureReport prepareDataDumpCSVFailureRecord(ReportEntity reportEntity) {

        List<ReportDataEntity> reportDataEntities = reportEntity.getReportDataEntities();

        DataDumpFailureReport dataDumpFailureReport = new DataDumpFailureReport();

        for (Iterator<ReportDataEntity> iterator = reportDataEntities.iterator(); iterator.hasNext(); ) {
            ReportDataEntity report =  iterator.next();
            String headerName = report.getHeaderName();
            String headerValue = report.getHeaderValue();
            Method setterMethod = getSetterMethod(headerName);
            if(null != setterMethod){
                try {
                    setterMethod.invoke(dataDumpFailureReport, headerValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error(ReCAPConstants.ERROR,e);
                }
            }
        }
        return dataDumpFailureReport;
    }

    public Method getSetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getWriteMethod(new PropertyDescriptor(propertyName, DataDumpFailureReport.class));
        } catch (IntrospectionException e) {
            logger.error(ReCAPConstants.ERROR,e);
        }
        return null;
    }

    public Method getGetterMethod(String propertyName) {
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            return propertyUtilsBean.getReadMethod(new PropertyDescriptor(propertyName, DataDumpFailureReport.class));
        } catch (IntrospectionException e) {
            logger.error(ReCAPConstants.ERROR,e);
        }
        return null;
    }
}
