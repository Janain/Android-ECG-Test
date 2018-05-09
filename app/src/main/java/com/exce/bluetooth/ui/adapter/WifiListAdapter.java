package com.exce.bluetooth.ui.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.exce.bluetooth.R;

import java.util.ArrayList;
import java.util.List;


public class WifiListAdapter extends BaseAdapter {
    //设备列表
    public List<ScanResult> mScanResults;

    private Context mContext;

    public WifiListAdapter(Context mContext) {
        this.mContext = mContext;
        mScanResults = new ArrayList<ScanResult>();
    }

    public void addData(List<ScanResult> datas) {

        if (mScanResults.size() > 0) {
            mScanResults.clear();
        }
        mScanResults.addAll(datas);
        notifyDataSetChanged();

    }

    public void clear() {
        mScanResults.clear();
    }

    @Override
    public int getCount() {
        if (mScanResults != null) {
            return mScanResults.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mScanResults.get(position);
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
                    R.layout.wifi_item_list, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_device = convertView.findViewById(R.id.tv_wifi_device);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String msg = "SSID：" + mScanResults.get(position).SSID;

        viewHolder.tv_device.setText(msg);

        return convertView;
    }

    class ViewHolder {
        TextView tv_device;
    }

}
