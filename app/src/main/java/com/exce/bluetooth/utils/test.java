package com.exce.bluetooth.utils;

/**
 * @Author Wangjj
 * @Create 2018/5/3  13:56.
 * @Title
 */
public class test {

    public static void main(String[] args) {

        int aa[] = { 22, 38,38, 22,22, 4, 4, 11, 11 };
        int temp = 0;
        for (int i = 0; i < aa.length; i++) {
            temp ^= aa[i];
        }
        System.out.println(temp);



        int a = 10, b = 5;

        a = a ^ b;

        b = a ^ b;

        a = a ^ b;
        System.out.println(a);
    }
}
