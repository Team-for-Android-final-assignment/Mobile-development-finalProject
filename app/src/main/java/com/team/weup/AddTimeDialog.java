package com.team.weup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 时间dialog
 */

class AddTimeDialog {

    private Message message;

    //该接口在调用页面实现
    public interface Message {
        void changer(Map<String, String> map);
    }

    //将接口与调用页面进行绑定？
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    private Context context;
    private String source;

    private Spinner spinnerYear, spinnerMonth, spinnerDay, spinnerTime, spinnerMinute;
    private ArrayAdapter<String> adapterYear;
    private ArrayAdapter<String> adapterMonth;
    private ArrayAdapter<String> adapterDay;
    private ArrayAdapter<String> adapterTime;
    private ArrayAdapter<String> adapterMinute;
    private String year, month, day, time, minute;
    private int yearSys, monthSys, daySys, timeSys, minuteSys;
    private String yearSs, monthSs, daySs, timeSs, minuteSs;
    private Date date;

    //定义五个数组用于年、月、日、小时、分的spinner里面的数据
    String[] msgYear = new String[]{"2016", "2017", "2018", "2019", "2020", "2021"};
    String[] msgMonth = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    String[] msgDay = new String[]{
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    String[] msgTime = new String[]{
            "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    String[] msgMinute = new String[]{"00",
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
            "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
            "41", "42", "43", "44", "45", "46", "47", "48", "49", "50",
            "51", "52", "53", "54", "55", "56", "57", "58", "59"};

    public AddTimeDialog(Context context, String source) {
        this.context = context;
        this.source = source;
        date = new Date();
        yearSys = date.getYear() + 1900;
        monthSys = date.getMonth() + 1;
        daySys = date.getDate();
        timeSys = date.getHours();
        minuteSys = date.getMinutes();

        Log.e("AddTimeDialog", yearSys + "");
        Log.e("AddTimeDialog", monthSys + "");
        Log.e("AddTimeDialog", daySys + "");
        Log.e("AddTimeDialog", timeSys + "");
        Log.e("AddTimeDialog",minuteSys + "");

        yearSs = yearSys + "";
        if (monthSys < 10) {
            monthSs = "0" + monthSys;
        } else {
            monthSs = monthSys + "";
        }
        if (daySys < 10) {
            daySs = "0" + daySys;
        } else {
            daySs = "" + daySys;
        }
        if (timeSys < 10) {
            timeSs = "0" + timeSys;
        } else {
            timeSs = "" + timeSys;
        }
        if (minuteSys < 10) {
            minuteSs = "0" + minuteSys;
        } else {
            minuteSs = "" + minuteSys;
        }
    }

    /**
     * 自定义一个dialog
     * 用于其他页面要设置日期时显示此dialog
     * 并将此页面的数据返回到调用此dialog的activity里面
     */
    public void ShowDia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.time_log_layout, null);       //绑定布局文件
        builder.setView(view);
        builder.setTitle("设置时间：");

        //初始化spinner
        spinnerYear = (Spinner) view.findViewById(R.id.send_task_back_year);
        spinnerMonth = (Spinner) view.findViewById(R.id.send_task_back_month);
        spinnerDay = (Spinner) view.findViewById(R.id.send_task_back_day);
        spinnerTime = (Spinner) view.findViewById(R.id.send_task_back_time);
        spinnerMinute = (Spinner) view.findViewById(R.id.send_task_back_minute);

        //初始化adapter，向adapter里面添加数据
        adapterYear = new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, msgYear);
        //将spinner设置到adapter里面
        spinnerYear.setAdapter(adapterYear);
        setSpinnerItemSelectedByValue(spinnerYear, yearSs, 1);
        //为spinner设置监听事件
        this.spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String msg = parent.getItemAtPosition(position).toString();
                year = msg;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapterMonth = new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, msgMonth);
        spinnerMonth.setAdapter(adapterMonth);
        setSpinnerItemSelectedByValue(spinnerMonth, monthSs, 2);
        this.spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String msg = parent.getItemAtPosition(position).toString();
                month = msg;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapterDay = new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, msgDay);
        spinnerDay.setAdapter(adapterDay);
        setSpinnerItemSelectedByValue(spinnerDay, daySs, 3);
        this.spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String msg = parent.getItemAtPosition(position).toString();
                day = msg;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapterTime = new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, msgTime);
        spinnerTime.setAdapter(adapterTime);
        setSpinnerItemSelectedByValue(spinnerTime, timeSs, 4);
        this.spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String msg = parent.getItemAtPosition(position).toString();
                time = msg;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        adapterMinute = new ArrayAdapter<String>(context, android.R.layout.simple_selectable_list_item, msgMinute);
        spinnerMinute.setAdapter(adapterMinute);
        setSpinnerItemSelectedByValue(spinnerMinute, minuteSs, 5);
        this.spinnerMinute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String msg = parent.getItemAtPosition(position).toString();
                minute = msg;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (message != null) {
                    /**
                     * 将不同的spinner的数据存到hasMap里面
                     * 用于返回数据
                     * */
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("year", year);
                    map.put("month", month);
                    map.put("day", day);
                    map.put("time", time);
                    map.put("minute", minute);
                    map.put("source", source);
                    message.changer(map);
                }
            }
        });
        builder.show();
    }


    public void setSpinnerItemSelectedByValue(Spinner spinner, String value, int j) {
        SpinnerAdapter apsAdapter = spinner.getAdapter();
        int k = apsAdapter.getCount();
        for (int i = 0; i < k; i++) {
            if (value.equals(apsAdapter.getItem(i).toString())) {
                spinner.setSelection(i, true);//默认选中项
                switch (j) {
                    case 1:
                        year = apsAdapter.getItem(i).toString();
                        break;
                    case 2:
                        month = apsAdapter.getItem(i).toString();
                        break;
                    case 3:
                        day = apsAdapter.getItem(i).toString();
                        break;
                    case 4:
                        time = apsAdapter.getItem(i).toString();
                        break;
                    case 5:
                        minute = apsAdapter.getItem(i).toString();
                }
                break;
            }
        }
    }
}
