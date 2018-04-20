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

/**
 * @Author Wangjj
 * @Create 2018/4/10.
 * @Content
 */
public class PersonalActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private EditText mAge, mHeight, mUserName, mSex, mWeight, mPhone, mCid;
    private Button save;
    private Spinner mSampleSpeed, mGain, mPaintType, mDisplayLines;
    private UserInfo ui;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peronal);
        initArgs();
    }
    // 初始化参数
    private void initArgs() {
        mAge = findViewById(R.id.ecg_age);
        mHeight = findViewById(R.id.ecg_height);
        mUserName = findViewById(R.id.ecg_name);
        mSex = findViewById(R.id.ecg_sex);
        mWeight = findViewById(R.id.wcg_weight);
        mPhone = findViewById(R.id.ecg_phone);
        mCid = findViewById(R.id.ecg_cid);
        mSampleSpeed = findViewById(R.id.ecg_sample_speed);
        mGain = findViewById(R.id.ecg_gain);
        mPaintType = findViewById(R.id.ecg_paint_type);
        mDisplayLines = findViewById(R.id.ecg_display_lines);

        mSampleSpeed.setOnItemSelectedListener(this);
        mGain.setOnItemSelectedListener(this);
        mPaintType.setOnItemSelectedListener(this);
        mDisplayLines.setOnItemSelectedListener(this);

        save = findViewById(R.id.ecg_save);
        save.setOnClickListener(this);
        ui = new UserInfo();
        ui.setAge(Byte.parseByte(mAge.getText().toString()))
                .setHeight(mHeight.getHeight())
                .setUserName(mUserName.getText().toString())
                .setSex(Byte.parseByte(mSex.getText().toString()))
                .setWeight(Float.parseFloat(mWeight.getText().toString()))
                .setPhone(mPhone.getText().toString())
                .setCid(mCid.getText().toString())
                .setSampleSpeed((short) 360)
                .setGain((byte) 2)
                .setPatientType(Byte.parseByte("儿童"))
                .setDisplayLines(Byte.parseByte("12"));

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
//                ui.setGain(gainArr[position]);
                Log.e("onItemSelected: ", "---------gainArr--------" + gainArr[position]);
                break;
            case R.id.ecg_paint_type:
                String[] paintArr = getResources().getStringArray(R.array.patient_type);//获取病人类型列表数据
//                ui.setPatientType(paintArr[position]);
                Log.e("onItemSelected: ", "---------paintArr--------" + paintArr[position]);
                break;
            case R.id.ecg_display_lines:
                String[] linesArr = getResources().getStringArray(R.array.display_line);//获取显示导联列表数据
//                ui.setDisplayLines(linesArr[position]);
                Log.e("onItemSelected: ", "---------linesArr--------" + linesArr[position]);
                break;

            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
