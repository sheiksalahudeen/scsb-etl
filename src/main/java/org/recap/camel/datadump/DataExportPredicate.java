package org.recap.camel.datadump;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

/**
 * Created by peris on 11/5/16.
 */
public class DataExportPredicate implements Predicate {

    private Integer batchSize;
    DataExportHeaderUtil dataExportHeaderUtil;

    public DataExportPredicate(Integer batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public boolean matches(Exchange exchange) {
        Integer batchSize = (Integer) exchange.getIn().getHeader("batchSize");

        String batchHeaderString = (String) exchange.getIn().getHeader("batchHeaders");

        Integer totalPageCount = Integer.valueOf(getDataExportHeaderUtil().getValueFor(batchHeaderString, "totalPageCount"));
        Integer currentPageCount = Integer.valueOf(getDataExportHeaderUtil().getValueFor(batchHeaderString, "currentPageCount"));

        System.out.println("Total page count: " + totalPageCount + " and Current page count: " + currentPageCount);

        if (this.batchSize.equals(batchSize) || (totalPageCount == currentPageCount)) {
            exchange.getIn().setHeader("batchSize", 0);
            return true;
        }
        return false;
    }

    public DataExportHeaderUtil getDataExportHeaderUtil() {
        if (null == dataExportHeaderUtil) {
            dataExportHeaderUtil = new DataExportHeaderUtil();
        }
        return dataExportHeaderUtil;
    }
}
