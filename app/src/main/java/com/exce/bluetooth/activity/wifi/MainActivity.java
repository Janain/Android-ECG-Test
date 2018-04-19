package com.exce.bluetooth.activity.wifi;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.exce.bluetooth.R;
import com.exce.bluetooth.view.EcgView;
import com.google.common.primitives.Shorts;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2018/4/11.
 * @Content
 */
public class MainActivity extends AppCompatActivity {

    /**
     * ------------心电-----------
     */
    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();

    private boolean dataHandThread_isRunning = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Activity按钮事件中
        GetLogTask task = new GetLogTask();
        task.execute();

        simulator();
    }

    /**
     * socket
     */
    public class GetLogTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            try {
                Socket s = new Socket("10.1.1.251", 12306);

                System.out.println("连接成功......");
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
     *  开启数据处理线程
     */
    private void startDataHandThread() {
        if (!dataHandThread_isRunning) {
            dataHandThread_isRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[4096];
                    int len;
                    while (dataHandThread_isRunning) {
                        // 取协议头
                        byte b;
                        b = dequeue(dataB);
                        if (b != unsigned(0xaa)) continue;
                        b = dequeue(dataB);
                        if (b != unsigned(0xaa)) continue;
                        // 取总长度
                        for (int i = 0; i < 2; i++) {
                            buffer[i] = dequeue(dataB);
                        }
                        len = Shorts.fromBytes(buffer[0], buffer[1]);

                        // 取剩下的
                        for (int i = 0; i < len; i++) {
                            buffer[i] = dequeue(dataB);
                        }

                        // 判断协议完整性(判断尾或crc)
                        if (buffer[len - 2] != unsigned(0x55)) continue;
                        if (buffer[len - 1] != unsigned(0x55)) continue;

                        // -------------判断帧类型-------------------------
                        // 帧类型
                        // TODO 判断帧类型，这里默认为数据
                        if (buffer[0] != 0x30) continue;

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
                }
            }).start();
        }
    }

    /**
     * 取出队列中的一个数据
     *
     * @param queue
     * @return
     */
    private byte dequeue(BlockingQueue<Byte> queue) {
        Byte b = null;
        do {
            b = queue.poll();
            if (b == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (b == null);
        return b;
    }

    /**
     * 模拟unsigned 的 byte类型
     *
     * @param i
     * @return
     */
    private byte unsigned(int i) {
        if (i > 255 || i < 0) {
            throw new RuntimeException("i 必须在 0x00 - 0xff 之间");
        }
        if (i > 127) {
            return (byte) (i - 256);
        } else {
            return (byte) i;
        }
    }


    /**
     * 显示心电图入口
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
