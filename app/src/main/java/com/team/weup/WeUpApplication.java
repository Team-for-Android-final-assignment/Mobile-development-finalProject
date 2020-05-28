package com.team.weup;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WeUpApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化全局变量
        SystemStatus.LoadSetting(this);

//        //根据是否已登录进行跳转
//        //已登录
//        if (SystemStatus.isLogin()) {
//            //startActivity(new Intent(this, MainActivity.class));
//            Log.i("TEST","test10");
//            Intent intent = new Intent(this, HomeActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }
//        //未登录
//        else {
//            Intent intent = new Intent(this, LoginInActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }
    }


    @Override
    public void onTerminate() {
        SystemStatus.SaveSetting(this);
        super.onTerminate();
    }

    private static Map<String, Activity> destroyMap = new HashMap<>();

    public static void addDestroyActivity(Activity activity,String activityName){
        destroyMap.put(activityName,activity);
    }

    public static void destroyActivity(String activityName){
        Set<String> keySet = destroyMap.keySet();
        for(String key:keySet){
            destroyMap.get(key).finish();
        }
    }
}
