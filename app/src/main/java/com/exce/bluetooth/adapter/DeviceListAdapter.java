package com.exce.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.exce.bluetooth.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Wangjj
 * @Create 2018/3/8.
 * @Content 蓝牙设备适配器处理
 */
public class DeviceListAdapter extends BaseAdapter {
    //设备列表
    public List<BluetoothDevice> mBleDevices;
    private List<Double> mRssis;
    private Context mContext;

    public DeviceListAdapter(Context mContext) {
        this.mContext = mContext;
        mBleDevices = new ArrayList<BluetoothDevice>();
        mRssis = new ArrayList<Double>();
    }

    public void addDevice(BluetoothDevice device, Double rssi) {
        if (!mBleDevices.contains(device)) {
            mBleDevices.add(device);
            mRssis.add(rssi);
        }
        notifyDataSetChanged();
    }


    public BluetoothDevice getDevice(int position) {
        if (mBleDevices != null) {
            return mBleDevices.get(position);
        }
        return null;
    }

    public String getAddress(int position) {
        if (mBleDevices != null) {
            return mBleDevices.get(position).getAddress();
        }
        return null;
    }

    public void clear() {
        mBleDevices.clear();
    }

    @Override
    public int getCount() {
        if (mBleDevices != null) {
            return mBleDevices.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mBleDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_list_scan, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_device = (TextView) convertView.findViewById(R.id.tv_device);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Double mrssi = mRssis.get(position);
        String msg = "名称：" + mBleDevices.get(position).getName() + " 地址： " +
                mBleDevices.get(position).getAddress() + " " + "Rssi：" + String.format("%.2f", mrssi);

        viewHolder.tv_device.setText(msg);

        return convertView;
    }

    class ViewHolder {
        TextView tv_device;
    }
}
