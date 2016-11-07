package org.recap.camel.datadump;

import java.util.StringTokenizer;

/**
 * Created by peris on 11/5/16.
 */
public class DataExportHeaderValueEvaluator {
    public String getValueFor(String batchHeaderString, String key) {
        StringTokenizer stringTokenizer = new StringTokenizer(batchHeaderString, ";");
        while(stringTokenizer.hasMoreTokens()){
            StringTokenizer stringTokenizer1 = new StringTokenizer(stringTokenizer.nextToken(), "#");
            if(stringTokenizer1.nextToken().equals(key)){
                return stringTokenizer1.nextToken();
            }
        }
        return null;
    }
}
