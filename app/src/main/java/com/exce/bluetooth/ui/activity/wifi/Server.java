package com.exce.bluetooth.ui.activity.wifi;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Shorts;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Wangjj
 * @Create 2018/4/8.
 * @Content
 */
public class Server {
    //协议头 2 字节 总长度 2 字节 帧类型 2 字节 数据长度 2 字节 数据 2 字节 结束字 2 字节
    private static void genProtocol(DataOutputStream out, short data) throws IOException {
        // 数据
        byte[] dataBuffer = Shorts.toByteArray(data);
        List<Byte> sendList = new ArrayList<Byte>();

        //帧类型
        byte type = 0x30;
        sendList.add(type);

        //数据长度
        short datalen = (short) ((dataBuffer.length) * 12);
        byte[] lenArr = Shorts.toByteArray(datalen);

        for (byte dataLen : lenArr) {
            sendList.add(dataLen);
        }
        //数据 2 * 8
        for (int i = 0; i < 12; i++) {
            for (byte d : dataBuffer) {
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
        for (int i = allArr.length-1; i >= 0 ; i--) {
            sendList.add(0, allArr[i]);
        }

        // 协议头 2
        byte head = 0xaa - 256;
        sendList.add(0,head);
        sendList.add(0,head);

        byte[] send = Bytes.toArray(sendList);
        //写入消息内容
        out.write(send);

    }

    public class CommunicateThread extends Thread {
        Socket socket;
        DataInputStream dis;
        DataOutputStream dos;
        String filename = "D:\\Exce_company\\exceecg.txt";

        public CommunicateThread(Socket socket) {
            this.socket = socket;
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            super.run();
            System.out.println("1连接......");
            OutputStream os = null;
            try {
                os = socket.getOutputStream();
                DataOutputStream outs = new DataOutputStream(os);
                String dataStr = readFileByLines(filename);
                String[] dataStrArr = dataStr.split(",");
                System.out.println("开始发送");
                int count = 0;
                boolean sendover = false;
                while (!sendover) {
                    for (String d : dataStrArr) {
                        count++;
                        genProtocol(outs, Short.parseShort(d));
//                        Thread.sleep(10);
                        System.out.println("----------------data: " + d);
                    }
                    sendover = true;
                    System.out.println("tatal received: " + count);
                    System.out.println("发送完成");
                }
                System.out.println("关闭");

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public void startServer() {
        try {
            //服务器在9990端口监听客户端的连接
            ServerSocket ss = new ServerSocket(12306);
            System.out.println("server is listening...");
            while (true) {
                //阻塞的accept方法，当一个客户端连接上，才会返回Socket对象
                Socket s = ss.accept();
                System.out.println("a client has connected!");

                //开启线程处理通信
                new CommunicateThread(s).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static String readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            String temp = "";
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                System.out.println("line " + line + ": " + tempString);
                temp += tempString;
                line++;
            }
            reader.close();
            return temp;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return readFileByLines(fileName);
    }


    public static void main(String[] args) {
        new Server().startServer();
    }
}
