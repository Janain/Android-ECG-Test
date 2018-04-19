package com.exce.bluetooth.blueutils.request;

import android.util.Log;

import com.exce.bluetooth.blueutils.callback.OnReceiverCallback;

import java.util.HashMap;

/**
 * 描述:接收通知数据请求队列
 */
public class ReceiverRequestQueue implements IRequestQueue<OnReceiverCallback> {
    private static final String TAG = "ReceiverRequestQueue";
    HashMap<String, OnReceiverCallback> map = new HashMap<>();

    @Override
    public void set(String key, OnReceiverCallback onReceiver) {
        map.put(key, onReceiver);
    }

    @Override
    public OnReceiverCallback get(String key) {
        return map.get(key);
    }

    public HashMap<String, OnReceiverCallback> getMap() {
        return map;
    }

    /**
     * 移除一个元素
     *
     * @param key
     */
    public boolean removeRequest(String key) {
        Log.d(TAG, "ReceiverRequestQueue before:" + map.size());
        OnReceiverCallback onReceiverCallback = map.remove(key);
        Log.d(TAG, "ReceiverRequestQueue after:" + map.size());
        return null == onReceiverCallback;
    }
}
