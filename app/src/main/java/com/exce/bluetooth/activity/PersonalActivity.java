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


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

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
        ui.age = getInt(getEditTextValue(mAge));
        ui.height = "" + getEditTextValue(mHeight);
        ui.userName = "" + getEditTextValue(mUserName);
        ui.sex = getSexValue(getEditTextValue(mSex));
        ui.weight = "" + getEditTextValue(mWeight);
        ui.phone = "" + getEditTextValue(mPhone);
        ui.cid = "" + getEditTextValue(mCid);
        ui.race = "" + getEditTextValue(mRace);
        ui.setAge(ui.age)
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
                ui.setSampleSpeed(sampleArr[position]);
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
        byte[] data = mParse(ui);
        System.out.println("++++++++++++++++++-------------------+++++++++++++++ " + data);
        System.out.println("++++++++++++++++++-------------------+++++++++++++++ " + data);
        System.out.println("++++++++++++++++++-------------------+++++++++++++++ " + data);
    }

    /**
     * 用自定义协议包装
     *
     * @param ui 包装对象
     * @return byte[] 包装后的对象
     */
    public byte[] mParse(UserInfo ui) {
        ui = new UserInfo();
        Class mClass = ui.getClass();
        System.out.println("获取ui的类名" + mClass);
        try {
            Field field = mClass.getDeclaredField("userName");
            Field field1 = mClass.getDeclaredField("age");
            Field field2 = mClass.getDeclaredField("openId");
            System.out.println("获取ui的成员变量userName" + field + " ," + field1 + "," + field2);
            Field[] filed1 = mClass.getDeclaredFields();
            Class type = field1.getType();
            for (Field f : filed1) {
                System.out.println("Declared Field----类型 :" + type.getName() + "---成员变量----：" + f.getName());
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        try {
            ui = new UserInfo();
            Class mClass1 = ui.getClass();
            System.out.println("class的名称" + mClass1.getName());
            Method[] methods = mClass1.getMethods();
            System.out.println("获取该类自己声明的方法" + mClass1.getDeclaredMethods());
            for (int i = 0; i < methods.length; i++) {
                Class returns = methods[i].getReturnType();
                System.out.print("得到方法的返回值类型" + returns.getName() + "");
                System.out.print("便利后的方法名" + methods[i].getName() + "(");
//                System.out.println("得到返回值参数的类类类型");
                Class[] classArr = methods[i].getParameterTypes();
                for (Class class1 :
                        classArr) {
                    System.out.print(class1.getName() + ",");
                }
                System.out.println(")");
            }

            Reflect(ui);
            System.out.println("ui的对象" + Reflect(mClass1));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String Reflect(Object model)
            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Field[] field = model.getClass().getDeclaredFields(); // 获取实体类的所有属性，返回Field数组
        String content = "";
        for (int j = 0; j < field.length; j++) { // 遍历所有属性
            String name = field[j].getName(); // 获取属性的名字
            name = name.substring(0, 1).toUpperCase() + name.substring(1); // 将属性的首字符大写，方便构造get，set方法
            String type = field[j].getGenericType().toString(); // 获取属性的类型
            if (type.equals("class java.lang.String")) { // 如果type是类类型，则前面包含"class
                // "，后面跟类名
                Method m = model.getClass().getMethod("get" + name);
                String value = (String) m.invoke(model); // 调用getter方法获取属性值
                if (value != null) {
                    content += value + "\t";
                }
            }
            if (type.equals("class java.lang.Integer")) {
                Method m = model.getClass().getMethod("get" + name);
                Integer value = (Integer) m.invoke(model);
                if (value != null) {
                    content += value + "\t";
                }
            }
            if (type.equals("class java.lang.Short")) {
                Method m = model.getClass().getMethod("get" + name);
                Short value = (Short) m.invoke(model);
                if (value != null) {
                }
            }
            if (type.equals("class java.lang.Double")) {
                Method m = model.getClass().getMethod("get" + name);
                Double value = (Double) m.invoke(model);
                if (value != null) {
                }
            }
            if (type.equals("class java.lang.Boolean")) {
                Method m = model.getClass().getMethod("get" + name);
                Boolean value = (Boolean) m.invoke(model);
                if (value != null) {
                    String cc = value == true ? "1" : "0";
                    content += cc + "\t";
                }
            }
            if (type.equals("class java.util.Date")) {
                Method m = model.getClass().getMethod("get" + name);
                Date value = (Date) m.invoke(model);
                if (value != null) {
                    content += value + "\t";
                    System.out.println(type + "attribute value:" + value.toLocaleString());
                }
            }
        }
        return content;
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
