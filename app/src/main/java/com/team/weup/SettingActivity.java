package com.team.weup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor = null;
    private SwitchButton switchButton;
    private Spinner spinnerDifficulty;
    private Spinner spinnerAllNum;
    private ArrayAdapter<String> adapterDfficulty,adapterAllNum;
    String[] difficulty = new String[]{"四级","六级","托福","GRE"};
    String[] number = new String[]{"2道","4道","6道","8道"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //初始化share数据库
        sharedPreferences = this.getSharedPreferences("share", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switchButton = (SwitchButton)findViewById(R.id.switch_button);
        spinnerDifficulty = (Spinner)findViewById(R.id.spinner_difficulty);
        spinnerAllNum = (Spinner)findViewById(R.id.spinner_all_number);

        adapterDfficulty = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item,difficulty);
        spinnerDifficulty.setAdapter(adapterDfficulty);

        adapterAllNum = new ArrayAdapter<String>(this,android.R.layout.simple_selectable_list_item,number);
        spinnerAllNum.setAdapter(adapterAllNum);
        //setSpinnerItemSelectedByValue(spinnerDifficulty,"四级");


        //设置开关事件：包括显示事件和数据事件
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.switch_button:
                        //如果开关处于打开状态
                        if(switchButton.isSwitchOpen()){
                            switchButton.closeSwitch();
                            editor.putBoolean("btnTf",false);
                        }
                        //开关关闭
                        else{
                            switchButton.openSwitch();
                            editor.putBoolean("btnTf",true);
                        }
                        editor.commit();
                        break;
                }
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        if(sharedPreferences.getBoolean("btnTf",false)){
            switchButton.openSwitch();
        }
        else{
            switchButton.closeSwitch();
        }
    }
}
