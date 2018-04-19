package com.exce.bluetooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.exce.bluetooth.R;
import com.exce.bluetooth.utils.SharedPreferenceUtil;

/**
 * @Author Wangjj
 * @Create 2018/4/9.
 * @Content
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    EditText mUsername, mPassword;
    Button mBtnLogin;
    private String login_name, login_password;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        mUsername = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);
        mBtnLogin = findViewById(R.id.main_btn_login);
        mBtnLogin.setOnClickListener(this);

        mUsername.setText(SharedPreferenceUtil.getLoginName(this));
        mPassword.setText(SharedPreferenceUtil.getLoginPassword(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btn_login:
                login_name = mUsername.getText().toString();
                login_password = mPassword.getText().toString();
                if (mUsername.getText().toString().equals(login_name) &&
                        mPassword.getText().toString().equals(login_password)) {
                    Intent intent = new Intent(this, MainTabActivity.class);
                    startActivity(intent);
                    SharedPreferenceUtil.putLoginName(getApplicationContext(), login_name);
                    SharedPreferenceUtil.putLoginPassword(getApplicationContext(), login_password);
                    Toast.makeText(getApplicationContext(), "登陆成功...", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "重新登陆...", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
