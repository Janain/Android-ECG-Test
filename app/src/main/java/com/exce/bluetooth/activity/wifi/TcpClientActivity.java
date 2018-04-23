package com.exce.bluetooth.activity.wifi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.exce.bluetooth.R;
import com.exce.bluetooth.view.EcgView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @Author Wangjj
 * @Create 2018/4/23.
 * @Content
 */
public class TcpClientActivity extends Activity {

    private static final String TAG = "TcpClientActivity";
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private TextView txtSend;
    private EditText editIp, editPort, editSendMsg;
    private Button btnConn, btnDisconn, btnClientSend;
    private static TcpClient tcpClient = null;
    private MyBtnClicker myBtnClicker = new MyBtnClicker();
    private final MyHandler myHandler = new MyHandler(this);
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();
    ExecutorService exec = Executors.newCachedThreadPool();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_wifi);
        init();
        bindListener();
        bindReceiver();
    }

    /**
     * 绑定ID
     */
    private void init() {
        editIp = findViewById(R.id.edit_tcpClientIp);
        editPort = findViewById(R.id.edit_tcpClientPort);
        editSendMsg = findViewById(R.id.edit_tcpClientSend);
        txtSend = findViewById(R.id.tv_send);
        btnConn = findViewById(R.id.btn_conn_wifi);
        btnDisconn = findViewById(R.id.btn_disconn_wifi);
        btnClientSend = findViewById(R.id.btn_tcpClientSend);
    }

    /***
     * 绑定监听
     */
    private void bindListener() {
        btnConn.setOnClickListener(myBtnClicker);
        btnDisconn.setOnClickListener(myBtnClicker);
        btnClientSend.setOnClickListener(myBtnClicker);
        btnDisconn.setEnabled(false);
        btnClientSend.setEnabled(false);
    }

    /**
     * 绑定广播
     */
    private void bindReceiver() {
        IntentFilter intentFilter = new IntentFilter("tcpClientReceiver");
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    /**
     * 获取端口号
     *
     * @param msg 端口
     * @return int
     */
    private int getPort(String msg) {
        if (msg.equals("")) {
            msg = "12306";
        }
        return Integer.parseInt(msg);
    }

    /**
     * 监听点击事件
     */
    private class MyBtnClicker implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_conn_wifi://开始连接
                    Log.i(TAG, "onClick: 开始");
                    btnConn.setEnabled(false);
                    btnDisconn.setEnabled(true);
                    btnClientSend.setEnabled(true);
                    tcpClient = new TcpClient(editIp.getText().toString(), getPort(editPort.getText().toString()));
                    exec.execute(tcpClient);
                    break;
                case R.id.btn_disconn_wifi://断开连接
                    tcpClient.closeSelf();
                    btnConn.setEnabled(true);
                    btnDisconn.setEnabled(false);
                    btnClientSend.setEnabled(false);
                    break;
                case R.id.btn_tcpClientSend://发送消息
                    Message message = Message.obtain();
                    message.what = 2;
                    message.obj = editSendMsg.getText().toString();
                    myHandler.sendMessage(message);
                    exec.execute(() -> tcpClient.send(editSendMsg.getText().toString()));
                    break;
            }
        }
    }
    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();


    /**
     * 更新界面UI
     */
    private class MyHandler extends Handler {
        private WeakReference<TcpClientActivity> mActivity;

        MyHandler(TcpClientActivity activity) {
            mActivity = new WeakReference<TcpClientActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity != null) {
                switch (msg.what) {
                    case 1:
                        //TODO 显示心电图
                        if (EcgView.isRunning) {
                            if (data0Q.size() > 0) {
                                EcgView.addEcgData0(data0Q.poll());
                            }
                        }
                        break;
                    case 2:
                        //TODO 向下位机发送数据
                        txtSend.append(msg.obj.toString());
                        break;
                }
            }
        }
    }

    /**
     * 广播接收消息
     */
    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            switch (mAction) {
                case "tcpClientReceiver":
                    String msg = intent.getStringExtra("tcpClientReceiver");
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = msg;
                    myHandler.sendMessage(message);
                    break;
            }
        }
    }


}
