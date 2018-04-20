package com.exce.bluetooth.bean;

import java.io.Serializable;

/**
 * @Author Wangjj
 * @Create 2018/4/9.
 * @Content
 */
public class UserInfo implements Serializable {
    private String openId;//账号
    private byte age;//年龄
    private float height;//身高
    private String userName;//姓名
    private byte sex;//性别
    private float weight; // 体重
    private String phone;//手机号
    private String cid;//身份证
    private short sampleSpeed;//采样速率
    public byte gain; //增益
    private byte patientType;// 病人类型
    private byte displayLines;// 显示通道 8

    public String getOpenId() {
        return openId;
    }

    public UserInfo setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    public byte getAge() {
        return age;
    }

    public UserInfo setAge(byte age) {
        this.age = age;
        return this;
    }

    public float getHeight() {
        return height;
    }

    public UserInfo setHeight(float height) {
        this.height = height;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public UserInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public byte getSex() {
        return sex;
    }

    public UserInfo setSex(byte sex) {
        this.sex = sex;
        return this;
    }

    public float getWeight() {
        return weight;
    }

    public UserInfo setWeight(float weight) {
        this.weight = weight;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserInfo setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getCid() {
        return cid;
    }

    public UserInfo setCid(String cid) {
        this.cid = cid;
        return this;
    }

    public short getSampleSpeed() {
        return sampleSpeed;
    }

    public UserInfo setSampleSpeed(short sampleSpeed) {
        this.sampleSpeed = sampleSpeed;
        return this;
    }

    public byte getGain() {
        return gain;
    }

    public UserInfo setGain(byte gain) {
        this.gain = gain;
        return this;
    }

    public byte getPatientType() {
        return patientType;
    }

    public UserInfo setPatientType(byte patientType) {
        this.patientType = patientType;
        return this;
    }

    public byte getDisplayLines() {
        return displayLines;
    }

    public UserInfo setDisplayLines(byte displayLines) {
        this.displayLines = displayLines;
        return this;
    }

}
