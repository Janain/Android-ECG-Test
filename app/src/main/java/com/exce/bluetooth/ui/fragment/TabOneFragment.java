package com.exce.bluetooth.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.exce.bluetooth.R;
import com.exce.bluetooth.ui.activity.ble.BLEActivity;
import com.exce.bluetooth.ui.activity.usb.USBActivity;
import com.exce.bluetooth.bean.MyField;
import com.exce.bluetooth.bean.UserInfo;
import com.exce.bluetooth.utils.MyObjIterator;
import com.exce.bluetooth.utils.SharedPreferenceUtil;
import com.exce.bluetooth.utils.Untils;
import com.exce.bluetooth.view.EcgView;
import com.google.common.primitives.Shorts;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2017/12/21.
 * @Content
 */
public class TabOneFragment extends Fragment {
    //---------------------心电---------------------
    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();
    private boolean dataHandThread_isRunning = false; //数据处理线程
    //------------------------------------------
    private View mRootView;
    private Toolbar mToolBar;
    private TextView txtSend;
    private EditText editPort, editSendMsg;
    private Button btnConn, btnClientSend, btnOpen;
    private MyBtnClicker myBtnClicker = new MyBtnClicker();

    //--------------------------------------------
    public TabOneFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        init(mRootView);
        Untils.hideIputKeyboard(getContext());

        return mRootView;
    }


    /**
     * 绑定ID
     */
    private void init(View view) {

        editPort = view.findViewById(R.id.edit_tcpClientPort);
        editSendMsg = view.findViewById(R.id.edit_tcpClientSend);
        txtSend = view.findViewById(R.id.tv_send);
        btnConn = view.findViewById(R.id.btn_conn_wifi);
        btnOpen = view.findViewById(R.id.btn_open_wifi);
        btnClientSend = view.findViewById(R.id.btn_tcpClientSend);
        btnConn.setOnClickListener(myBtnClicker);
        btnOpen.setOnClickListener(myBtnClicker);
        btnClientSend.setOnClickListener(myBtnClicker);
        btnClientSend.setEnabled(false);

        mToolBar = view.findViewById(R.id.tool_bar);
        mToolBar.inflateMenu(R.menu.toolbar_menu);
        mToolBar.setOnMenuItemClickListener(item -> {
            //在这里执行我们的逻辑代码
            switch (item.getItemId()) {
                case R.id.change_usb:
                    Intent intent1 = new Intent(getContext(), USBActivity.class);
                    startActivity(intent1);
                    Toast.makeText(getContext(), "settings", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.change_ble:
                    Intent intent2 = new Intent(getContext(), BLEActivity.class);
                    startActivity(intent2);
                    Toast.makeText(getContext(), "settings", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        });
    }


    /**
     * 监听点击事件
     */
    private class MyBtnClicker implements View.OnClickListener {
        private static final String TAG = "MyBtnClicker";

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_conn_wifi://开始连接
                    Log.i(TAG, "onClick: 开始");
                    btnConn.setEnabled(false);
                    btnClientSend.setEnabled(true);
                    new GetLogTask().execute();
                    simulator();
                    break;
                case R.id.btn_tcpClientSend://发送消息
//                    Message message = Message.obtain();
//                    message.what = 2;
//                    message.obj = editSendMsg.getText().toString();
//                    myHandler.sendMessage(message);
//                    exec.execute(() -> tcpClient.send(editSendMsg.getText().toString()));
                    break;
                case R.id.btn_open_wifi://发送消息
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * socket
     */
    @SuppressLint("StaticFieldLeak")
    public class GetLogTask extends AsyncTask<Void, Void, String> {
        //doInBackground执行完后由UI线程调用，用于更新界面操作
        @Override
        protected String doInBackground(Void... voids) {
            try {
                Socket s = new Socket("10.1.1.251", 12306);
                System.out.println("连接成功......");
                OutputStream ous = s.getOutputStream();
                DataOutputStream dos = new DataOutputStream(ous);
                //采集信息  //发送数据指令 发送回调
                UserInfo ui = SharedPreferenceUtil.getUser(getContext(), "ecg", "config_msg");
                byte[] data = mParse(ui);
                dos.write(data);

                InputStream is = s.getInputStream();
                DataInputStream dis = new DataInputStream(is);
                startDataHandThread();
                int len;
                byte[] buffer = new byte[4096];
                while ((len = dis.read(buffer)) != -1) {
                    for (int i = 0; i < len; i++) {
                        dataB.add(buffer[i]);
                        System.out.println("------------------------------*************--------------------" + buffer[i]);
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "";
        }

    }

    /**
     * 开启数据处理线程
     */
    private void startDataHandThread() {
        if (!dataHandThread_isRunning) {
            dataHandThread_isRunning = true;
            new Thread(() -> {
                byte[] buffer = new byte[4096];
                int len;
                int stral;
                while (dataHandThread_isRunning) {
                    // 取协议头
                    byte b;
                    b = Untils.dequeue(dataB);
                    if (b != Untils.unsigned_byte(0xD3)) continue;
                    b = Untils.dequeue(dataB);
                    if (b != Untils.unsigned_byte(0x96)) continue;
                    //stral
                    for (int i = 0; i < 2; i++) {
                        buffer[i] = Untils.dequeue(dataB);
                    }

                    // 取len
                    for (int i = 0; i < 2; i++) {
                        buffer[i] = Untils.dequeue(dataB);
                    }
                    len = Shorts.fromBytes(buffer[2], buffer[3]);

                    // 取剩下的
                    for (int i = 0; i < len; i++) {
                        buffer[i] = Untils.dequeue(dataB);
                    }

                    // 判断协议完整性(判断尾或crc)
                    if (buffer[len - 2] != Untils.unsigned_byte(0Xd6)) continue;
                    if (buffer[len - 1] != Untils.unsigned_byte(0x93)) continue;

                    //lrc
//
//                        if (buffer[len - 3]==  ){
//
//                        }


                    // 数据
                    Float[] f = new Float[12];

                    for (int i = 0; i < len / 2; i++) {
                        f[i] = (float) Shorts.fromBytes(buffer[2 * i + 4], buffer[2 * i + 5]);
                    }
                    data0Q.add(f);
                }
            }).start();
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
     * 对比UserInfo  更新
     *
     * @param old  旧的userinfo
     * @param newU 新的userinfo
     * @return UserInfo
     */
    public UserInfo compare(UserInfo old, UserInfo newU) {
        UserInfo u = new UserInfo();

        if (!newU.getOpenId().equals(old.getOpenId())) {
            u.setOpenId(newU.getOpenId());
        }
        if (newU.getAge() == newU.getAge()) {
            u.setAge(newU.getAge());
        }
        if (newU.getHeight() == newU.getHeight()) {
            u.setHeight(newU.getHeight());
        }
        if (!newU.getUserName().equals(newU.getUserName())) {
            u.setUserName(newU.getUserName());
        }
        if (newU.getSex() == newU.getSex()) {
            u.setSex(newU.getSex());
        }
        if (newU.getWeight() == newU.getWeight()) {
            u.setWeight(newU.getWeight());
        }
        if (!newU.getPhone().equals(newU.getPhone())) {
            u.setPhone(newU.getPhone());
        }
        if (!newU.getCid().equals(newU.getCid())) {
            u.setCid(newU.getCid());
        }
        if (newU.getSampleSpeed() == newU.getSampleSpeed()) {
            u.setSampleSpeed(newU.getSampleSpeed());
        }
        if (newU.getGain() == newU.getGain()) {
            u.setGain(newU.getGain());
        }
        if (newU.getPatientType() == newU.getPatientType()) {
            u.setPatientType(newU.getPatientType());
        }
        if (newU.getDisplayLines() == newU.getDisplayLines()) {
            u.setDisplayLines(newU.getDisplayLines());
        }

        return u;
    }

    /**
     * 对象转byte数组（自定义）
     *
     * @param obj  obj
     * @param type type
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
     * @param name String
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

    /**
     * 显示心电图
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
}
