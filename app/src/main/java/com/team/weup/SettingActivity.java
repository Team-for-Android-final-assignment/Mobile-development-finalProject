package com.team.weup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

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
        //setSpinnerItemSelectedByValue()
        setSpinnerItemSelectedByValue(spinnerAllNum,sharedPreferences.getInt("allNum",2)+"道");
        this.spinnerAllNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String msg = parent.getItemAtPosition(position).toString();
                int i = Integer.parseInt(msg.substring(0,1));
                editor.putInt("allNum",i);
                editor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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

    //设置下拉框默认选项
    public void setSpinnerItemSelectedByValue(Spinner spinner,String value){
        SpinnerAdapter apsAdapter = spinner.getAdapter();
        int k = apsAdapter.getCount();
        for(int i=0;i<k;i++){
            if(value.equals(apsAdapter.getItem(i).toString())){
                spinner.setSelection(i,true);
            }
        }
    }
}
