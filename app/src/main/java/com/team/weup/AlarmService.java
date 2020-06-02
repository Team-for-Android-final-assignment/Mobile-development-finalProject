package com.team.weup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.team.weup.model.TodoItem;
import com.team.weup.repo.TodoItemInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 提醒服务（没有作用，暂时放弃）
 */

public class AlarmService extends Service {
    private static final String TAG = "提醒服务测试";

    private AlarmManager am;
    private PendingIntent pi;
    private Long time;
    private String title;
    private String content;

    private TodoItem currentItem;
    private boolean done = false;

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "test1");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "test2");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在Service结束后关闭AlarmManager
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.cancel(pi);
        Log.i(TAG, "test3");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i(TAG, "test4");
        getAlarmTime(intent,flags,startId);
        Log.i(TAG, "test5");
        return START_REDELIVER_INTENT;//提高优先级，内存清理时该服务不容易被杀死
    }

    public void getAlarmTime(Intent intent, int flags, int startId){
        long userId = Long.parseLong(SystemStatus.getNow_account());
        NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                .getTodoItemsByUserId(userId)
                .enqueue(new Callback<ReturnVO<List<TodoItem>>>() {
                    @Override
                    public void onResponse(Call<ReturnVO<List<TodoItem>>> call,
                                           Response<ReturnVO<List<TodoItem>>> response) {
                        ReturnVO<List<TodoItem>> body = response.body();
                        assert body != null;
                        if (ReturnVO.ERROR.equals(body.getCode())) {
                            Log.i(TAG, "test6");
                            Toast.makeText(getApplicationContext(), "获得待办列表失败", Toast.LENGTH_LONG).show();
                        } else if(body.getCode()==200){
                            Log.i(TAG, "test7");
                            List<TodoItem> data = body.getData();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//年-月-日 时-分
                            Date date1 = new Date();
                            try {
                                //遍历数据库，寻找设置提醒且提醒时间最近的待办事项
                                for (int i = 0; i < data.size(); i++) {
                                    if (data.get(i).getAlert() == true && !done) {
                                        currentItem = data.get(i);
                                        date1 = dateFormat.parse(data.get(i).getAlertTime().toString());
                                        done = true;
                                    }
                                    if (data.get(i).getAlert() == true){
                                        Date date2 = dateFormat.parse(data.get(i).getAlertTime().toString());
                                        if(date1.getTime()>date2.getTime()){
                                            currentItem = data.get(i);
                                        }
                                    }
                                }
                                Log.i(TAG, "test8");
                                if(currentItem.getAlert() == true){
                                    //设置该待办事项的提醒为否
                                    Log.i(TAG, "test9");
                                    currentItem.setAlert(false);
                                    NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                                            .updateTodoItemById((long)currentItem.getId(), currentItem)
                                            .enqueue(new Callback<ReturnVO<TodoItem>>() {
                                                @Override
                                                public void onResponse(Call<ReturnVO<TodoItem>> call,
                                                                       Response<ReturnVO<TodoItem>> response) {
                                                    ReturnVO<TodoItem> body = response.body();
                                                    if(body.getCode()==500){
                                                        Toast.makeText(getApplicationContext(),"删除提醒失败",
                                                                Toast.LENGTH_SHORT).show();
                                                    }else if(body.getCode()==200){
                                                        Toast.makeText(getApplicationContext(),"删除提醒成功",
                                                                Toast.LENGTH_SHORT).show();
                                                        currentItem.setAlert(true);
                                                    }
                                                }
                                                @Override
                                                public void onFailure(Call<ReturnVO<TodoItem>> call, Throwable t) {

                                                }
                                            });
                                }
                                Log.i(TAG, "test10");
                                Intent startNotification = new Intent(getApplicationContext(), AlarmReceiver.class);//启动广播
                                startNotification.putExtra("content", currentItem.getContent());
                                am = (AlarmManager) getSystemService(ALARM_SERVICE);   //系统闹钟的对象
                                pi = PendingIntent.getBroadcast(getApplicationContext(),
                                        0, startNotification, PendingIntent.FLAG_UPDATE_CURRENT);//设置事件
                                Log.i(TAG, "test11");
                                if (currentItem.getAlert() == true) {
                                    Date date3 = dateFormat.parse(currentItem.getAlertTime().toString());
                                    Log.i(TAG, "test12");
                                    am.set(AlarmManager.RTC_WAKEUP, date3.getTime(), pi);    //提交事件，发送给广播接收器
                                    Log.i(TAG, "test13");
                                } else {
                                    //当提醒时间为空的时候，关闭服务，下次添加提醒时再开启
                                    stopService(new Intent(getApplicationContext(), AlarmService.class));
                                    Log.i(TAG, "test14");
                                }

                            }catch (ParseException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<ReturnVO<List<TodoItem>>> call, Throwable t) {
                    }
                });
    }
}
