package com.exce.bluetooth.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.exce.bluetooth.R;
import com.exce.bluetooth.adapter.ViewPagerAdapter;

public class MainTabActivity extends AppCompatActivity {

    NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;
    TabLayout mTabLayout;
    ViewPager mViewPager;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        init();
    }

    public void init() {
        mNavigationView = findViewById(R.id.navigation_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        initDrawerLayout();
        initTablayout();
    }

    private void initTablayout() {

        PagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(adapter.getCount());
        mTabLayout.setupWithViewPager(mViewPager);

    }

    private void initDrawerLayout() {

        headerView = mNavigationView.getHeaderView(0);
        headerView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            Toast.makeText(MainTabActivity.this, "headerView clicked", Toast.LENGTH_LONG).show();
        });

        mNavigationView.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.menu_app_update:
                    Toast.makeText(MainTabActivity.this, "点击了应用更新", Toast.LENGTH_LONG).show();
                    break;
                case R.id.menu_message:
                    Toast.makeText(MainTabActivity.this, "点击了消息", Toast.LENGTH_LONG).show();
                    break;
            }

            return false;
        });
    


//        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.open, R.string.close);
//
//        drawerToggle.syncState();
//
//        mDrawerLayout.addDrawerListener(drawerToggle);

    }


}
