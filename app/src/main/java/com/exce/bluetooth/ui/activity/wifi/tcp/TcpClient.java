package com.exce.bluetooth.ui.activity.wifi.tcp;

import android.content.Intent;
import android.util.Log;

import com.exce.bluetooth.ui.activity.wifi.WifiConfigActivity;
import com.exce.bluetooth.ui.fragment.TabOneFragment;
import com.exce.bluetooth.utils.Utils;
import com.google.common.primitives.Shorts;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2018/5/8  10:18.
 * @Title
 */

public class TcpClient implements Runnable {
    private String TAG = "TcpClient";

    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();
    private boolean dataHandThread_isRunning = false; //数据处理线程

    private String  serverIP = "10.1.1.251";
    private int serverPort = 12306;
    private PrintWriter pw;
    private InputStream is;
    private DataInputStream dis;
    private boolean isRun = true;
    private Socket socket = null;
    byte buff[]  = new byte[4096];
    private String rcvMsg;
    private int rcvLen;



    public TcpClient(String ip , int port ){
        this.serverIP = ip;
        this.serverPort = port;

    }

    public void closeSelf(){
        isRun = false;
    }

    public void send(String msg){
        pw.println(msg);
        pw.flush();
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverIP,serverPort);
            socket.setSoTimeout(5000);
            pw = new PrintWriter(socket.getOutputStream(),true);
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            genProtocol();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (isRun){
            try {
                rcvLen = dis.read(buff);
                rcvMsg = new String(buff,0,rcvLen,"utf-8");
                Log.i(TAG, "run: 收到消息:"+ rcvMsg);
                Intent intent =new Intent();
                intent.setAction("tcpClientReceiver");
                intent.putExtra("tcpClientReceiver",rcvMsg);
                WifiConfigActivity.context.sendBroadcast(intent);//将消息发送给主界面
                if (rcvMsg.equals("QuitClient")){   //服务器要求客户端结束
                    isRun = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            pw.close();
            is.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 客户端解析报文
     * STX (2B)	SERIAL(2B)	LEN (2B)	DATA(nB)	LRC (1B)	ETX (2B)
     */
    private void genProtocol() {
        if (!dataHandThread_isRunning) {
            dataHandThread_isRunning = true;
            new Thread(() -> {
                // 数据
                byte[] buffer = new byte[4096];
                int len;

                while (dataHandThread_isRunning) {
                    // 取协议头
                    byte b;
                    b = Utils.dequeue(dataB);
                    if (b != Utils.unsigned_byte(0xD3)) continue;
                    b = Utils.dequeue(dataB);
                    if (b != Utils.unsigned_byte(0x96)) continue;
                    //stral
                    for (int i = 0; i < 2; i++) {
                        buffer[i] = Utils.dequeue(dataB);
                    }
                    // 取len
                    for (int i = 0; i < 2; i++) {
                        buffer[i] = Utils.dequeue(dataB);
                    }
                    len = Shorts.fromBytes(buffer[2], buffer[3]);

                    // 取剩下的
                    for (int i = 0; i < len; i++) {
                        buffer[i] = Utils.dequeue(dataB);
                    }

                    // 判断协议完整性(判断尾或crc)
                    if (buffer[len - 2] != Utils.unsigned_byte(0Xd6)) continue;
                    if (buffer[len - 1] != Utils.unsigned_byte(0x93)) continue;

                    //LRC:从STX字段开始到ETX字段逐个进行异或的值
                    byte temp = 0;
                    for (byte lrc : buffer) {
                        temp ^= lrc;
                    }
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

}
