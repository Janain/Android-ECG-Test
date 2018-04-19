package com.exce.bluetooth.activity.wifi;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.exce.bluetooth.R;
import com.exce.bluetooth.activity.SetActivity;
import com.exce.bluetooth.activity.usb.USBActivity;
import com.exce.bluetooth.view.EcgView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2018/4/8.
 * @Content
 */
public class WIFIActivity extends AppCompatActivity implements View.OnClickListener,Toolbar.OnMenuItemClickListener {

    /**
     * ------------心电-----------
     */
    private BlockingQueue<Float[]> datas = new LinkedBlockingQueue<Float[]>();
    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<Float[]>();

    /**
     * -------------------wifi--------------
     *
     * @param savedInstanceState
     */
    private Toolbar mToolBar;
    private EditText ipTv, portTv;
    private Button btnConn, btnDisconn;
    private String ip, port;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_wifi);
        init();
        start();
    }

    private void init() {

        mToolBar = findViewById(R.id.tool_bar);
        mToolBar.inflateMenu(R.menu.toolbar_menu);
        mToolBar.setOnMenuItemClickListener(this);

        ipTv = findViewById(R.id.edit_tcpClientIp);
        portTv = findViewById(R.id.edit_tcpClientPort);
        btnConn = findViewById(R.id.btn_conn_wifi);
        btnDisconn = findViewById(R.id.btn_disconn_wifi);
        ip = ipTv.getText().toString();
        port = portTv.getText().toString();
        btnConn.setOnClickListener(this);
        btnDisconn.setOnClickListener(this);

    }

    public void start() {
        try {
            //客户端
            //1、创建客户端Socket，指定服务器地址和端口
            Socket socket = new Socket("localhost", 10086);
            //2、获取输出流，向服务器端发送信息
            OutputStream outputStream = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(outputStream);//将输出流包装成打印流
            pw.write("把数据转为界面");
            pw.flush();
            socket.shutdownOutput();
            //3、获取输入流，并读取服务器端的响应信息
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String info = null;
            while ((info = bufferedReader.readLine()) != null) {
                loadDatas();
                simulator();
                System.out.println("我是客户端，服务器说：" + info);
            }

            //4、关闭资源
            bufferedReader.close();
            inputStream.close();
            pw.close();
            outputStream.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                datas.add(fs);
            }
            data0Q.addAll(datas);
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_conn_wifi:
                new Thread() {
                    @Override
                    public void run() {
//                        start();
                    }
                };
                break;
            case R.id.btn_disconn_wifi:

                break;
            default:
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        //在这里执行我们的逻辑代码
        switch (item.getItemId()) {
            case R.id.change_usb:
                Intent intent1 = new Intent(getApplicationContext(), USBActivity.class);
                startActivity(intent1);
                Toast.makeText(getApplication(), "usb", Toast.LENGTH_SHORT).show();
                break;

            case R.id.change_wifi:
                Intent intent2 = new Intent(getApplicationContext(), WIFIActivity.class);
                startActivity(intent2);
                Toast.makeText(getApplication(), "wifi", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_settings:
                Intent intent3 = new Intent(getApplicationContext(), SetActivity.class);
                startActivity(intent3);
                Toast.makeText(getApplication(), "settings", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
            return false;
    }
}
