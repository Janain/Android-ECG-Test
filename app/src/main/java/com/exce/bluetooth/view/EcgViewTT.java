package com.exce.bluetooth.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 采样率 ： 1s/ 1000包数据  ，  走纸速度：1s/25mm
 * Custom electrocardiogram
 * <p>
 * 1. Solve the background grid drawing problem
 * 2. Real-time data padding
 * <p>
 * author Bruce Young
 * 2017年8月7日10:54:01
 */

public class EcgViewTT extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    public static boolean isRunning = false;
    public static boolean isRead = false;
    private Canvas mCanvas;

    private String bgColor = "#00000000";
    public static int wave_speed = 25;//波速: 25mm/s   25
    private int sleepTime = 14; //每次锁屏的时间间距 8，单位:ms   8
    private float lockWidth;//每次锁屏需要画的
    private int ecgPerCount = 17;//每次画心电数据的个数，8  17
    private static Queue<Float> ecg0Datas = new LinkedBlockingQueue<>();
    private Paint mPaint;//画波形图的画笔
    private int mWidth;//控件宽度
    private int mHeight;//控件高度
    private float startY0;
    private Rect rect;
    private int ecgWidth = 4;
    public Thread RunThread = null;
    private float startX = 0;//每次画线的X坐标起点
    public static float ecgXOffset;//每次X坐标偏移的像素
    public static float widthStart = 0f;  // 宽度开始的地方（横屏）
    public static float highStart = 0f;  // 高度开始的地方（横屏）
    public static float ecgSensitivity = 2;  // 1 的时候代表 5g 一大格  2 的时候 10g 一大格
    public static float baseLine = 2f / 4f;

    // 背景 网格 相关属性
    //画笔
    protected Paint mbgPaint;
    //网格颜色
    protected int mGridColor = Color.parseColor("#1b4200");
    //背景颜色
    protected int mBackgroundColor = Color.BLACK;
    // 小格子 个数
    protected int mGridWidths = 40;
    // 横坐标个数
    private int mGridHighs = 0;

    public EcgViewTT(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        rect = new Rect();
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EcgView);
//        ecgWidth = typedArray.getInteger(R.styleable.EcgView_ecgWidth,4);
//        mGridColor = typedArray.getColor(R.styleable.EcgView_checkeredColor,Color.parseColor("#1b4200"));
//        typedArray.recycle();
        converXOffset();
    }

    private void init() {
        mbgPaint = new Paint();
        mbgPaint.setAntiAlias(true);
        mbgPaint.setStyle(Paint.Style.STROKE);
        //连接处更加平滑
        mbgPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        //连接处更加平滑
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, wave_speed, dm); // 25mm --> px
        ecgXOffset = size / 1000f;
        startY0 = -1;//波1初始Y坐标是控件高度的1/2
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
        double px1s = 25 * px1mm;
        //每次锁屏所需画的宽度
        lockWidth = (float) (px1s * (sleepTime / 1000f));
        float widthSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 25, dm); // 25mm --> px
        widthStart = (width % widthSize) / 2;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.parseColor(bgColor));
        initBackground(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //          surfaceview透明
//        holder.setFormat(PixelFormat.TRANSPARENT);
        //surfceview放置在顶层，即始终位于最上层
        setZOrderOnTop(false);
        setZOrderMediaOverlay(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int high = dm.heightPixels;
        float widthSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 25, dm); // 25mm --> px
        widthStart = (width % widthSize) / 2;
        w = floatToInt(w - widthStart);
        // TODO: 2017/11/21 暂时使用固定的 25mm/s
        mGridWidths = (floatToInt(w / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 25, dm)) * 5);
        mWidth = w;

        float highSize = 0f;
        if (high / widthSize >= 3) {
            highSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 25, dm);
            mGridHighs = (floatToInt(high / highSize) * 5);
            highStart = (high % highSize) / 2;
            h = floatToInt(h - highStart);
        } else {
            highStart = high % 3;
            high = (int) (high - highStart);
            highSize = high / 15;
            mGridHighs = 15;
            h = floatToInt(h - highStart);
        }
        mHeight = h;
        isRunning = false;
        init();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int high = MeasureSpec.getSize(heightMeasureSpec);
        Log.e("ecgview:", "width：" + width + " height:" + high);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }

    public void startThread() {
        isRunning = true;
        RunThread = new Thread(drawRunnable);
        // 每次开始清空画布，重新画
        ClearDraw();
        startY0 = -1;
        RunThread.start();
    }

    public void stopThread() {
        if (isRunning) {
            isRunning = false;
            RunThread.interrupt();
            startX = 0;
            startY0 = -1;
        }
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
                        break;
                    }
                }
            }
        }
    };

    private void startDrawWave() {
        //锁定画布修改 位置
        int blankLineWidth = 5;
        rect.set((int) (startX), 0, (int) (startX + lockWidth + blankLineWidth), mHeight);
        mCanvas = surfaceHolder.lockCanvas(rect);
        if (mCanvas == null) return;
        mCanvas.drawColor(Color.parseColor(bgColor));
        drawWave0();
        surfaceHolder.unlockCanvasAndPost(mCanvas);
    }

    /**
     * 画 脉象
     */
    private void drawWave0() {
        try {
            float mStartX = startX;
            initBackground(mCanvas);
            if (ecg0Datas.size() > ecgPerCount) {
                for (int i = 0; i < ecgPerCount; i++) {
                    float newX = (mStartX + ecgXOffset);
                    float newY = (mHeight * baseLine) - (ecg0Datas.poll() * ((mHeight / mGridHighs) /   ecgSensitivity));
                    if (startY0 != -1) {
                        mCanvas.drawLine(mStartX, startY0, newX, newY, mPaint);
                    }
                    mStartX = newX;
                    startY0 = newY;

                    startX = mStartX;
                    if (startX >= mWidth) {
                        startX = 0;
                        mStartX = 0;
                    }
                }
            } else {
                // 清空画布
                if (isRead) {
                    if (startY0 == -1) {
                        startX = 0;
                    }
                    Paint paint = new Paint();
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    mCanvas.drawPaint(paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    initBackground(mCanvas);
                    stopThread();
                }
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    public static void addEcgData0(Float data) {
        ecg0Datas.offer(data);
    }

    public static void clearEcgData0() {
        if (ecg0Datas.size() > 0) {
            ecg0Datas.clear();
        }
    }

    //绘制背景 网格
    private void initBackground(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);
        //小格子的尺寸
        int latticeWidth = mWidth / mGridWidths;
        int latticeHigh = mHeight / mGridHighs;

        mbgPaint.setColor(mGridColor);

        for (int k = 0; k <= mWidth / latticeWidth; k++) {
            if (k % 5 == 0) {//每隔5个格子粗体显示
                mbgPaint.setStrokeWidth(2);
                canvas.drawLine(k * latticeWidth, 0, k * latticeWidth, mHeight, mbgPaint);
            } else {
                mbgPaint.setStrokeWidth(1);
                canvas.drawLine(k * latticeWidth, 0, k * latticeWidth, mHeight, mbgPaint);
            }
        }
            /* 宽度 */
        for (int g = 0; g <= mHeight / latticeHigh; g++) {
            if (g % 5 == 0) {
                mbgPaint.setStrokeWidth(2);
                canvas.drawLine(0, g * latticeHigh, mWidth, g * latticeHigh, mbgPaint);
            } else {
                mbgPaint.setStrokeWidth(1);
                canvas.drawLine(0, g * latticeHigh, mWidth, g * latticeHigh, mbgPaint);
            }
        }
    }

    /**
     * 清空 画布
     */
    public void ClearDraw() {
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas(null);
            canvas.drawColor(Color.WHITE);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
            // 绘制网格
            initBackground(canvas);
        } catch (Exception e) {

        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public static int floatToInt(float f) {
        int i = 0;
        if (f > 0) {
            i = (int) ((f * 10 + 5) / 10);
        } else if (f < 0) {
            i = (int) ((f * 10 - 5) / 10);
        } else i = 0;
        return i;
    }

}
