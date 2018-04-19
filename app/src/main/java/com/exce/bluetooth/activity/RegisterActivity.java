/**
 * 南京熙健 ecg 开发支持库
 * Copyright (C) 2015 mhealth365.com All rights reserved.
 * create by lc  2015年6月16日 上午9:56:01
 */

package com.exce.bluetooth.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.exce.bluetooth.R;
import com.exce.bluetooth.bean.UserInfo;


/**
 * 注册
 */
public class RegisterActivity extends Activity {

    private Button btnRegister;
    private EditText etName, etPhone, etCid, etOpenId, etAccountName;
    private EditText etSex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg_register);

        etOpenId = (EditText) findViewById(R.id.editOpenId);
        etAccountName = (EditText) findViewById(R.id.editAccountName);//用户名
        etName = (EditText) findViewById(R.id.editName);//姓名
        etCid = (EditText) findViewById(R.id.editCid);
        etPhone = (EditText) findViewById(R.id.editPhone);
        etSex = (EditText) findViewById(R.id.editSex);


        btnRegister = (Button) findViewById(R.id.register_button);
        btnRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i("RegisterActivity", "register----");

                UserInfo ui = new UserInfo();
                ui.userName = "" + getEditTextValue(etName);
//                ui.accountName = "" + getEditTextValue(etAccountName); // 可能是手机号或者email
                ui.openId = "" + getEditTextValue(etOpenId);
                ui.phone = "" + getEditTextValue(etPhone);
                ui.cid = "" + getEditTextValue(etCid);

                ui.sex = getSexValue(getEditTextValue(etSex));

            }
        });
    }


    private boolean emptyString(String str) {
        if (str == null)
            return true;
        if (str.equals(""))
            return true;
        return false;
    }


    public UserInfo.SEX getSexValue(String sex) {
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

    private String getEditTextValue(EditText et) {
        return et.getText().toString().trim();
    }

}