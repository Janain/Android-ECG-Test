package com.exce.bluetooth.utils;

/**
 * @Author Wangjj
 * @Create 2018/5/3  13:56.
 * @Title
 */
public class test {

    public static void main(String[] args) {

        byte aa[] = { 22, 38,38, 22,22, 4, 4, 11, 11 };
        byte temp = 0;
        for (byte anAa : aa) {
            temp ^= anAa;
        }
        System.out.println(temp);


    }
}
