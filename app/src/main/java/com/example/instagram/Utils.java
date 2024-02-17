package com.example.instagram;

import android.text.format.DateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class Utils {

    public static final String NOTIFICATION_TYPE_NEW_MESSAGE="NEW_MESSAGE"; // TYPE Thông báo
    public static final String MESSAGE_TYPE_TEXT="TEXT"; // message đoạn chat
    public static final String MESSAGE_TYPE_IMAGE="IMAGE"; // message hình ảnh

    //chat path
    public static final String chatPath(String receiptUid, String yourUid){
        //mảng uid chat
        String[] arrayUids = new String[]{receiptUid,yourUid};
        //sắp xếp mảng chat
        Arrays.sort(arrayUids);
        //nối cả 2 mảng sau khi sắp xếp
        String chatPath = arrayUids[0] + "_" + arrayUids[1];
        return chatPath;
    }

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
