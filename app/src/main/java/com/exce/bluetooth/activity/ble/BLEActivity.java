package com.exce.bluetooth.activity.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.exce.bluetooth.R;
import com.exce.bluetooth.activity.usb.USBActivity;
import com.exce.bluetooth.adapter.DeviceListAdapter;
import com.exce.bluetooth.bean.MyField;
import com.exce.bluetooth.bean.UserInfo;
import com.exce.bluetooth.blueutils.BleController;
import com.exce.bluetooth.blueutils.callback.ConnectCallback;
import com.exce.bluetooth.blueutils.callback.OnWriteCallback;
import com.exce.bluetooth.blueutils.callback.ScanCallback;
import com.exce.bluetooth.utils.MyObjIterator;
import com.exce.bluetooth.utils.SharedPreferenceUtil;
import com.exce.bluetooth.utils.Untils;
import com.exce.bluetooth.view.EcgView;
import com.google.common.primitives.Shorts;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2018/4/23  16:58.
 * @Title 蓝牙测试
 */
public class BLEActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final String TAG = "BLEActivity";
    //-----------------------蓝牙------------------
    private Toolbar mToolBar;
    private Button btnDevice, btnDisconn, btnSample;
    private TextView connState, sampTime;//连接设备tv
    private ImageButton btRefresh;//dialog 上的刷新按钮
    private Dialog dialog;  //点击设备弹出的dialog
    private DeviceListAdapter dvAdapter; //dialog列表的适配器
    private BleController mBleController; //蓝牙工具类
    private String mDeviceAddress; //当前连接的mac地址

    private boolean dataHandThread_isRunning = false; //数据处理线程运行状态

    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_ble);
        init();
    }

    //初始化
    @SuppressLint("SetTextI18n")
    public void init() {
        btnDevice = findViewById(R.id.btn_device);
        btnDisconn = findViewById(R.id.btn_disconn);
        btnSample = findViewById(R.id.btn_sample_msg);
        connState = findViewById(R.id.tv_conn_state);
        sampTime = findViewById(R.id.tv_sampling_time);
        mToolBar = findViewById(R.id.tool_bar);
        mBleController = BleController.getInstance().initble(this);
        dvAdapter = new DeviceListAdapter(this);
        btnDevice.setOnClickListener(this);
        btnDisconn.setOnClickListener(this);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        sampTime.setText(simpleDateFormat.format(date));
        scanDevices(true);
        mToolBar.inflateMenu(R.menu.toolbar_menu);
        mToolBar.setOnMenuItemClickListener(item -> {
            //在这里执行我们的逻辑代码
            switch (item.getItemId()) {
                case R.id.change_usb:
                    Intent intent1 = new Intent(getApplicationContext(), USBActivity.class);
                    startActivity(intent1);
                    Toast.makeText(getApplicationContext(), "settings", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.change_ble:
                    Intent intent2 = new Intent(getApplicationContext(), BLEActivity.class);
                    startActivity(intent2);
                    Toast.makeText(getApplicationContext(), "settings", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
            return false;
        });
    }

    /**
     * 模拟心电发送，心电数据是一秒300个包，所以
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


    /**
     * 扫描
     *
     * @param enable 是否打开
     */
    private void scanDevices(final boolean enable) {
        mBleController.ScanBle(enable, new ScanCallback() {
            @Override
            public void onSuccess() {
                if (dvAdapter.mBleDevices.size() < 0) {
                    Toast.makeText(getApplicationContext(), "未搜索到Ble设备", Toast.LENGTH_SHORT).show();
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

    /**
     * 蓝牙连接并读取数据
     */
    private void showDeviceListDialog() {
        Log.e("  ", "kkkkkkk");
        LayoutInflater factory = LayoutInflater.from(this);
        View view = factory.inflate(R.layout.dialog_scan_device, null);
        dialog = new Dialog(this, R.style.MyDialog);
        // ContentView
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.show();
        Button btn_dialgo_cancle = view.findViewById(R.id.btn_dialog_scan_cancle);
        btRefresh = view.findViewById(R.id.btn_refresh);
        ListView listView = view.findViewById(R.id.listview_device);
        listView.setAdapter(dvAdapter);
        Log.e("  ", "ffffff");
        btRefresh.setOnClickListener(v -> {
            dvAdapter.clear();
            scanDevices(true);
            Toast.makeText(getApplicationContext(), "点击刷新小圈圈。。。", Toast.LENGTH_SHORT).show();
        });
        listView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            connState.setText("正在连接。。");
            Toast.makeText(getApplicationContext(), "请稍候! " + " " + "正在连接... ", Toast.LENGTH_SHORT).show();
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
                    startDataHandThread();
                    ///设置读取数据的监听
                    mBleController.RegistReciveListener(TAG, value -> {
                        // Todo
                        byte[] data = characteristic.getValue();
                        for (byte d : data) {
                            dataB.add(d);
                        }
                        simulator();
                    });
                }

                @Override
                public void onConnFailed() {
                    connState.setText("连接断开。。");
                    Toast.makeText(getApplicationContext(), "连接超时，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        });
        btn_dialgo_cancle.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * 连接成功后进行数据协议的解析
     */
    private void startDataHandThread() {
        if (!dataHandThread_isRunning) {
            dataHandThread_isRunning = true;
            new Thread(() -> {
                byte[] buffer = new byte[4096];
                int len;
                while (dataHandThread_isRunning) {
                    // 取协议头
                    byte b;
                    b = Untils.dequeue(dataB);
                    if (b != Untils.unsigned_byte(0xaa)) continue;
                    b = Untils.dequeue(dataB);
                    if (b != Untils.unsigned_byte(0xaa)) continue;
                    // 取总长度
                    for (int i = 0; i < 2; i++) {
                        buffer[i] = Untils.dequeue(dataB);
                    }
                    len = Shorts.fromBytes(buffer[0], buffer[1]);

                    // 取剩下的
                    for (int i = 0; i < len; i++) {
                        buffer[i] = Untils.dequeue(dataB);
                    }

                    // 判断协议完整性(判断尾或crc)
                    if (buffer[len - 2] != Untils.unsigned_byte(0x55)) continue;
                    if (buffer[len - 1] != Untils.unsigned_byte(0x55)) continue;

                    // -------------判断帧类型-------------------------
                    // 帧类型
                    // TODO 判断帧类型，这里默认为数据
                    if (buffer[0] != Untils.unsigned_byte(0x32)) continue;

                    //--------以下为数据帧解析---------------
                    // 数据长度
                    short datalen = Shorts.fromBytes(buffer[1], buffer[2]);
                    // 数据
                    Float[] f = new Float[12];
                    for (int i = 0; i < datalen / 2; i++) {
                        f[i] = (float) Shorts.fromBytes(buffer[2 * i + 3], buffer[2 * i + 4]);
                    }
                    data0Q.add(f);
                }
            }).start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_device:
                //蓝牙连接按钮
                showDeviceListDialog();
                Toast.makeText(getApplicationContext(), "蓝牙连接按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_disconn:
                //蓝牙断开按钮
                mBleController.disConnection();
                data0Q.clear();
                EcgView.isRunning = false;
                Toast.makeText(getApplicationContext(), "蓝牙断开按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_sample_msg:
                //采集信息  //发送数据指令 发送回调
                UserInfo ui = SharedPreferenceUtil.getUser(getApplicationContext(), "ecg", "config_msg");
                byte[] data = mParse(ui);
                mBleController.WriteBuffer(data, new OnWriteCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailed(int state) {

                    }
                });
                Toast.makeText(getApplicationContext(), "蓝牙断开按钮", Toast.LENGTH_SHORT).show();
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
        MyObjIterator iterator = new MyObjIterator(ui);

        byte[] allIns = null;
        while (iterator.hasNext()) {
            MyField field = iterator.next();
            if (field.getValue() == null) continue;
            String name = field.getName();
            byte[] insHead = getInsHead(name); // 指令头
            byte[] value = objToByte(field.getValue(), field.getType()); // 指令数据
            short valueLen = (short) value.length;
            byte[] valueLenBuffer = Shorts.toByteArray(valueLen); // 指令数据长度

            byte[] ins = Untils.byteAppend(insHead, valueLenBuffer, value); // 得到单条指令
            allIns = Untils.byteAppend(allIns, ins); // 追加指令到指令集合中
        }
        byte[] head = new byte[]{Untils.unsigned_byte(0xaa), Untils.unsigned_byte(0xaa)}; // 头
        int allInsLen = (allIns == null) ? 0 : allIns.length;
        byte[] allLen = Shorts.toByteArray((short) (allInsLen + 3)); // 总长度
        byte[] type = new byte[]{Untils.unsigned_byte(0x30)}; // 帧类型
        byte[] end = new byte[]{Untils.unsigned_byte(0x55), Untils.unsigned_byte(0x55)}; // 结束字
        return Untils.byteAppend(head, allLen, type, allIns, end);
    }

    /**
     * 对象转byte数组（自定义）
     *
     * @param obj
     * @param type
     * @return byte[]
     */
    public byte[] objToByte(Object obj, Type type) {
        byte[] b = null;
        if (type == String.class) {
            b = String.valueOf(obj).getBytes(StandardCharsets.UTF_8);
        } else if (type == byte.class) {
            b = new byte[]{(byte) obj};
        } else if (type == short.class) {
            b = Shorts.toByteArray((short) obj);
        } else if (type == float.class) {
            b = Untils.float2Bytes((float) obj);
        }
        return b;
    }

    /**
     * 获取指令头
     *
     * @param name
     * @return 指令
     */
    public byte[] getInsHead(String name) {
        byte[] b = new byte[2];
        switch (name) {
            case "openId":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x01);
                break;
            case "age":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x02);
                break;
            case "height":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x03);
                break;
            case "userName":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x04);
                break;
            case "sex":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x05);
                break;
            case "weight":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x06);
                break;
            case "phone":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x07);
                break;
            case "cid":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x08);
                break;
            case "sampleSpeed":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x0a);
                break;
            case "gain":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x0b);
                break;
            case "patientType":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x0c);
                break;
            case "displayLines":
                b[0] = Untils.unsigned_byte(0x00);
                b[1] = Untils.unsigned_byte(0x0d);
                break;
            default:
                throw new RuntimeException("未知的指令名");
        }
        return b;
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
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
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
                Toast.makeText(getApplicationContext(), "位置权限已开启", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "未开启位置权限", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


}
