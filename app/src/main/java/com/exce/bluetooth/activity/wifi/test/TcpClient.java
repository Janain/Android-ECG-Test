package com.exce.bluetooth.activity.wifi.test;

import android.content.Intent;

import com.exce.bluetooth.bean.MyField;
import com.exce.bluetooth.bean.UserInfo;
import com.exce.bluetooth.utils.MyObjIterator;
import com.exce.bluetooth.utils.TypeUntils;
import com.google.common.primitives.Shorts;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2018/4/23.
 * @Content
 */
public class TcpClient implements Runnable {

    private String TAG = "TcpClient";
    private String serverIP = "10.1.1.251";
    private int serverPort = 12306;
    private PrintWriter pw;
    private InputStream is;
    private DataInputStream dis;
    private boolean isRun = true;
    private Socket socket = null;
    byte buff[] = new byte[4096];
    private String rcvMsg;
    private int rcvLen;


    public TcpClient(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
    }

    public void closeSelf() {
        isRun = false;
    }

    public void send(String msg) {
        pw.println(msg);
        pw.flush();
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverIP, serverPort);
            socket.setSoTimeout(5000);
            pw = new PrintWriter(socket.getOutputStream(), true);
            is = socket.getInputStream();
            dis = new DataInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startDataHandThread();
        while (isRun) {
            try {

                rcvLen = dis.read(buff);
                for (int i = 0; i < rcvLen; i++) {
                    dataB.add(buff[i]);
//                    rcvMsg = new String(buff, 0, rcvLen,"utf-8");
//                rcvMsg = new String(buff, 0, rcvLen, "utf-8");
//                Log.i(TAG, "run: 收到消息:" + rcvMsg);
                    Intent intent = new Intent();
                    intent.setAction("tcpClientReceiver");
                    intent.putExtra("tcpClientReceiver", dataB.add(buff[i]));
                    System.out.println("------------------------------*************--------------------" + dataB.add(buff[i]));
                    TcpClientActivity.context.sendBroadcast(intent);//将消息发送给主界面
                    if (rcvMsg.equals("QuitClient")) {   //服务器要求客户端结束
                        isRun = false;
                    }

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

    private BlockingQueue<Float[]> data0Q = new LinkedBlockingQueue<>();
    private BlockingQueue<Byte> dataB = new LinkedBlockingQueue<>();

    private boolean dataHandThread_isRunning = false;

    /**
     * 开启数据处理线程
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
                    b = dequeue(dataB);
                    if (b != unsigned_byte(0xaa)) continue;
                    b = dequeue(dataB);
                    if (b != unsigned_byte(0xaa)) continue;
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
                    if (buffer[len - 2] != unsigned_byte(0x55)) continue;
                    if (buffer[len - 1] != unsigned_byte(0x55)) continue;

                    // -------------判断帧类型-------------------------
                    // 帧类型
                    // TODO 判断帧类型，这里默认为数据
                    if (buffer[0] != unsigned_byte(0x32)) continue;

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
            byte[] ins = TypeUntils.byteAppend(insHead, valueLenBuffer, value); // 得到单条指令
            allIns = TypeUntils.byteAppend(allIns, ins); // 追加指令到指令集合中
        }
        byte[] head = new byte[]{TypeUntils.unsigned_byte(0xaa), TypeUntils.unsigned_byte(0xaa)}; // 头
        int allInsLen = (allIns == null) ? 0 : allIns.length;
        byte[] allLen = Shorts.toByteArray((short) (allInsLen + 3)); // 总长度
        byte[] type = new byte[]{TypeUntils.unsigned_byte(0x30)}; // 帧类型
        byte[] end = new byte[]{TypeUntils.unsigned_byte(0x55), TypeUntils.unsigned_byte(0x55)}; // 结束字
        return TypeUntils.byteAppend(head, allLen, type, allIns, end);
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
            b = TypeUntils.float2Bytes((float) obj);
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
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x01);
                break;
            case "age":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x02);
                break;
            case "height":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x03);
                break;
            case "userName":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x04);
                break;
            case "sex":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x05);
                break;
            case "weight":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x06);
                break;
            case "phone":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x07);
                break;
            case "cid":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x08);
                break;
            case "sampleSpeed":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x0a);
                break;
            case "gain":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x0b);
                break;
            case "patientType":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x0c);
                break;
            case "displayLines":
                b[0] = TypeUntils.unsigned_byte(0x00);
                b[1] = TypeUntils.unsigned_byte(0x0d);
                break;
            default:
                throw new RuntimeException("未知的指令名");
        }
        return b;
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
    private byte unsigned_byte(int i) {
        if (i > 255 || i < 0) {
            throw new RuntimeException("i 必须在 0x00 - 0xff 之间");
        }
        if (i > 127) {
            return (byte) (i - 256);
        } else {
            return (byte) i;
        }
    }


}
