package com.exce.bluetooth.ui.activity.wifi.tcp;

import android.content.Intent;
import android.util.Log;

import com.exce.bluetooth.ui.fragment.TabOneFragment;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Shorts;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Wangjj
 * @Create 2018/5/8  10:18.
 * @Title
 */

public class TcpServer implements Runnable {
    private String TAG = "TcpServer";
    private int port = 12306;
    private boolean isListen = true;   //线程监听标志位
    public ArrayList<ServerSocketThread> SST = new ArrayList<ServerSocketThread>();
    public TcpServer(int port) {
        this.port = port;
    }

    //更改监听标志位
    public void setIsListen(boolean b) {
        isListen = b;
    }

    public void closeSelf() {
        isListen = false;
        for (ServerSocketThread s : SST) {
            s.isRun = false;
        }
        SST.clear();
    }

    private Socket getSocket(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "run: 监听超时");
            return null;
        }
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(5000);
            while (isListen) {
                Log.i(TAG, "run: 开始监听...");
                Socket socket = getSocket(serverSocket);
                if (socket != null) {
                    new ServerSocketThread(socket);
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class ServerSocketThread extends Thread {
        Socket socket = null;
        private PrintWriter pw;
        private InputStream is = null;
        private OutputStream os = null;
        private String ip = null;
        private boolean isRun = true;

        ServerSocketThread(Socket socket) {
            this.socket = socket;
            ip = socket.getInetAddress().toString();
            Log.i(TAG, "ServerSocketThread:检测到新的客户端联入,ip:" + ip);
            try {
                socket.setSoTimeout(5000);
                os = socket.getOutputStream();
                is = socket.getInputStream();
                //发送
                sendProtocol(os);
                pw = new PrintWriter(os, true);
                start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void send(String msg){
            pw.println(msg);
            pw.flush(); //强制送出数据
        }

        @Override
        public void run() {
            byte buff[] = new byte[4096];
            String rcvMsg;
            int rcvLen;
            SST.add(this);
            while (isRun && !socket.isClosed() && !socket.isInputShutdown()) {
                try {
                    if ((rcvLen = is.read(buff)) != -1) {
                        rcvMsg = new String(buff, 0, rcvLen, "utf-8");
                        Log.i(TAG, "run:收到消息: " + rcvMsg);
                        Intent intent = new Intent();
                        intent.setAction("tcpServerReceiver");
                        intent.putExtra("tcpServerReceiver", rcvMsg);
                        TabOneFragment.context.sendBroadcast(intent);//将消息发送给主界面
                        if (rcvMsg.equals("QuitServer")) {
                            isRun = false;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                socket.close();
                SST.clear();
                Log.i(TAG, "run: 断开连接");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 服务端发送报文
     * STX (2B)	SERIAL(2B)	LEN (2B)	DATA(nB)	LRC (1B)	ETX (2B)
     *
     * @param out  DataOutputStream

     * @throws IOException 异常
     */
    private void sendProtocol(OutputStream out ) throws IOException {
        // 数据
        byte buff[]  = new byte[4096];
        List<Byte> sendList = new ArrayList<Byte>();

        //帧类型
        byte type = 0x30;
        sendList.add(type);

        //数据长度
        short datalen = (short) ((buff.length) * 12);
        byte[] lenArr = Shorts.toByteArray(datalen);

        for (byte dataLen : lenArr) {
            sendList.add(dataLen);
        }
        //数据 2 * 8
        for (int i = 0; i < 12; i++) {
            for (byte d : buff) {
                sendList.add(d);
            }
        }
        //结束字 2
        byte foot = 0x55;
        sendList.add(foot);
        sendList.add(foot);

        //总长度 2
        short allLen = (short) (sendList.size());
        byte[] allArr = Shorts.toByteArray(allLen);
        for (int i = allArr.length - 1; i >= 0; i--) {
            sendList.add(0, allArr[i]);
        }

        // 协议头 2
        byte head = 0xaa - 256;
        sendList.add(0, head);
        sendList.add(0, head);

        byte[] send = Bytes.toArray(sendList);
        //写入消息内容
        out.write(send);

    }

}
