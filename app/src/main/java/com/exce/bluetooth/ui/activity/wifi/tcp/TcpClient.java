package com.exce.bluetooth.ui.activity.wifi.tcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @Author Wangjj
 * @Create 2018/5/8  10:18.
 * @Title
 */

public class TcpClient implements Runnable {
    private String TAG = "TcpClient";
    private String  serverIP = "10.1.1.251";
    private int serverPort = 12306;
    private PrintWriter pw;
    private boolean isRun = true;
    private Socket socket = null;



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
            pw = new PrintWriter(socket.getOutputStream(),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("-------------------------------------------------------------------------------连接成功");
    }
}
