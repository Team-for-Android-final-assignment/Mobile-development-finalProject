package com.team.weup;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    //月、日、星期
    private String month;
    private String day;
    private int week;
    private String weekStr;
    private String hour;
    private String minute;

    private TextView current_time;
    private TextView current_date;

    private TextView english_word;
    private TextView yinbiao;

    private RadioGroup checkArea;
    private RadioButton option1;
    private RadioButton option2;
    private RadioButton option3;

    private KeyguardManager km;
    private KeyguardManager.KeyguardLock kl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        current_time = (TextView)findViewById(R.id.current_time);
        current_date = (TextView)findViewById(R.id.current_date);

        option1 = (RadioButton)findViewById(R.id.option1);
        option2 = (RadioButton)findViewById(R.id.option2);
        option3 = (RadioButton)findViewById(R.id.option3);
        checkArea = (RadioGroup)findViewById(R.id.options);
        checkArea.setOnCheckedChangeListener(this);

        english_word = (TextView)findViewById(R.id.english_word);
        yinbiao = (TextView)findViewById(R.id.yinbiao);

        km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("unlock");
    }

    @Override
    protected void onStart(){
        super.onStart();
        //在这里获取系统时间
        Calendar calendar = Calendar.getInstance();
        month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        day = String.valueOf(calendar.get(calendar.DAY_OF_MONTH));
        week = calendar.get(calendar.DAY_OF_WEEK);

        //小时、分钟个位数数值补0
        if(calendar.get(Calendar.HOUR_OF_DAY)<10){
            hour = "0"+String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }
        else{
            hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        }

        if(calendar.get(Calendar.MINUTE)<10){
            minute = "0"+String.valueOf(calendar.get(Calendar.MINUTE));
        }
        else{
            minute = String.valueOf(calendar.get(Calendar.MINUTE));
        }

        //将星期映射为中文，一周从周日开始
        switch(week){
            case 1:
                weekStr = "天";
                break;
            case 2:
                weekStr = "一";
                break;
            case 3:
                weekStr = "二";
                break;
            case 4:
                weekStr = "三";
                break;
            case 5:
                weekStr = "四";
                break;
            case 6:
                weekStr = "五";
                break;
            case 7:
                weekStr = "六";
                break;
        }

        current_time.setText(hour+":"+minute);
        current_date.setText(month+"月"+day+"日"+"    "+"星期"+weekStr);
    }

    //用来记录和标记错题
    private void saveWrongData(){

    }

    //该函数用于实现当用户选择词义时，判断选择是否正确,并产生相应的动态效果
    private void btnGetText(String msg, RadioButton btn){
        setWordColor();
        if(msg.equals("抛弃、放弃")){
            english_word.setTextColor(Color.GREEN);
            yinbiao.setTextColor(Color.GREEN);
            btn.setTextColor(Color.GREEN);
        }
        else{

            english_word.setTextColor(Color.RED);
            yinbiao.setTextColor(Color.RED);
            btn.setTextColor(Color.RED);
            //这里后续还需要保存错误单词

        }

    }

    //设置RadioGroup的点击事件
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        //默认情况下，选项为未选中状态
        checkArea.setClickable(false);
        //用于判断哪个选项被选中
        switch(checkedId){
            case R.id.option1:
                String msg1 = option1.getText().toString().substring(2);
                btnGetText(msg1,option1);
                //选对了就解锁
                //unlock();
                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                break;
            case R.id.option2:
                String msg2 = option2.getText().toString().substring(2);
                btnGetText(msg2,option2);
                break;
            case R.id.option3:
                String msg3 = option3.getText().toString().substring(2);
                btnGetText(msg3,option3);
                break;
        }
    }

    //恢复默认颜色
    private void setWordColor(){
        option1.setChecked(false);
        option2.setChecked(false);
        option3.setChecked(false);
        option1.setTextColor(Color.WHITE);
        option2.setTextColor(Color.WHITE);
        option3.setTextColor(Color.WHITE);
        yinbiao.setTextColor(Color.WHITE);
        english_word.setTextColor(Color.WHITE);
    }

    //解锁动作函数
    private void unlock(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        kl.disableKeyguard();
        finish();
    }

    //手势滑动解锁,已掌握？
    @Override
    public boolean onTouchEvent(MotionEvent event){

        return super.onTouchEvent(event);
    }





}
