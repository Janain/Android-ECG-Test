package com.exce.bluetooth.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.exce.bluetooth.bean.Constants;
import com.exce.bluetooth.bean.EcgPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author Wangjj
 * @Create 2018/4/20.
 * @Content 自定义surfaceview控件画心电图
 */
public class EcgView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    public static boolean isRunning = false;
    private int ecgPerCount;//每次画心电数据的个数，8  17
    private static Queue<Float[]> ecg0Datas = new LinkedBlockingQueue<>();
    private Paint mPaint;//画波形图的画笔
    private Paint textPaint;
    private float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());
    private float mWidth;//控件宽度
    private float xOffset; // 2个点之间 x 轴的距离
    private float mHeight;//控件高度
    public Thread RunThread = null;
    private final float XOFFSET_BIG = 100; // x 坐标系偏移值
    public final int XCOUNT = 3 * 500; // x轴 点 总数 3= 秒， 128= 采样率
    public int xLoc = 0; // 当前画笔的位置
    /********--------------------------------------------------------------------------*/
    protected Paint bgPaint; //背景画笔
    protected int mSGridColor = Color.parseColor("#1b4200");//小网格颜色
    protected int mBackgroundColor = Color.BLACK;   //背景颜色
    protected float mSGridSize; //小网格的尺寸( w = h )

    public EcgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderMediaOverlay(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        init();
        Canvas canvas = holder.lockCanvas();
        initBackground(canvas);
        holder.unlockCanvasAndPost(canvas);
        if (!isRunning) {
            startThread();
        }
    }

    private void init() {
        // 初始化宽高 格子大小 x最小偏移值
        this.mWidth = getWidth();
        this.mSGridSize = (mWidth - 100) / 75;
        this.xOffset = (mWidth - 100) / this.XCOUNT;
        this.mHeight = mSGridSize * 205;
        int SAMPLING_RATE = 300;//采样率
        int d = SAMPLING_RATE / 60;
        if (d % 60 != 0) {
            d++;
        }
        this.ecgPerCount = d;
        //初始化绘制盘块的画笔
        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(textSize);
        //背景画笔
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.STROKE);
        //连接处更加平滑
        bgPaint.setStrokeJoin(Paint.Join.ROUND);
        //画线画笔
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        //连接处更加平滑
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setZOrderOnTop(false);
        setZOrderMediaOverlay(false);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }

    //开始画线
    private void startThread() {
        isRunning = true;
        RunThread = new Thread(drawRunnable);
        // 每次开始清空画布，重新画
        ClearDraw();
        RunThread.start();
    }

    //结束画线
    private void stopThread() {
        if (isRunning) {
            isRunning = false;
            RunThread.interrupt();
        }
    }

    Runnable drawRunnable = new Runnable() {
        @Override
        public void run() {
            List<EcgPoint> lastPoints = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                float y = (i + 1) * mSGridSize * 15;
                lastPoints.add(new EcgPoint(xLoc * xOffset, y));
            }
            while (isRunning) {
                try {
                    mDraw(lastPoints);
                } catch (java.lang.IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void mDraw(List<EcgPoint> lastPoints) {
        Rect rect = new Rect();
        int leftLoc = (int) (xLoc * xOffset + XOFFSET_BIG);
        int topLoc = 0;
        int rightLoc = (int) (xLoc * xOffset + XOFFSET_BIG) + 20;
        int bottomLoc = (int) mHeight;
        rect.set(leftLoc, topLoc, rightLoc, bottomLoc);
        Canvas mCanvas = surfaceHolder.lockCanvas(rect);
        try {
            initBackground(mCanvas);
            drawLines(mCanvas, lastPoints, ecgPerCount);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            surfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /**
     * 画曲线
     *
     * @param mCanvas    画布
     * @param lastPoints 记录上次画的点
     * @param count      每次画的个数
     */
    private void drawLines(Canvas mCanvas, List<EcgPoint> lastPoints, int count) {
        //画曲线
        for (int i = 0; i < count; i++) {
            float stopX = this.xLoc * this.xOffset + this.XOFFSET_BIG;
            float[] stopY = ecgConver(ecg0Datas.poll());
            for (int j = 0; j < 12; j++) {
                float startX = lastPoints.get(j).getX();
                float startY = lastPoints.get(j).getY();
                mCanvas.drawLine(startX, startY, stopX, stopY[j], mPaint);
                lastPoints.get(j).setX(stopX);
                lastPoints.get(j).setY(stopY[j]);
            }
            this.xLoc++;
            this.xLoc = this.xLoc % XCOUNT == 0 ? 0 : this.xLoc;
        }
    }

    /**
     * 添加数据
     *
     * @param data 心电数据
     */
    public static void addEcgData0(Float[] data) {
        ecg0Datas.offer(data);
    }

    /**
     * 将心电数据转换成用于显示的Y坐标
     *
     * @param datas 心电数据
     * @return float[]
     */
    private float[] ecgConver(Float[] datas) {
        float yOffset = mSGridSize * 15;
        float[] fArr = new float[12];
        if (datas == null) {
            for (int i = 0; i < 12; i++) {
                fArr[i] = (i + 1) * yOffset;
            }
            return fArr;
        }
        for (int i = 0; i < 12; i++) {
            if (datas[i] == null) {
                fArr[i] = (i + 1) * yOffset;
            } else {
//                float mv = (float) (datas[i] * 2.4 / 8388607 * 1000 / 6);
//                fArr[i] = -(mv * this.mSGridSize * 10) + (i + 1) * yOffset;
                float mv = (datas[i] - 1024) * (10f / 2048) / 2;
                fArr[i] = -(mv * this.mSGridSize * 10) + (i + 1) * yOffset;

            }
        }
        return fArr;
    }

    /**
     * 画文本
     */
    private String[] mStrs;

    private void drawText(Canvas mCanvas) {
        float yOffset = mSGridSize * 15;
        for (int i = 0; i < 12; i++) {
            mStrs = new String[]{"I", "II", "III", "aVR", "aVL", "aVF", "V1", "V2", "V3", "V4", "V5", "V6"};
            for (int j = 0; j < 12; j++) {
                float hOffset = 10;
                float vOffset = (i + 1) * yOffset;
                mCanvas.drawText(mStrs[i], hOffset, vOffset, textPaint);
            }
        }
    }

    /**
     * 画网格背景
     *
     * @param canvas 画布
     */
    private void initBackground(Canvas canvas) {
        canvas.drawColor(mBackgroundColor);
        bgPaint.setColor(mSGridColor);
        float startX, startY, stopX, stopY;
        float xOffset = 100;
        //画竖线
        startY = 0;
        stopY = mHeight;
        for (int i = 0; i <= 75; i++) {
            startX = stopX = i * mSGridSize + xOffset;
            if (i % 5 == 0) {//每隔5个格子粗体显示
                bgPaint.setStrokeWidth(4);
                drawText(canvas);
                canvas.drawLine(startX, startY, stopX, stopY, bgPaint);
            } else {
                bgPaint.setStrokeWidth(1);
                canvas.drawLine(startX, startY, stopX, stopY, bgPaint);
            }
        }
        //画横线
        startX = xOffset;
        stopX = mWidth + xOffset;
        for (int i = 0; i <= 205; i++) {
            startY = stopY = i * mSGridSize;
            if (i % 5 == 0) {//每隔5个格子粗体显示
                bgPaint.setStrokeWidth(4);
                canvas.drawLine(startX, startY, stopX, stopY, bgPaint);
            } else {
                bgPaint.setStrokeWidth(1);
                canvas.drawLine(startX, startY, stopX, stopY, bgPaint);
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
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

}
