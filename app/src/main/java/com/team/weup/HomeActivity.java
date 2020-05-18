package com.team.weup;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements BlankFragment.OnFragmentInteractionListener,ReviewFragment.OnFragmentInteractionListener{

    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;
    private ReviewFragment reviewFragment;

    //存储功能开启设置
    private SharedPreferences sharedPreferences;
    //屏幕监听
    private ScreenListener screenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        initView();
        requestPermissions();
    }

    private void init(){
        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        //设置屏幕监听,用来监听用户的解锁动作
        screenListener = new ScreenListener(this);
        screenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {

            }

            @Override
            public void onScreenOff() {

            }

            @Override
            public void onUserPresent() {

            }
        });
    }

    private void initView(){
        mViewPager = (ViewPager)findViewById(R.id.fragment_vp);
        mTabRadioGroup = (RadioGroup) findViewById(R.id.tabs_rg);
        reviewFragment = new ReviewFragment();
        // init fragment
        //将这里的fragment修改成你自己的页面
        mFragments = new ArrayList<>(4);
        mFragments.add(BlankFragment.newInstance("生活",""));
        mFragments.add(reviewFragment);
        mFragments.add(new SportsFragment());
        mFragments.add(BlankFragment.newInstance("我的",""));
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        // register listener
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        screenListener.unregisterListener();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener(){

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            RadioButton radioButton = (RadioButton) mTabRadioGroup.getChildAt(i);
            radioButton.setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i).getId() == checkedId) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    };

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mList;
        public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.mList = list;
        }

        @Override
        public Fragment getItem(int position) {
            return this.mList == null ? null : this.mList.get(position);
        }

        @Override
        public int getCount() {
            return this.mList == null ? 0 : this.mList.size();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri){

    }

    private void requestPermissions() {
        // 申请权限：
        String[] neededPermissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        List<String> tempPermissions = new ArrayList<>();
        for (String p : neededPermissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                tempPermissions.add(p);
            }
        }
        if (!tempPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, tempPermissions.toArray(new String[0]), 100);
        }
    }

    // 需要重载回调函数：用户对权限申请做出相应操作后执行
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "权限被拒绝：" + permissions[i], Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

