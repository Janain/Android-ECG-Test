package com.exce.bluetooth.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.exce.bluetooth.R;
import com.exce.bluetooth.bean.UserInfo;
import com.exce.bluetooth.utils.SharedPreferenceUtil;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @Author Wangjj
 * @Create 2018/4/10.
 * @Content
 */
public class PersonalActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private EditText mAge, mHeight, mUserName, mSex, mWeight, mPhone, mCid, mRace;
    private Button save;
    private Spinner mSampleSpeed, mGain, mPaintType, mDisplayLines, mWorkMode, mWorkData;
    private UserInfo ui;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peronal);
        mAge = findViewById(R.id.ecg_age);
        mHeight = findViewById(R.id.ecg_height);
        mUserName = findViewById(R.id.ecg_name);
        mSex = findViewById(R.id.ecg_sex);
        mWeight = findViewById(R.id.wcg_weight);
        mPhone = findViewById(R.id.ecg_phone);
        mCid = findViewById(R.id.ecg_cid);
        mRace = findViewById(R.id.ecg_race);
        mSampleSpeed = findViewById(R.id.ecg_sample_speed);
        mGain = findViewById(R.id.ecg_gain);
        mPaintType = findViewById(R.id.ecg_paint_type);
        mDisplayLines = findViewById(R.id.ecg_display_lines);
        mWorkMode = findViewById(R.id.ecg_work_mode);
        mWorkData = findViewById(R.id.ecg_work_data);

        mSampleSpeed.setOnItemSelectedListener(this);
        mGain.setOnItemSelectedListener(this);
        mPaintType.setOnItemSelectedListener(this);
        mDisplayLines.setOnItemSelectedListener(this);
        mWorkMode.setOnItemSelectedListener(this);
        mWorkData.setOnItemSelectedListener(this);

        save = findViewById(R.id.ecg_save);
        save.setOnClickListener(this);
        initArgs();
        testFanshe();

    }


    // 初始化参数
    private void initArgs() {
        ui = new UserInfo();

        ui.height = "" + getEditTextValue(mHeight);
        ui.userName = "" + getEditTextValue(mUserName);
        ui.sex = getSexValue(getEditTextValue(mSex));
        ui.weight = "" + getEditTextValue(mWeight);
        ui.phone = "" + getEditTextValue(mPhone);
        ui.cid = "" + getEditTextValue(mCid);
        ui.race = "" + getEditTextValue(mRace);
        ui.setAge(getInt(getEditTextValue(mAge)))
                .setHeight(ui.height)
                .setUserName(ui.userName)
                .setSex(ui.sex)
                .setWeight(ui.weight)
                .setPhone(ui.phone)
                .setCid(ui.cid)
                .setRace(ui.race)
                .setSampleSpeed("360")
                .setGain("2")
                .setPatientType("儿童")
                .setDisplayLines("12")
                .setWorkMode("低功耗")
                .setWorkData("无");
    }

    @Override
    public void onClick(View v) {
        //点击保存个人信息数据
        try {
            SharedPreferenceUtil.saveUser(getApplicationContext(), "ecg", "config_msg", ui);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.ecg_sample_speed:
                String[] sampleArr = getResources().getStringArray(R.array.sample_speed);//获取采样速率列表数据
//                ui.setSampleSpeed(sampleArr[position]);
                Log.e("onItemSelected: ", "---------sampleArr--------" + sampleArr[position]);
                break;
            case R.id.ecg_gain:
                String[] gainArr = getResources().getStringArray(R.array.gain);//获取增益列表数据
                ui.setGain(gainArr[position]);
                Log.e("onItemSelected: ", "---------gainArr--------" + gainArr[position]);
                break;
            case R.id.ecg_paint_type:
                String[] paintArr = getResources().getStringArray(R.array.patient_type);//获取病人类型列表数据
                ui.setPatientType(paintArr[position]);
                Log.e("onItemSelected: ", "---------paintArr--------" + paintArr[position]);
                break;
            case R.id.ecg_display_lines:
                String[] linesArr = getResources().getStringArray(R.array.display_line);//获取显示导联列表数据
                ui.setDisplayLines(linesArr[position]);
                Log.e("onItemSelected: ", "---------linesArr--------" + linesArr[position]);
                break;
            case R.id.ecg_work_mode:
                String[] modeArr = getResources().getStringArray(R.array.work_mode);//获取工作模式列表数据
                ui.setWorkMode(modeArr[position]);
                Log.e("onItemSelected: ", "---------modeArr--------" + modeArr[position]);
                break;
            case R.id.ecg_work_data:
                String[] dataArr = getResources().getStringArray(R.array.work_data);//获取工作参数列表数据
                ui.setWorkData(dataArr[position]);
                Log.e("onItemSelected: ", "---------dataArr--------" + dataArr[position]);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private void testFanshe() {
        UserInfo ui = SharedPreferenceUtil.getUser(getApplicationContext(), "ecg", "config_msg");
        try {
            String data = mParse(ui);
            System.out.println("++++++++++++++++++-------------------+++++++++++++++ " + data);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用自定义协议包装
     *
     * @param ui 包装对象
     * @return byte[] 包装后的对象
     */

//        System.out.println("*********" + gson.toJson(ui.sex));
//        System.out.println("*****666666666****" + gson.toJson(ui.sex.getClass().getTypeParameters()));
//        ui = new UserInfo().setAge(1).setCid("1q231231");
    public String mParse(UserInfo ui) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ui = new UserInfo();
        String content = "";
        Field[] filed = ui.getClass().getDeclaredFields();
        for (int i = 0; i < filed.length; i++) {
            Class type = filed[i].getType();
            Type genericType = filed[i].getGenericType();
            String name = filed[i].getName();
            System.out.println("类型 ----:" + type + "---获得通用类型---" + genericType + "---名称----：" + name);
        }

        return content;
    }


    private static Object reflectMethod(Object object) {
        Field[] field = object.getClass().getDeclaredFields(); // 获取实体类的所有属性，返回Field数组
//        Method[] methods = object.getClass().getMethod("")
        for (int i = 0; i < field.length; i++) {
            String name = field[i].getName(); // 获取属性的名字
            field[i].getType();

            field[i].getDeclaringClass();

        }
        List l = new ArrayList();
        l.add("aa");
        l.add("bb");
        l.add("cc");

        Iterator iter = l.iterator();
        while (iter.hasNext()) {
            String str = (String) iter.next();
            System.out.println(str);
        }

        return null;

    }


    /**
     * 文本框值为Int
     *
     * @param value
     * @return
     */
    private int getInt(String value) {
        int getValue = 0;
        if (emptyString(value)) {
            return getValue;
        }
        try {
            getValue = Integer.parseInt(value);
        } catch (Exception e) {
            getValue = -1;
        }

        return getValue;
    }

    /**
     * 判断空字符串
     *
     * @param str
     * @return
     */
    private boolean emptyString(String str) {
        if (str == null)
            return true;
        if (str.equals(""))
            return true;
        return false;
    }

    /**
     * 年龄表示
     *
     * @param sex
     * @return
     */
    private UserInfo.SEX getSexValue(String sex) {
        if (emptyString(sex)) {
            return UserInfo.SEX.SECRET;
        } else if (sex.equals("男")) {
            return UserInfo.SEX.MALE;
        } else if (sex.equals("女")) {
            return UserInfo.SEX.FAMALE;
        } else if (sex.equals("保密")) {
            return UserInfo.SEX.SECRET;
        }
        return null;
    }

    /**
     * 获取EditView文本框内容
     *
     * @param et
     * @return
     */
    private String getEditTextValue(EditText et) {
        return et.getText().toString().trim();
    }

}
