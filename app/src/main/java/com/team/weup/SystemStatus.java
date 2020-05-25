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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.team.weup.model.User;
import com.team.weup.repo.UserInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

//这个类用于记录系统状态
public final class SystemStatus {
    //当前登录者帐号（学号）
    private static String now_account = null;
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

        editor.putString("now_account", now_account);
        editor.putBoolean("islogin", islogin);
        editor.putString("now_name", now_name);

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
            now_account = sharedPreferences.getString("now_account", null);
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

    //从云上下载图片
    private static Bitmap downHead = null;
    public static Bitmap downloadHeadFromCloud(String imgPath) {
        try {

            downHead = null;
            String imgUrl = "http://123.56.85.195/upload/" + imgPath;
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(imgUrl);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HttpResponse response = client.execute(httpGet);
                        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            InputStream input = response.getEntity().getContent();
                            downHead = BitmapFactory.decodeStream(input);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        client.getConnectionManager().shutdown();
                    }
                }
            });
            thread.start();
            thread.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            return downHead;
        }
    }


    //getter && setter

    public static void setNow_account(String accounts) {
        now_account = accounts;
    }

    public static String getNow_account() {
        return now_account;
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
