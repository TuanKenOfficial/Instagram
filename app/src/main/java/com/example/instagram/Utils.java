package com.example.instagram;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class Utils {

    public static  long getTimestamp(){
        return System.currentTimeMillis();
    }
    //định dạng ngày/tháng/năm
    public static String formatTimestampDate(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }

    //định dạng ngày/tháng/năm giờ/phút/giây
    public static String formatTimestampDateTime(Long timestamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);

        String date = DateFormat.format("dd/MM/yyyy HH:mm",calendar).toString();
        return date;
    }
}
