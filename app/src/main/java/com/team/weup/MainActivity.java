package com.team.weup;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.team.weup.model.User;
import com.team.weup.model.Word;
import com.team.weup.repo.UserInterface;
import com.team.weup.repo.WordInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.util.Calendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private String chinese="";
    private int progress=0;

    private User currentUser;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor = null;

    //用来统计当前答对的题数
    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        sharedPreferences = getSharedPreferences("share",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

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


        hideBottomUIMenu();
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

        WeUpApplication.addDestroyActivity(this,"mainActivity");
        getDBData();
        //设置屏幕上单词的显示

    }

    private void getNextData(){
        //解锁的标准
        int require = sharedPreferences.getInt("allNum",2);

    }

    //用来记录和标记错题
    private void saveWrongData(){

    }

    //该函数用于实现当用户选择词义时，判断选择是否正确,并产生相应的动态效果
    private void btnGetText(String msg, RadioButton btn){
        setWordColor();
        if(msg.equals(chinese)){
            english_word.setTextColor(Color.GREEN);
            yinbiao.setTextColor(Color.GREEN);
            btn.setTextColor(Color.GREEN);

            //这里判断一下答对的题目数，够数了就解锁，不够下一个。答错题了也应该换下一个，同时题目存储到错题本上

            //进度++
            progress++;
            //对题数++
            count++;
            Log.i("TEST","3:"+String.valueOf(progress));
            //修改进度
            if(currentUser == null){
                Log.i("TEST","user object is null");
            }
            else{
                currentUser.setProgress(progress);
            }
            //返回新的对象
            NetworkUtil.getRetrofit().create(UserInterface.class)
                    .updateUser((long)1,currentUser)
                    .enqueue(new Callback<ReturnVO<User>>() {
                        @Override
                        public void onResponse(Call<ReturnVO<User>> call, Response<ReturnVO<User>> response) {
                            //选对了就解锁
                            //getInt的第二个参数是不存在这个键时，返回的缺省值
                            if(count == sharedPreferences.getInt("allNum",2)){
                                unlock();
                            }
                            else{
                                //没有到数的话就渲染下一个词
                                Log.i("TEST","test8");
                                //progress还是5？？
                                getDBData();
                                setWordColor();
                            }
                        }

                        @Override
                        public void onFailure(Call<ReturnVO<User>> call, Throwable t) {

                        }
                    });

            //startActivity(new Intent(MainActivity.this,HomeActivity.class));
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
        Log.i("TEST","test6");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        //这句话是用来隐藏锁屏界面的
        //disabled出问题了
        kl.disableKeyguard();

        finish();
    }

    @Override
    protected void onPause(){
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onPause();
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i("TEST","test11");
        //kl.reenableKeyguard();
    }

    //手势滑动解锁,已掌握？
    @Override
    public boolean onTouchEvent(MotionEvent event){

        return super.onTouchEvent(event);
    }

    private void getDBData(){
        //从数据库中读取当前的进度
        NetworkUtil.getRetrofit().create(UserInterface.class)
                .getUserById((long)1)
                .enqueue(new Callback<ReturnVO<User>>() {
                    @Override
                    public void onResponse(Call<ReturnVO<User>> call, Response<ReturnVO<User>> response) {
                        ReturnVO<User> body = response.body();
                        progress = body.getData().getProgress();
                        currentUser = new User(body.getData());
                        if(currentUser == null){
                            Log.i("TEST","Hello");
                        }
                        Log.i("TEST","1:"+String.valueOf(progress));
                        //根据当前进度读取下一个单词
                        //第二层开始
                        NetworkUtil.getRetrofit().create(WordInterface.class)
                                .getWordById((long) (progress%6+1))
                                .enqueue(new Callback<ReturnVO<Word>>() {
                                    @Override
                                    public void onResponse(Call<ReturnVO<Word>> call, Response<ReturnVO<Word>> response) {
                                        ReturnVO<Word> body = response.body();
                                        Log.i("TEST","2:"+String.valueOf(progress));
                                        english_word.setText(body.getData().getEnglish());
                                        yinbiao.setText(body.getData().getYinbiao());
                                        option1.setText("A:"+body.getData().getOption1());
                                        option2.setText("B:"+body.getData().getOption2());
                                        option3.setText("C:"+body.getData().getOption3());
                                        chinese = body.getData().getChinese();
                                        Log.i("TEST",chinese);

                                    }
                                    @Override
                                    public void onFailure(Call<ReturnVO<Word>> call, Throwable t) {

                                    }
                                });
                        //第二层结束

                    }
                    @Override
                    public void onFailure(Call<ReturnVO<User>> call, Throwable t) {

                    }
                });
    }

    protected void hideBottomUIMenu() {
    //隐藏底部导航栏
        final View decorView = getWindow().getDecorView();
        final int  uiOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_IMMERSIVE
                |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOption);

        // This code will always hide the navigation bar
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){
            @Override
            public void  onSystemUiVisibilityChange(int visibility)
            {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    decorView.setSystemUiVisibility(uiOption);
                }
            }
        });
    }




}
