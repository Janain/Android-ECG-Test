package com.exce.bluetooth.utils;

/**
 * @Author Wangjj
 * @Create 2018/4/9.
 * @Content
 */
public class TypeUntils {
    /**
     * 数组byte转int数据(大端)
     * @param source
     * @return
     */
    public static int be2Int24(byte[] source, int offset) {
        int target = 0;
        for (int i = 0; i < 3; i++) {
            target += (source[i + offset] & 0xff) << (8 * (3-1-i));
        }
        System.out.println(Integer.toHexString(target));
        if ((target & 0x00800000) == 0x00800000) {
            target = target | 0xff000000;
        }
        return target;
    }

    /**
     * 数组byte转int数据（小端）
     * @param source
     * @return
     */
    public static int le2Int24(byte[] source, int offset) {
        int target = 0;
        for (int i = 0; i < 3; i++) {
            target += (source[i + offset] & 0xff) << (8 * i);
        }
        System.out.println(Integer.toHexString(target));
        if ((target & 0x00800000) == 0x00800000) {
            target = target | 0xff000000;
        }
        return target;
    }

//    public static void main(String[] args){
//        byte[] data = new byte[3];
//
//        be2Int24(data,0);
//
//    }
}
