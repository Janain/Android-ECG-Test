package com.exce.bluetooth.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.exce.bluetooth.R;
import com.exce.bluetooth.ui.PersonalActivity;

/**
 * @Author Wangjj
 * @Create 2017/12/21.
 * @Content
 */
public class TabThreeFragment extends Fragment implements View.OnClickListener{
    private View mRootView;
    private TextView accountTv;
    private ImageView nextImg;
    String login_name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_mine, container, false);
        accountTv = mRootView.findViewById(R.id.account_msg);
        nextImg = mRootView.findViewById(R.id.account_next);

        login_name = accountTv.getText().toString();
//        accountTv.setText(SharedPreferenceUtil.getLoginName(getContext()));
        nextImg.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.account_next:
                Intent intent = new Intent(getContext(),PersonalActivity.class);
                startActivity(intent);
                break;
        }
    }
}
