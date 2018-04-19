package com.exce.bluetooth.bean;

import java.io.Serializable;

/**
 * @Author Wangjj
 * @Create 2018/4/9.
 * @Content
 */
public class UserInfo implements Serializable {

    public String openId;//账号
    public int age;//年龄
    public String height;//身高
    public String userName;//姓名
    public UserInfo.SEX sex;//性别
    public String weight; //体重
    public String phone;//手机号
    public String cid;//身份证
    public String race;//种族
    public String sampleSpeed;//采样速率
    public String gain;//增益
    public String patientType;//病人类型
    public String displayLines;//显示导联
    public String workMode;//工作模式
    public String workData;//工作参数



    public UserInfo() {
        this.sex = UserInfo.SEX.SECRET;
        this.age = 0;
    }

    public UserInfo(String openId, int age, String height, String userName, SEX sex,
                    String weight, String phone, String cid, String race, String sampleSpeed,
                    String gain, String patientType, String displayLines, String workMode, String workData) {
        this.openId = openId;
        this.age = age;
        this.height = height;
        this.userName = userName;
        this.sex = sex;
        this.weight = weight;
        this.phone = phone;
        this.cid = cid;
        this.race = race;
        this.sampleSpeed = sampleSpeed;
        this.gain = gain;
        this.patientType = patientType;
        this.displayLines = displayLines;
        this.workMode = workMode;
        this.workData = workData;
    }

    public static enum SEX {
        SECRET("0"),
        MALE("1"),
        FAMALE("2");

        String string = "";

        private SEX(String var3) {
            this.string = var3;
        }
    }

    public String getOpenId() {
        return openId;
    }

    public UserInfo setOpenId(String openId) {
        this.openId = openId;
        return this;
    }

    public int getAge() {
        return age;
    }

    public UserInfo setAge(int age) {
        this.age = age;
        return this;
    }

    public String getHeight() {
        return height;
    }

    public UserInfo setHeight(String height) {
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

    public SEX getSex() {
        return sex;
    }

    public UserInfo setSex(SEX sex) {
        this.sex = sex;
        return this;
    }

    public String getWeight() {
        return weight;
    }

    public UserInfo setWeight(String weight) {
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

    public String getRace() {
        return race;
    }

    public UserInfo setRace(String race) {
        this.race = race;
        return this;
    }

    public String getSampleSpeed() {
        return sampleSpeed;
    }

    public UserInfo setSampleSpeed(String sampleSpeed) {
        this.sampleSpeed = sampleSpeed;
        return this;
    }

    public String getGain() {
        return gain;
    }

    public UserInfo setGain(String gain) {
        this.gain = gain;
        return this;
    }

    public String getPatientType() {
        return patientType;
    }

    public UserInfo setPatientType(String patientType) {
        this.patientType = patientType;
        return this;
    }

    public String getDisplayLines() {
        return displayLines;
    }

    public UserInfo setDisplayLines(String displayLines) {
        this.displayLines = displayLines;
        return this;
    }

    public String getWorkMode() {
        return workMode;
    }

    public UserInfo setWorkMode(String workMode) {
        this.workMode = workMode;
        return this;
    }

    public String getWorkData() {
        return workData;
    }

    public UserInfo setWorkData(String workData) {
        this.workData = workData;
        return this;
    }
}
