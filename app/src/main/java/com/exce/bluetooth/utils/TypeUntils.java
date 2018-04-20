package com.exce.bluetooth.utils;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import java.util.LinkedList;
import java.util.List;

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

    /**
     * 模拟unsigned 的 byte类型
     *
     * @param i
     * @return
     */
    public static byte unsigned_byte(int i) {
        if (i > 255 || i < 0) {
            throw new RuntimeException("i 必须在 0x00 - 0xff 之间");
        }
        if (i > 127) {
            return (byte) (i - 256);
        } else {
            return (byte) i;
        }
    }

    /**
     * byte[] 拼接
     * @return
     */
    public static byte[] byteAppend(byte[]... bytes) {
        int len = 0;
        int subscript = 0;
        for (byte[] b: bytes) {
            if (b == null) continue;
            len += b.length;
        }

        byte[] all = new byte[len];
        for (byte[] b: bytes) {
            if (b == null) continue;
            for (byte sb: b) {
                all[subscript] = sb;
                subscript++;
            }
        }

        return all;
    }

    /**
     * float 转 byte[]
     * @param f
     * @return
     */
    public static byte[] float2Bytes(float f) {
        return Ints.toByteArray(Float.floatToIntBits(f));
    }

    /**
     * byte[] 转 float
     * @param b
     * @return
     */
    public static float bytes2Float(byte[] b){
        return Float.intBitsToFloat(Ints.fromByteArray(b));
    }
}
