package com.exce.bluetooth.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
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
import com.exce.bluetooth.ui.activity.wifi.WifiConfigActivity;
import com.exce.bluetooth.ui.activity.wifi.tcp.TcpServer;
import com.exce.bluetooth.utils.Utils;
import com.exce.bluetooth.view.EcgView;

import java.lang.ref.WeakReference;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2017/12/21.
 * @Content
 */
public class TabOneFragment extends Fragment {
    public static Context context;
    //---------------------心电---------------------
    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();
    private boolean dataHandThread_isRunning = false; //数据处理线程
    //------------------------------------------
    private View mRootView;
    private Toolbar mToolBar;
    private EditText editServerPort;
    private TextView txtServerIp;
    private Button btnStartServer, btnCloseServer;
    private MyBtnClicker myBtnClicker = new MyBtnClicker();
    private static TcpServer tcpServer = null;
    private   MyHandler myHandler = new MyHandler( );
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    ExecutorService exec = Executors.newCachedThreadPool();


    public TabOneFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_home_wifi, container, false);
        init(mRootView);
        bindReceiver();
        Utils.hideIputKeyboard(getContext());
        return mRootView;
    }


    /**
     * 绑定ID
     */
    private void init(View view) {
        btnStartServer = view.findViewById(R.id.btn_tcpServerConn);
        btnCloseServer = view.findViewById(R.id.btn_tcpServerClose);
        txtServerIp = view.findViewById(R.id.txt_Server_Ip);
        editServerPort = view.findViewById(R.id.edit_Server_Port);

        btnStartServer.setOnClickListener(myBtnClicker);
        btnCloseServer.setOnClickListener(myBtnClicker);

        btnCloseServer.setEnabled(false);
        txtServerIp.setText(getHostIP());

        mToolBar = view.findViewById(R.id.tool_bar);
        mToolBar.inflateMenu(R.menu.toolbar_menu);
        mToolBar.setOnMenuItemClickListener(item -> {
            //在这里执行我们的逻辑代码
            switch (item.getItemId()) {
                case R.id.change_usb:
                    Intent intent1 = new Intent(getContext(), USBActivity.class);
                    startActivity(intent1);
                    Toast.makeText(getContext(), "选择usb", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.change_ble:
                    Intent intent2 = new Intent(getContext(), BLEActivity.class);
                    startActivity(intent2);
                    Toast.makeText(getContext(), "选择ble", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.change_wifi_config:
                    Intent intent3 = new Intent(getContext(), WifiConfigActivity.class);
                    startActivity(intent3);
                    Toast.makeText(getContext(), "WiFi配置", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        });
    }


    private static class MyHandler extends android.os.Handler {
        private final WeakReference<TabOneFragment> mActivity;

        MyHandler(TabOneFragment activity) {
            mActivity = new WeakReference<TabOneFragment>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TabOneFragment activity = mActivity.get();
            if (activity != null) {
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

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            switch (mAction) {
                case "tcpServerReceiver":
                    String msg = intent.getStringExtra("tcpServerReceiver");
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = msg;
                    myHandler.sendMessage(message);
                    break;
            }
        }
    }

    private void bindReceiver() {
        IntentFilter intentFilter = new IntentFilter("tcpServerReceiver");
//        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    /**
     * 监听点击事件
     */
    private class MyBtnClicker implements View.OnClickListener {
        private static final String TAG = "MyBtnClicker";

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_tcpServerConn:
                    Log.i("A", "onClick: 开启服务端");
                    btnStartServer.setEnabled(false);
                    btnCloseServer.setEnabled(true);
                    tcpServer = new TcpServer(getHost(editServerPort.getText().toString()));
                    exec.execute(tcpServer);
                    break;
                case R.id.btn_tcpServerClose:
                    tcpServer.closeSelf();
                    btnStartServer.setEnabled(true);
                    btnCloseServer.setEnabled(false);
                    break;
                default:
                    break;
            }
        }
    }

    private int getHost(String msg) {
        if (msg.equals("")) {
            msg = "12306";
        }
        return Integer.parseInt(msg);
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }

        } catch (SocketException e) {
            Log.i("FuncTcpServer", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

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
