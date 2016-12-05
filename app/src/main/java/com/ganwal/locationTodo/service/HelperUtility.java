package com.ganwal.locationTodo.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class HelperUtility {

    public static final String MOVIES_POPULARITY_SORT_ORDER = "popularity.desc";

    public static final int DUE_DATE_SORT_ORDER_VALUE = 1;
    public static final int PRIORITY_SORT_ORDER_VALUE = 2;
    public static final int NAME_SORT_ORDER_VALUE = 3;

    public static final String EXTRA_TODO_ID = "com.ganwal.locationTodo.todo_id";
    public static final String EXTRA_USER_ID = "com.ganwal.locationTodo.user_id";
    public static final String AUTH_HEADER_PREFIX  = "Bearer ";



    private static final String LOG_TAG = HelperUtility.class.getSimpleName();


    public static String getShortenedSummary(String summary) {
        int limit = (summary != null && summary.length() < 50) ? summary.length() : 50 ;
        return summary != null ? summary.substring(0, limit ): null;
    }

    public static Date convertLongToDate(Long longDate){
        return (longDate != null
                && longDate.compareTo(Long.MIN_VALUE) != 0 ) ? new Date(longDate) : null;
    }

    public static Long convertDateToLong(Date date){
        return date != null ? date.getTime() : Long.MIN_VALUE;
    }

    public static Date convertStringToDate(String dateStr){
        //assuming the date is in the format returned from movieDBapi is 2015-08-14
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = null;
        try {
            if(TextUtils.isEmpty(dateStr)) {
                Log.w(LOG_TAG, "WARNING - Received empty/null Date String");
            } else {
                newDate = format.parse(dateStr);
            }

        } catch (ParseException e) {
            Log.e(LOG_TAG, "ERROR -  Can't parse date string" + e.getMessage());
        }
        return newDate;
    }

    public static String convertDatetoString(Date date){

        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
        String dateStr = new String();
        if(date != null) {
            dateStr = format.format(date);
        }
        return dateStr;
    }

    public static String convertLongToString(Long longDate){
        return convertDatetoString(convertLongToDate(longDate));
    }

    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

}
