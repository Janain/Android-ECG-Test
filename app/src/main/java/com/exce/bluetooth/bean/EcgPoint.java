package com.exce.bluetooth.bean;

/**
 * 点
 * @Author Wangjj
 * @Create 2018/3/29.
 * @Content
 */
public class EcgPoint {
    private float x;
    private float y;

    public EcgPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
