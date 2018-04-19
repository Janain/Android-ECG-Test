package com.exce.bluetooth.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.exce.bluetooth.R;
import com.exce.bluetooth.activity.SetActivity;
import com.exce.bluetooth.activity.usb.USBActivity;
import com.exce.bluetooth.activity.wifi.WIFIActivity;
import com.exce.bluetooth.adapter.DeviceListAdapter;
import com.exce.bluetooth.bean.UserInfo;
import com.exce.bluetooth.blueutils.BleController;
import com.exce.bluetooth.blueutils.callback.ConnectCallback;
import com.exce.bluetooth.blueutils.callback.OnReceiverCallback;
import com.exce.bluetooth.blueutils.callback.OnWriteCallback;
import com.exce.bluetooth.blueutils.callback.ScanCallback;
import com.exce.bluetooth.utils.SharedPreferenceUtil;
import com.exce.bluetooth.utils.TypeUntils;
import com.exce.bluetooth.view.EcgView;
import com.google.common.primitives.Shorts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static android.content.ContentValues.TAG;

/**
 * @Author Wangjj
 * @Create 2017/12/21.
 * @Content
 */
public class TabOneFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    /**
     * ---------------蓝牙---------
     */
    private Button btnDevice, btnDisconn, btnSample;
    //连接设备tv
    private TextView connState, sampTime;
    //dialog 上的刷新按钮
    private ImageButton btRefresh;
    //点击设备弹出的dialog
    private Dialog dialog;
    //dialog列表的适配器
    private DeviceListAdapter dvAdapter;
    //蓝牙工具类
    private BleController mBleController;
    //当前连接的mac地址
    private String mDeviceAddress;
    /**
     * 线程
     */
    boolean receivedThreadRuning = false; //数据处理线程运行状态
    /**
     * ------------心电-----------
     */
    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();

    //------------------------------------------
    private View mRootView;
    Toolbar mToolBar;

    public TabOneFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        init(mRootView);
        loadDatas();
        simulator();
        return mRootView;
    }

    //初始化
    @SuppressLint("SetTextI18n")
    public void init(View view) {
        btnDevice = view.findViewById(R.id.btn_device);
        btnDisconn = view.findViewById(R.id.btn_disconn);
        btnSample = view.findViewById(R.id.btn_sample_msg);
        connState = view.findViewById(R.id.tv_conn_state);
        sampTime = view.findViewById(R.id.tv_sampling_time);
        mToolBar = view.findViewById(R.id.tool_bar);
        mBleController = BleController.getInstance().initble(getContext());
        dvAdapter = new DeviceListAdapter(getContext());
        btnDevice.setOnClickListener(this);
        btnDisconn.setOnClickListener(this);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        sampTime.setText(simpleDateFormat.format(date));
        scanDevices(true);
        mToolBar.inflateMenu(R.menu.toolbar_menu);
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //在这里执行我们的逻辑代码
                switch (item.getItemId()) {
                    case R.id.change_usb:
                        Intent intent1 = new Intent(getContext(), USBActivity.class);
                        startActivity(intent1);
                        Toast.makeText(getContext(), "settings", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.change_wifi:
                        Intent intent2 = new Intent(getContext(), WIFIActivity.class);
                        startActivity(intent2);
                        Toast.makeText(getContext(), "settings", Toast.LENGTH_SHORT).show();
                        break;
//                    case R.id.action_settings:
//                        Intent intent3 = new Intent(getContext(), SetActivity.class);
//                        startActivity(intent3);
//                        Toast.makeText(getContext(), "settings", Toast.LENGTH_SHORT).show();
//                        break;
                    default:
                        break;
                }
                return false;
            }

        });
    }

    /**
     * 模拟心电发送，心电数据是一秒500个包，所以
     */
    private void simulator() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (EcgView.isRunning) {
                    if (data0Q.size() > 0) {
                        EcgView.addEcgData0(data0Q.poll());
                    }
                }
            }
        }, 0, 2);
    }

    private void loadDatas() {
        try {
            String data0 = "";
            InputStream in = getResources().openRawResource(R.raw.exceecg);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            data0 = new String(buffer);
            in.close();
            String[] data0s = data0.split(",");
            for (String str : data0s) {
                Float[] fs = new Float[12];
                Float f = Float.parseFloat(str);
                for (int i = 0; i < 12; i++) {
                    fs[i] = f;
                }
                data0Q.add(fs);
            }
        } catch (Exception e) {
        }
    }

    UserInfo ui;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_device:
                //蓝牙连接按钮
                showDeviceListDialog();
                Toast.makeText(getContext(), "蓝牙连接按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_disconn:
                //蓝牙断开按钮
//                mBleController.disConnection();
//                data0Q.clear();
//                EcgView.isRunning = false;
                Toast.makeText(getContext(), "蓝牙断开按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_sample_msg:
                //采集信息  //发送数据指令 发送回调
                UserInfo ui = SharedPreferenceUtil.getUser(getContext(), "ecg", "config_msg");
                byte[] data = mParse(ui);
                mBleController.WriteBuffer(data, new OnWriteCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailed(int state) {

                    }
                });

                Toast.makeText(getContext(), "蓝牙断开按钮", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }


    /**
     * 用自定义协议包装
     *
     * @param ui 包装对象
     * @return byte[] 包装后的对象
     */
    public byte[] mParse(UserInfo ui) {

        try {
            Reflect(ui);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String Reflect(Object model)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Field[] field = model.getClass().getDeclaredFields(); // 获取实体类的所有属性，返回Field数组
        String content = "";
        for (int j = 0; j < field.length; j++) { // 遍历所有属性
            String name = field[j].getName(); // 获取属性的名字
            name = name.substring(0, 1).toUpperCase() + name.substring(1); // 将属性的首字符大写，方便构造get，set方法
            String type = field[j].getGenericType().toString(); // 获取属性的类型
            if (type.equals("class java.lang.String")) { // 如果type是类类型，则前面包含"class
                // "，后面跟类名
                Method m = model.getClass().getMethod("get" + name);
                String value = (String) m.invoke(model); // 调用getter方法获取属性值
                if (value != null) {
                    content += value + "\t";
                }
            }
            if (type.equals("class java.lang.Integer")) {
                Method m = model.getClass().getMethod("get" + name);
                Integer value = (Integer) m.invoke(model);
                if (value != null) {
                    content += value + "\t";
                }
            }
            if (type.equals("class java.lang.Short")) {
                Method m = model.getClass().getMethod("get" + name);
                Short value = (Short) m.invoke(model);
                if (value != null) {
                }
            }
            if (type.equals("class java.lang.Double")) {
                Method m = model.getClass().getMethod("get" + name);
                Double value = (Double) m.invoke(model);
                if (value != null) {
                }
            }
            if (type.equals("class java.lang.Boolean")) {
                Method m = model.getClass().getMethod("get" + name);
                Boolean value = (Boolean) m.invoke(model);
                if (value != null) {
                    String cc = value == true ? "1" : "0";
                    content += cc + "\t";
                }
            }
            if (type.equals("class java.util.Date")) {
                Method m = model.getClass().getMethod("get" + name);
                Date value = (Date) m.invoke(model);
                if (value != null) {
                    content += value + "\t";
                    System.out.println(type + "attribute value:" + value.toLocaleString());
                }
            }
        }
        return content;
    }


    /**
     * 扫描
     *
     * @param enable
     */
    private void scanDevices(final boolean enable) {
        mBleController.ScanBle(enable, new ScanCallback() {
            @Override
            public void onSuccess() {
                if (dvAdapter.mBleDevices.size() < 0) {
                    Toast.makeText(getContext(), "未搜索到Ble设备", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScanning(BluetoothDevice device, int rssi, byte[] scanRecord) {
                dvAdapter.addDevice(device, getDistance(rssi));
            }
        });
    }

    private static final double A_Value = 60; // A - 发射端和接收端相隔1米时的信号强度
    private static final double n_Value = 2.0; //  n - 环境衰减因子

    //根据Rssi获得返回的距离,返回数据单位为m
    public static double getDistance(int rssi) {
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }

    BluetoothDevice device;

    //显示列表dialog
    BluetoothGattCharacteristic characteristic;

    private void showDeviceListDialog() {
        Log.e("  ", "kkkkkkk");
        LayoutInflater factory = LayoutInflater.from(getContext());
        View view = factory.inflate(R.layout.dialog_scan_device, null);
        dialog = new Dialog(getContext(), R.style.MyDialog);
        // ContentView
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
        Button btn_dialgo_cancle = view.findViewById(R.id.btn_dialog_scan_cancle);
        btRefresh = view.findViewById(R.id.btn_refresh);
        ListView listView = view.findViewById(R.id.listview_device);
        listView.setAdapter(dvAdapter);
        Log.e("  ", "ffffff");
        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dvAdapter.clear();
                scanDevices(true);
                Toast.makeText(getContext(), "点击刷新小圈圈。。。", Toast.LENGTH_SHORT).show();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                connState.setText("正在连接。。");
                Toast.makeText(getContext(), "请稍候! " + " " + "正在连接... ", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                device = dvAdapter.getDevice(position);
                if (device == null)
                    return;
                mDeviceAddress = device.getAddress();
                mBleController.Connect(mDeviceAddress, new ConnectCallback() {
                    @Override
                    public void onConnSuccess() {
                        connState.setText("连接成功。。");
                        //连接成功后进行数据协议的解析
                        if (!receivedThreadRuning) {
                            receivedThreadRuning = true;
//                            new receiveThread().start();
                        }
                        ///设置读取数据的监听
                        mBleController.RegistReciveListener(TAG, new OnReceiverCallback() {
                            @Override
                            public void onReceiver(byte[] value) {
                                // Todo
                                byte[] data = characteristic.getValue();
                                for (byte d : data) {
                                    dataB.add(d);
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnFailed() {
                        connState.setText("连接断开。。");
                        Toast.makeText(getContext(), "连接超时，请重试", Toast.LENGTH_SHORT).show();
                    }

                });

            }
        });

        btn_dialgo_cancle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGps();
    }

    /**
     * 开启位置权限
     */
    private void checkGps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanDevices(true);
                Toast.makeText(getContext(), "位置权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "未开启位置权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    /**
     *
     */


    public void bleWrite() {


    }


}
