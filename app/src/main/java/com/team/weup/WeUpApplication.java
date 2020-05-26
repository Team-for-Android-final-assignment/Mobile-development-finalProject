package com.team.weup;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class WeUpApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化全局变量
        SystemStatus.LoadSetting(this);

        //根据是否已登录进行跳转
        //已登录
        if (SystemStatus.isLogin()) {
            startActivity(new Intent(this, MainActivity.class));
        }
        //未登录
        else {
            startActivity(new Intent(this, LoginInActivity.class));
        }
    }

    @Override
    public void onTerminate() {
        SystemStatus.SaveSetting(this);
        super.onTerminate();
    }
}
