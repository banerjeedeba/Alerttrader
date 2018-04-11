package com.at.alerttrader.util;

/**
 * Created by lenovo on 24-03-2018.
 */

public class StringUtils {

    public static boolean isEmpty(String value){
        if(value!=null && value.trim().length()>0) return false;
        return true;
    }
}
