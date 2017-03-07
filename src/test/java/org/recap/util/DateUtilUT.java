package org.recap.util;

import org.junit.Test;
import org.recap.RecapConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by premkb on 20/8/16.
 */
public class DateUtilUT {

    @Test
    public void getDateFromString() {
        Calendar cal = Calendar.getInstance();
        System.out.print(cal);
        Date inputDate = cal.getTime();
        DateFormat df = new SimpleDateFormat(RecapConstants.DATE_FORMAT_MMDDYYY);
        String inputDateString = df.format(inputDate);
        Date outputDate = DateUtil.getDateFromString(inputDateString, RecapConstants.DATE_FORMAT_MMDDYYY);
        assertEquals(inputDate.getDate(), outputDate.getDate());
        assertEquals(inputDate.getMonth(), outputDate.getMonth());
        assertEquals(inputDate.getYear(), outputDate.getYear());
    }

}
