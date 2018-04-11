package com.at.alerttrader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lenovo on 24-03-2018.
 */

public class DateUtil {

    public static String dateToString(Date date, String inputFormat){
        String dateFormat = "dd-MM-yyyy HH:mm:ss";
        if(!StringUtils.isEmpty(inputFormat)) dateFormat = inputFormat;
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.format(date);
    }

    public static Date stringToDate(String date, String inputFormat) throws ParseException {
        String dateFormat = "dd-MM-yyyy HH:mm:ss";
        if(!StringUtils.isEmpty(inputFormat)) dateFormat = inputFormat;
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.parse(date);
    }
}
