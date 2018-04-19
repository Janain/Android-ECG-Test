package com.exce.bluetooth.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.exce.bluetooth.bean.FragmentInfo;
import com.exce.bluetooth.fragment.TabOneFragment;
import com.exce.bluetooth.fragment.TabThreeFragment;
import com.exce.bluetooth.fragment.TabTwoFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Wangjj
 * @Create 2017/12/21.
 * @Content
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<FragmentInfo> mFragments = new ArrayList<>(4);

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        initFragments();
    }

    private void initFragments() {

        mFragments.add(new FragmentInfo("主页", TabOneFragment.class));
        mFragments.add(new FragmentInfo("本地", TabTwoFragment.class));
        mFragments.add(new FragmentInfo("我的", TabThreeFragment.class));

    }

    @Override
    public Fragment getItem(int position) {

        try {
            return (Fragment) mFragments.get(position).getFragment().newInstance();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getTitle();
    }
}
