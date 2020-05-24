package com.team.weup;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

//这个类用于记录系统状态
public final class SystemStatus {
    //当前登录者帐号（学号）
    private static String now_accounts = null;
    //当前登陆者名字
    private static String now_name = null;
    //当前登陆者头像
    private static Bitmap userhead = null;
    //是否已登录
    private static Boolean islogin = false;

    //所需权限
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };
    //存储系统信息
    private static String PREFERENCE_NAME = "SaveSetting";
    private static int MODE = Context.MODE_PRIVATE;

    //申请权限
    public static void AskForPermission(Activity activity) {
        try {
            //检测是否有权限
            List<String> permissionList = new ArrayList<>();
            for (int i = 0; i < PERMISSIONS_STORAGE.length; i++) {
                if (activity.checkSelfPermission(PERMISSIONS_STORAGE[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(PERMISSIONS_STORAGE[i]);
                }
            }
            // 没有权限，申请权限
            if (!permissionList.isEmpty())
                activity.requestPermissions(permissionList.toArray(new String[permissionList.size()]), 100);
        } catch (Exception e) {
            Toast.makeText(activity, "申请权限失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    //存储系统信息
    public static void SaveSetting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("islogin", islogin);
        editor.putString("now_name", now_name);
        editor.putString("now_accounts", now_accounts);
        //转码图片
        if (userhead != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            userhead.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();
            editor.putString("head", Base64.encodeToString(bytes, Base64.DEFAULT));
        }

        editor.apply();
    }

    //读取系统存储的信息
    public static void LoadSetting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE);

        islogin = sharedPreferences.getBoolean("islogin", false);

        if (islogin) {
            now_accounts = sharedPreferences.getString("now_accounts", null);
            now_name = sharedPreferences.getString("now_name", null);
            String source = sharedPreferences.getString("head", null);
            //解码图片
            if (source != null) {
                byte[] bitmapArray;
                bitmapArray = Base64.decode(source, Base64.DEFAULT);
                userhead = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            }
        }
    }

    //getter && setter

    public static void setNow_accounts(String accounts) {
        now_accounts = accounts;
    }

    public static String getNow_accounts() {
        return now_accounts;
    }

    public static void setNow_name(String name) {
        now_name = name;
    }

    public static String getNow_name() {
        return now_name;
    }

    public static void setLogin(boolean login) {
        islogin = login;
    }

    public static Boolean isLogin() {
        return islogin;
    }

    public static void setUserhead(Bitmap head) {
        userhead = head;
    }

    public static Bitmap getUserhead() {
        return userhead;
    }

}
