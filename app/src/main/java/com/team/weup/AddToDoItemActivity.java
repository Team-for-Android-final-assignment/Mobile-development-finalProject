package com.team.weup;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.team.weup.model.TodoItem;
import com.team.weup.repo.TodoItemInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 添加待办事项页面
 */

public class AddToDoItemActivity extends AppCompatActivity implements View.OnClickListener, AddTimeDialog.Message {

    private Button back;  //返回按钮
    private Button finish;  //完成按钮

    private LinearLayout ddl; //截止日期布局
    private TextView year, month, day, time, minute;//年月日时分
    private Spinner completion_status_spinner;//完成状态spinner
    private int completion_status = 1;//默认未完成状态
    private Switch alert_switch;//是否提醒switch
    private boolean alert = false;//默认不提醒
    private LinearLayout ddl_remind;//提醒日期布局
    private TextView year_remind, month_remind, day_remind, time_remind, minute_remind;
    private EditText content;//待办内容

    private TodoItem newItem = new TodoItem();//保存添加待办事项页面输入的信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add);
        initUI();
    }

    //初始化部件
    private void initUI() {
        //绑定布局文件的部件
        back = (Button) findViewById(R.id.back_add);
        finish = (Button) findViewById(R.id.finish);
        ddl = (LinearLayout) findViewById(R.id.ddl);
        year = (TextView) findViewById(R.id.ddl_year);
        month = (TextView) findViewById(R.id.ddl_month);
        day = (TextView) findViewById(R.id.ddl_day);
        time = (TextView) findViewById(R.id.ddl_time);
        minute = (TextView) findViewById(R.id.ddl_minute);
        completion_status_spinner = (Spinner) findViewById(R.id.completion_status_spinner);
        alert_switch = (Switch) findViewById(R.id.alert_switch);
        ddl_remind = (LinearLayout) findViewById(R.id.ddl_remind);
        year_remind = (TextView) findViewById(R.id.ddl_year_remind);
        month_remind = (TextView) findViewById(R.id.ddl_month_remind);
        day_remind = (TextView) findViewById(R.id.ddl_day_remind);
        time_remind = (TextView) findViewById(R.id.ddl_time_remind);
        minute_remind = (TextView) findViewById(R.id.ddl_minute_remind);
        content = (EditText) findViewById(R.id.content_add);



        //设置监视事件
        finish.setOnClickListener(this);
        back.setOnClickListener(this);
        ddl.setOnClickListener(this);
        //设置完成状态。1为未完成，2为已完成，3为暂时搁置
        completion_status_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                completion_status = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //有意思的bug2：要放在setOnClickListenser后面，否则会出现进入新建待办事项页面时，默认不提醒，而提醒时间部件可以点的情况。
        ddl_remind.setOnClickListener(this);
        ddl_remind.setClickable(false);

        //设置是否提醒。若提醒，提醒日期可以设置；若不提醒，提醒日期不可设置
        alert_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    alert = true;
                    ddl_remind.setClickable(true);
                } else {
                    alert = false;
                    ddl_remind.setClickable(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.finish://添加完成的按钮
                if (TextUtils.isEmpty(content.getText())//截止日期不能为空
                        || TextUtils.isEmpty(year.getText()) || TextUtils.isEmpty(month.getText())
                        || TextUtils.isEmpty(day.getText()) || TextUtils.isEmpty(time.getText())) {
                    Toast.makeText(this, "截止日期或待办事项内容不能为空", Toast.LENGTH_SHORT).show();
                } else if (alert == true) {//若设置提醒，提醒日期不能为空
                    if (TextUtils.isEmpty(year_remind.getText()) || TextUtils.isEmpty(month_remind.getText())
                            || TextUtils.isEmpty(day_remind.getText()) || TextUtils.isEmpty(time_remind.getText())) {
                        Toast.makeText(this, "提醒日期不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        String ddl_str = year.getText().toString().trim() + "-"
                                + month.getText().toString().trim() + "-"
                                + day.getText().toString().trim() + " "
                                + time.getText().toString().trim() + ":"
                                + minute.getText().toString().trim() + ":00";
                        newItem.setDdl(Timestamp.valueOf(ddl_str));
                        newItem.setItemStatus(completion_status);
                        newItem.setAlert(alert);
                        String ddl_remind_str = year_remind.getText().toString().trim() + "-"
                                + month_remind.getText().toString().trim() + "-"
                                + day_remind.getText().toString().trim() + " "
                                + time_remind.getText().toString().trim() + ":"
                                + minute_remind.getText().toString().trim() + ":00";
                        newItem.setAlertTime(Timestamp.valueOf(ddl_remind_str));
                        newItem.setContent(content.getText().toString().trim());

                        //判断提醒日期和当前时间的大小
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//年-月-日 时:分
                        try {
                            Date currentDate = new Date();
                            Date date1 = dateFormat.parse(dateFormat.format(currentDate));
                            Date date2 = dateFormat.parse(ddl_remind_str);
                            if (date2.getTime() > date1.getTime()) {
                                Long userId = Long.parseLong(SystemStatus.getNow_account());
                                //上传新添加的待办事项
                                NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                                        .addTodoItemOfUser(userId, newItem)
                                        .enqueue(new Callback<ReturnVO<TodoItem>>() {
                                            @Override
                                            public void onResponse(Call<ReturnVO<TodoItem>> call,
                                                                   Response<ReturnVO<TodoItem>> response) {
                                                ReturnVO<TodoItem> body = response.body();
                                                assert body != null;
                                                if (body.getCode() == 500) {
                                                    Toast.makeText(AddToDoItemActivity.this, "添加失败",
                                                            Toast.LENGTH_SHORT).show();
                                                } else if (body.getCode() == 200) {
                                                    Toast.makeText(AddToDoItemActivity.this, "添加成功",
                                                            Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<ReturnVO<TodoItem>> call, Throwable t) {
                                            }
                                        });
                                finish();
                            }else{
                                Toast.makeText(this, "提醒日期应该大于当前系统时间，请重输", Toast.LENGTH_LONG).show();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } else {//设置不提醒，提醒日期无法设置，即提醒日期可以为空
                    String ddl_str = year.getText().toString().trim() + "-"
                            + month.getText().toString().trim() + "-"
                            + day.getText().toString().trim() + " "
                            + time.getText().toString().trim() + ":"
                            + minute.getText().toString().trim() + ":00";
                    newItem.setDdl(Timestamp.valueOf(ddl_str));
                    newItem.setItemStatus(completion_status);
                    newItem.setAlert(alert);

                    newItem.setContent(content.getText().toString().trim());
                    Long userId = Long.parseLong(SystemStatus.getNow_account());
                    NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                            .addTodoItemOfUser(userId, newItem)
                            .enqueue(new Callback<ReturnVO<TodoItem>>() {
                                @Override
                                public void onResponse(Call<ReturnVO<TodoItem>> call,
                                                       Response<ReturnVO<TodoItem>> response) {
                                    ReturnVO<TodoItem> body = response.body();
                                    assert body != null;
                                    if (body.getCode() == 500) {
                                        Toast.makeText(AddToDoItemActivity.this, "添加失败",
                                                Toast.LENGTH_SHORT).show();
                                    } else if (body.getCode() == 200) {
                                        Toast.makeText(AddToDoItemActivity.this, "添加成功",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                                @Override
                                public void onFailure(Call<ReturnVO<TodoItem>> call, Throwable t) {
                                }
                            });
                    finish();
                }
                break;
            case R.id.back_add://返回按钮
                finish();
                break;
            case R.id.ddl://点击截止日期文本框，跳出时间dialog
                AddTimeDialog dialog = new AddTimeDialog(this, "ddl");
                dialog.ShowDia();
                dialog.setMessage(this);
                break;
            case R.id.ddl_remind://点击提醒日期文本框，跳出时间dialog
                AddTimeDialog dialog2 = new AddTimeDialog(this, "ddl_remind");
                dialog2.ShowDia();
                dialog2.setMessage(this);
                break;
        }
    }

    //将跳出的时间设置窗中的值传到当前界面上的截止时间及提醒时间文本框上
    @Override
    public void changer(Map<String, String> map) {//接口在AddTimeDialog.java
        if ("ddl".equals(map.get("source"))) {
            year.setText(map.get("year"));
            month.setText(map.get("month"));
            day.setText(map.get("day"));
            time.setText(map.get("time"));
            minute.setText(map.get("minute"));
        } else {
            year_remind.setText(map.get("year"));
            month_remind.setText(map.get("month"));
            day_remind.setText(map.get("day"));
            time_remind.setText(map.get("time"));
            minute_remind.setText(map.get("minute"));
        }
    }
}
