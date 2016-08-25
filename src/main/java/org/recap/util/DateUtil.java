package org.recap.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by premkb on 20/8/16.
 */
public class DateUtil {

    public static Date getDateFromString(String inputDateString,String dateFormat){
        Date outPutDate=null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            if(inputDateString != null) {
                outPutDate = sdf.parse(inputDateString);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outPutDate;
    }
}
