package com.exce.bluetooth.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.exce.bluetooth.R;

/**
 * @Author Wangjj
 * @Create 2017/12/21.
 * @Content
 */
public class TabTwoFragment extends Fragment {
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_location, container, false);

        return mRootView;

    }
}
