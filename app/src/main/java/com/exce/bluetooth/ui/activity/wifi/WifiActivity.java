package com.exce.bluetooth.ui.activity.wifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.exce.bluetooth.R;
import com.exce.bluetooth.ui.activity.wifi.tcp.TcpClient;
import com.exce.bluetooth.ui.adapter.WifiListAdapter;
import com.exce.bluetooth.utils.SPUntil;
import com.exce.wifiutils.WifiUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author Wangjj
 * @Create 2018/5/8  17:37.
 * @Title
 */
public class WifiActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static Context context;
    private final MyHandler myHandler = new MyHandler(this);


    private Dialog dialog;
    private Button btRefresh, btSaveMySelf, btSureSend, btOpenWifi, btConnWifi;
    private TextView deviceTv, wifiInfo;
    private EditText edInput;
    private WifiListAdapter mWifiListAdapter;
    private String password;
    List<ScanResult> mResultList;

    private String serverIP = "10.1.1.251";
    private int serverPort = 12306;
    private static TcpClient tcpClient = null;
    ExecutorService exec = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_main);
        tcpClient = new TcpClient(serverIP, serverPort);
        exec.execute(tcpClient);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 555);
        //检查wifi是否开启
        WifiUtils.enableLog(true);
        context = this;
        init();

    }

    //初始化控件
    private void init() {
        mWifiListAdapter = new WifiListAdapter(this);
        wifiInfo = findViewById(R.id.info);
        wifiInfo.setText(getInfoSave());
        edInput = findViewById(R.id.ed_input_pwd);
        btSaveMySelf = findViewById(R.id.bt_save_myself);
        btSureSend = findViewById(R.id.bt_sure_send);
        btOpenWifi = findViewById(R.id.bt_open_wifi);
        btConnWifi = findViewById(R.id.bt_conn_wifi);
        deviceTv = findViewById(R.id.tv_device_wifi);

        btSaveMySelf.setOnClickListener(v -> getInfoSave());
        btSureSend.setOnClickListener(v -> sureSend());
        btOpenWifi.setOnClickListener(v -> systemWifi());
        btConnWifi.setOnClickListener(v -> connWifi());
        deviceTv.setOnClickListener(v -> showDeviceListDialog());
    }

    private void connWifi() {
        password = edInput.getText().toString();
        WifiUtils.withContext(getApplicationContext())
                .connectWith(ssid, password)
                .setTimeout(40000)
                .onConnectionResult(this::checkResult)
                .start();
    }

    //获取系统WiFi
    private void systemWifi() {
        Intent it = new Intent();
        ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
        it.setComponent(cn);
        startActivity(it);
    }

    //获取当前WiFi信息 并 保存我自己wifi信息
    private String getInfoSave() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String maxText = info.getMacAddress();
        String ipText = intToIp(info.getIpAddress());
        String status = "";
        if (wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            status = "WIFI_STATE_ENABLED";
        }
        String ssid = info.getSSID();
        int networkID = info.getNetworkId();
        int speed = info.getLinkSpeed();
        SPUntil.putMyselfWifi(getApplicationContext(), ipText);
        Toast.makeText(getApplicationContext(), "保存成功--->" + ipText, Toast.LENGTH_SHORT).show();
        return "mac：" + maxText + "\n\r"
                + "ip：" + ipText + "\n\r"
                + "wifi status :" + status + "\n\r"
                + "ssid :" + ssid + "\n\r"
                + "net work id :" + networkID + "\n\r"
                + "connection speed:" + speed + "\n\r";
    }


    // 连接wifi 确定发送信息
    private void sureSend() {
        Message message = Message.obtain();
        message.what = 2;
        message.obj = SPUntil.getMyselfWifi(context);
        myHandler.sendMessage(message);
        exec.execute(() -> tcpClient.send(SPUntil.getMyselfWifi(context)));
    }

    @SuppressLint("HandlerLeak")
    private class MyHandler extends Handler {
        private WeakReference<WifiActivity> mActivity;
        MyHandler(WifiActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity != null) {
                switch (msg.what) {
                    case 1:
//                        txtRcv.append(msg.obj.toString());
                        break;
                    case 2:

//                        txtSend.append(msg.obj.toString());
                        break;
                }
            }
        }
    }

    //ip地址写法
    private String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);
    }

    //wifi 连接返回成功或失败
    private void checkResult(boolean isSuccess) {
        if (isSuccess)
            Toast.makeText(this, "SUCCESS!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "EPIC FAIL!", Toast.LENGTH_SHORT).show();
    }

    //扫描周围wifi
    private void scanWifi() {
        WifiUtils.withContext(getApplicationContext()).scanWifi(scanResults -> {
            if (scanResults.isEmpty()) {
                Log.i(TAG, "SCAN RESULTS IT'S EMPTY");
                return;
            }
            Log.i(TAG, "GOT SCAN RESULTS " + scanResults);
            mWifiListAdapter.addData(scanResults);
            System.out.println("scanresults.........................................." + scanResults);
        }).start();
    }

    String ssid;

    //列表显示&&刷新
    private void showDeviceListDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.wifi_dialog_scan_device, null);
        dialog = new Dialog(this, R.style.MyDialog);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
        Button btnCancle = view.findViewById(R.id.btn_scan_cancle);
        btRefresh = view.findViewById(R.id.btn_refresh_wifi);
        ListView lvDevice = view.findViewById(R.id.lv_wifi_device);
        lvDevice.setAdapter(mWifiListAdapter);
        scanWifi();
        btRefresh.setOnClickListener(v -> {
            mWifiListAdapter.clear();
            scanWifi();
            Toast.makeText(getApplicationContext(), "刷新wifi列表。。。", Toast.LENGTH_SHORT).show();
        });

        lvDevice.setOnItemClickListener((parent, view1, position, id) -> {
            mResultList = new ArrayList<>();
            mWifiListAdapter = (WifiListAdapter) parent.getAdapter();
            for (int i = 0; i < mWifiListAdapter.getCount(); i++) {
                ScanResult item = (ScanResult) mWifiListAdapter.getItem(position);
                ssid = item.SSID;
                deviceTv.setText(ssid);
            }
            Toast.makeText(getApplicationContext(), "选择... ", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        btnCancle.setOnClickListener(v -> dialog.dismiss());
    }
}
