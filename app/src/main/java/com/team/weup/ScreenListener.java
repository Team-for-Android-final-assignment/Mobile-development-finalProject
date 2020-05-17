package com.team.weup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class ScreenListener {
    private Context context;
    private ScreenBroadcastReceiver mScreenReceiver;
    //内部接口
    private ScreenStateListener mScreenStateListener;

    public ScreenListener(Context context){
        this.context = context;
        mScreenReceiver = new ScreenBroadcastReceiver();
    }

    //定义接口获取屏幕状态
    public interface ScreenStateListener{
        void onScreenOn();
        void onScreenOff();
        void onUserPresent();
    }

    //获取屏幕状态
    private void getScreenState(){
        PowerManager manager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        if(manager.isScreenOn()) {
            if(mScreenStateListener != null){
                mScreenStateListener.onScreenOn();
            }
        }
        else{
            if(mScreenStateListener != null){
                mScreenStateListener.onScreenOff();
            }
        }
    }

    //内部广播类,监听屏幕动作
    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;
        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if(action.equals(Intent.ACTION_SCREEN_ON)){
                mScreenStateListener.onScreenOn();
            }
            else if(action.equals(Intent.ACTION_SCREEN_OFF)){
                mScreenStateListener.onScreenOff();
            }
            else if(action.equals(Intent.ACTION_USER_PRESENT)){
                mScreenStateListener.onUserPresent();
            }
        }
    }

    //监听广播状态
    public void begin(ScreenStateListener listener){
        mScreenStateListener = listener;
        registerListener();
        getScreenState();
    }

    private void registerListener(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(mScreenReceiver,filter);
    }

    public void unregisterListener(){
        context.unregisterReceiver(mScreenReceiver);
    }
}
