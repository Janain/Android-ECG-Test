package com.exce.bluetooth.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.exce.bluetooth.R;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import static android.content.ContentValues.TAG;

/**
 * @Author Wangjj
 * @Create 2018/3/27.
 * @Content
 */
public class EcgSurfaceview extends SurfaceView implements SurfaceHolder.Callback{
    private Context mContext;
    private SurfaceHolder surfaceHolder;
    public static boolean isRunning;
    private Canvas mCanvas;

    private float ecgMax = 4096;//心电的最大值
    private String bgColor = "#3FB57D";
    private int wave_speed = 25;//波速: 25mm/s
    private int sleepTime = 8; //每次锁屏的时间间距，单位:ms
    private float lockWidth;//每次锁屏需要画的
    private int ecgPerCount = 8;//每次画心电数据的个数，心电每秒有500个数据包

    private static Queue<Integer> ecg0Datas = new LinkedList<Integer>();
    private static Queue<Integer> ecg1Datas = new LinkedList<Integer>();

    private Paint mPaint;//画波形图的画笔
    private int mWidth;//控件宽度
    private int mHeight;//控件高度
    private float ecgYRatio;// 心电图 y轴 的比例
    private int startY0;//Y0轴起始
    private int startY1;//y1轴起始
    private int yOffset1;//波2的Y坐标偏移值
    private Rect rect;//更新

    private int startX;//每次画线的X坐标起点
    private double ecgXOffset;//每次X坐标偏移的像素
    private int blankLineWidth = 6;//右侧空白点的宽度

    private static SoundPool soundPool;
    private static int soundId;//心跳提示音

    public EcgSurfaceview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        rect = new Rect();
        converXOffset();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(6);

        ecgXOffset = lockWidth / ecgPerCount;//心电图x偏移值=每次锁屏需要画的/每次画心电数据的个数
        startY0 = mHeight * (1 / 4);//波1初始Y坐标是控件高度的1/4
        startY1 = mHeight & (3 / 4);
        ecgYRatio = mHeight / 2 / ecgMax;// 心电图 y轴 的比例=屏幕的高度/2/心电图最大值
        Log.e(TAG, "height+width: " + mHeight + " " + mWidth);
        Log.e(TAG, "height+width: " + mHeight + " " + mWidth);

        yOffset1 = mHeight / 2;//Y偏移值=屏幕的高度/2
    }

    /**
     * 根据波速计算每次X坐标增加的像素
     * <p>
     * 计算出每次锁屏应该画的px值
     */
    private void converXOffset() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        //获取屏幕对角线的长度，单位:px
        double diagonalMm = Math.sqrt(width * width + height * height) / dm.densityDpi;//单位：英寸
        diagonalMm = diagonalMm * 2.54 * 10;//转换单位为：毫米
        double diagonalPx = width * width + height * height;
        diagonalPx = Math.sqrt(diagonalPx);
        //每毫米有多少px
        double px1mm = diagonalPx / diagonalMm;
        //每秒画多少px
        double px1s = wave_speed * px1mm;
        //每次锁屏所需画的宽度
        lockWidth = (float) (px1s * (sleepTime / 1000f));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
//        canvas.drawColor(Color.parseColor(bgColor));
        holder.unlockCanvasAndPost(canvas);
        startThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        isRunning = true;
        init();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }

    private void startThread() {
        isRunning = true;
        new Thread(drawRunnable).start();
    }

    private void stopThread() {
        isRunning = false;
    }

    Runnable drawRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                long startTime = System.currentTimeMillis();

                startDrawWave();

                long endTime = System.currentTimeMillis();
                if (endTime - startTime < sleepTime) {
                    try {
                        Thread.sleep(sleepTime - (endTime - startTime));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private void startDrawWave() {
        rect.set(startX, 0, (int) (startX + lockWidth + blankLineWidth), mHeight);
        mCanvas = surfaceHolder.lockCanvas(rect);
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.parseColor(bgColor));
//        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        drawWave0(mCanvas);
        drawWave1(mCanvas);

        surfaceHolder.unlockCanvasAndPost(mCanvas);

        startX = (int) (startX + lockWidth);
        if (startX > mWidth) {
            startX = 0;
        }
    }

    /**
     * 画波1
     */
    private void drawWave0(Canvas canvas) {
        try {
            float mStartX = startX;
            if (ecg0Datas.size() > ecgPerCount) {
                for (int i = 0; i < ecgPerCount; i++) {
                    float newX = (float) (mStartX + ecgXOffset);
                    int newY = ecgConver(ecg0Datas.poll());
                    canvas.drawLine(mStartX, startY0, newX, newY, mPaint);
                    mStartX = newX;
                    startY0 = newY;
                }
            } else {
                /**
                 * 如果没有数据
                 * 因为有数据一次画ecgPerCount个数，那么无数据时候就应该画ecgPercount倍数长度的中线
                 */
                int newX = (int) (mStartX + ecgXOffset * ecgPerCount);
                int newY = ecgConver((int) (ecgMax / 2));
                canvas.drawLine(mStartX, startY0, newX, newY, mPaint);
                startY0 = newY;
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    /**
     * 画波2
     */
    private void drawWave1(Canvas canvas) {
        try {
            float mStartX = startX;
            if (ecg1Datas.size() > ecgPerCount) {
                for (int i = 0; i < ecgPerCount; i++) {
                    float newX = (float) (mStartX + ecgXOffset);
                    int newY = ecgConver(ecg1Datas.poll()) + yOffset1;
                    canvas.drawLine(mStartX, startY1, newX, newY, mPaint);
                    mStartX = newX;
                    startY1 = newY;
                }
            } else {
                /**
                 * 如果没有数据
                 * 因为有数据一次画ecgPerCount个数，那么无数据时候就应该画ecgPercount倍数长度的中线
                 */
                int newX = (int) (mStartX + ecgXOffset * ecgPerCount);
                int newY = ecgConver((int) (ecgMax / 2)) + yOffset1;
                canvas.drawLine(mStartX, startY1, newX, newY, mPaint);
                startY1 = newY;
            }
        } catch (NoSuchElementException e) {
        }
    }

    /**
     * 将心电数据转换成用于显示的Y坐标
     *
     * @param data
     * @return
     */
    private int ecgConver(int data) {
        data = (int) (ecgMax - data);
        data = (int) (data * ecgYRatio);// 心电图 y轴 的比例

        return data;
    }

    public static void addEcgData0(int data) {
        ecg0Datas.add(data);
    }

    public static void addEcgData1(int data) {
        ecg1Datas.add(data);
    }

}
