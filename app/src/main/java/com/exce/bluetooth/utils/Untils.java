package com.exce.bluetooth.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * @Author Wangjj
 * @Create 2018/4/9.
 * @Content
 */
public class Untils {
    /**
     * 数组byte转int数据(大端)
     *
     * @param source byte[]
     * @return int
     */
    public static int be2Int24(byte[] source, int offset) {
        int target = 0;
        for (int i = 0; i < 3; i++) {
            target += (source[i + offset] & 0xff) << (8 * (3 - 1 - i));
        }
        System.out.println(Integer.toHexString(target));
        if ((target & 0x00800000) == 0x00800000) {
            target = target | 0xff000000;
        }
        return target;
    }

    /**
     * 数组byte转int数据（小端）
     *
     * @param source byte[]
     * @return int
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
     * 取出队列中的一个数据
     *
     * @param queue BlockingQueue<Byte>
     * @return byte
     */
    public static byte dequeue(BlockingQueue<Byte> queue) {
        Byte b;
        do {
            b = queue.poll();
            if (b == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (b == null);
        return b;
    }

    /**
     * 模拟unsigned 的 byte类型
     *
     * @param i int
     * @return byte
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
     *
     * @return byte[]
     */
    public static byte[] byteAppend(byte[]... bytes) {
        int len = 0;
        int subscript = 0;
        for (byte[] b : bytes) {
            if (b == null) continue;
            len += b.length;
        }
        byte[] all = new byte[len];
        for (byte[] b : bytes) {
            if (b == null) continue;
            for (byte sb : b) {
                all[subscript] = sb;
                subscript++;
            }
        }
        return all;
    }

    /**
     * float 转 byte[]
     *
     * @param f float
     * @return byte[]
     */
    public static byte[] float2Bytes(float f) {
        return Ints.toByteArray(Float.floatToIntBits(f));
    }

    /**
     * byte[] 转 float
     *
     * @param b byte[]
     * @return float
     */
    public static float bytes2Float(byte[] b) {
        return Float.intBitsToFloat(Ints.fromByteArray(b));
    }

    /**
     * 隐藏虚拟键盘
     */
    public  static void hideIputKeyboard(final Context context) {
        final Activity activity = (Activity) context;
        activity.runOnUiThread(() -> {
            InputMethodManager mInputKeyBoard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null) {
                mInputKeyBoard.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),        InputMethodManager.HIDE_NOT_ALWAYS);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }
        });
    }

}
