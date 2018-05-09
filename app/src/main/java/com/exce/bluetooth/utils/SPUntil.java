package com.exce.bluetooth.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * @Author Wangjj
 * @Create 2017/12/27.
 * @Content SharedPreferences 的封装
 */

public class SPUntil {

    private static String PROJECT_NAME = "wifi";
    private static final String MYSELF_WIFI = "my_self";

    private static SharedPreferences mSharedPreferences;

    /**
     * 保存
     *
     * @param context
     * @param contents
     */
    public static void putMyselfWifi(Context context, String contents) {
        mSharedPreferences = context.getSharedPreferences(PROJECT_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(MYSELF_WIFI, contents);
        edit.apply();
    }

    /**
     * 取出
     *
     * @param context
     * @return
     */
    public static String getMyselfWifi(Context context) {
        mSharedPreferences = context.getSharedPreferences(PROJECT_NAME, MODE_PRIVATE);
        return  mSharedPreferences.getString(MYSELF_WIFI, "");
    }

}
