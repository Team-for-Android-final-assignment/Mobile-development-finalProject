package com.team.weup;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
    //private RadioGroup mTabRadioGroup;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;
    private ReviewFragment reviewFragment;

    //底边栏
    private BottomFragment bottomFragment;

    //存储功能开启设置
    private SharedPreferences sharedPreferences;
    //屏幕监听
    private ScreenListener screenListener;

    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_home);
        initView();
        requestPermissions();
        init();
    }

    private void init(){
        //初始化share数据库
        sharedPreferences = getSharedPreferences("share", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        //设置屏幕监听,用来监听用户的解锁动作
        screenListener = new ScreenListener(this);
        screenListener.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onScreenOn() {//手机点亮屏幕时的动作
                //如果打开了开关
                Log.i("TEST","test1");
                if(sharedPreferences.getBoolean("btnTf",false)){
                    //如果屏幕解锁
                    Log.i("TEST","test2");
                    if(sharedPreferences.getBoolean("tf",false)){
                        Log.i("TEST","test3");
                        startActivity(new Intent(HomeActivity.this, WordActivity.class));
                        overridePendingTransition(0, 0);
                    }
                }
            }

            @Override
            public void onScreenOff() {//手机已锁屏时的操作
                Log.i("TEST","test4");
                editor.putBoolean("tf",true);
                editor.commit();
                WeUpApplication.destroyActivity("mainActivity");
                overridePendingTransition(0, 0);
            }

            @Override
            public void onUserPresent() {//手机已解锁时的操作
                Log.i("TEST","test5");
                editor.putBoolean("tf",false);
                editor.commit();
            }
        });
    }

    private void initView(){
        mViewPager = (ViewPager)findViewById(R.id.fragment_vp);
        //mTabRadioGroup = (RadioGroup) findViewById(R.id.tabs_rg);
        reviewFragment = new ReviewFragment();
        // init fragment
        //将这里的fragment修改成你自己的页面
        mFragments = new ArrayList<>(4);
        mFragments.add(BlankFragment.newInstance("生活",""));
        mFragments.add(reviewFragment);
        mFragments.add(new SportsFragment());
        mFragments.add(new IFragment());
        // init view pager
        mAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        // register listener
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        //mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);

        //底边栏
        bottomFragment = new BottomFragment();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //screenListener.unregisterListener();
        Log.i("TEST","test7");
        screenListener.unregisterListener();
        mViewPager.removeOnPageChangeListener(mPageChangeListener);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener(){

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            /*
            RadioButton radioButton = (RadioButton) mTabRadioGroup.getChildAt(i);
            radioButton.setChecked(true);
             */
            bottomFragment.UIMotify(i, HomeActivity.this);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    // 变更vpViewPager显示内容
    public void changePager(int i){
        mViewPager.setCurrentItem(i);
    }

    /*
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
     */

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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestPermissions() {
        // 申请权限：
        String[] neededPermissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACTIVITY_RECOGNITION,
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

