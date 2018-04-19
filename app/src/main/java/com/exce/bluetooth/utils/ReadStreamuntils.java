package com.exce.bluetooth.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @Author Wangjj
 * @Create 2018/4/11.
 * @Content
 */
public class ReadStreamuntils {
    /**
     * 读取流
     *
     * @param inStream
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            if (buffer[0] != (0xaa - 256)) break;
            if (buffer[1] != 0x30) break;
//            short datalen = Shorts.fromBytes(buffer[2], buffer[3]);

            outSteam.write(buffer, 4, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }

    public static void main(String[] args) {
        int number = 16;
        //原始数二进制
        printInfo(number);//f
        number = number << 1;
        //左移一位
        printInfo(number);//
        number = number >> 1;
        //右移一位
        printInfo(number);//111
    }

    /**
     * 输出一个int的二进制数
     *
     * @param num
     */
    private static void printInfo(int num) {
        System.out.println(Integer.toBinaryString(num));
    }
}
