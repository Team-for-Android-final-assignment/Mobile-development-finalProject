package com.team.weup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 待办事项详情页面
 */

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, AddTimeDialog.Message {
    private static final String TAG = "测试";

    private Button back;
    private Button save;

    /**
     * 下面的部件与AddActivity中的一致
     */
    private LinearLayout ddl;
    private TextView year, month, day, time, minute;
    private Spinner completion_status_spinner;
    private int completion_status;
    private Switch alert_switch;
    private boolean alert = true;
    private LinearLayout ddl_remind;
    private TextView year_remind, month_remind, day_remind, time_remind, minute_remind;
    private EditText content;

    private int note_id = 0;
    private TodoItem currentItem = new TodoItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        Log.i(TAG, "test1");

        initUI();

        //接收listView中点击item传来的note_id,
        Intent intent = getIntent();
        note_id = intent.getIntExtra("note_id", 0);
        Log.i(TAG, "note_id:" + String.valueOf(note_id));

        long userId = Long.parseLong(SystemStatus.getNow_account());
        NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                .getTodoItemsByUserId(userId)
                .enqueue(new Callback<ReturnVO<List<TodoItem>>>() {
                    @Override
                    public void onResponse(Call<ReturnVO<List<TodoItem>>> call,
                                           Response<ReturnVO<List<TodoItem>>> response) {
                        ReturnVO<List<TodoItem>> body = response.body();
                        if (body.getCode() == 500) {
                            Toast.makeText(DetailActivity.this, "无法获得列表",
                                    Toast.LENGTH_SHORT).show();
                        } else if (body.getCode() == 200) {
                            List<TodoItem> data = body.getData();
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getId() == note_id) {
                                    currentItem.setId((long)note_id);
                                    currentItem.setDdl(data.get(i).getDdl());
                                    currentItem.setItemStatus(data.get(i).getItemStatus());
                                    currentItem.setAlert(data.get(i).getAlert());
                                    currentItem.setContent(data.get(i).getContent());
                                    if (data.get(i).getAlert() == true) {
                                        currentItem.setAlertTime(data.get(i).getAlertTime());
                                        Calendar calendar2 = Calendar.getInstance();
                                        calendar2.setTime(new Date(data.get(i).getAlertTime().getTime()));
                                        year_remind.setText(String.valueOf(calendar2.get(Calendar.YEAR)));
                                        month_remind.setText(String.valueOf(calendar2.get(Calendar.MONTH) + 1));
                                        day_remind.setText(String.valueOf(calendar2.get(Calendar.DATE)));
                                        time_remind.setText(String.valueOf(calendar2.get(Calendar.HOUR_OF_DAY)));
                                        minute_remind.setText(String.valueOf(calendar2.get(Calendar.MINUTE)));
                                    }
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(new Date(data.get(i).getDdl().getTime()));
                                    year.setText(String.valueOf(calendar.get(Calendar.YEAR)));
                                    month.setText(String.valueOf(calendar.get(Calendar.MONTH) + 1));
                                    day.setText(String.valueOf(calendar.get(Calendar.DATE)));
                                    time.setText(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
                                    minute.setText(String.valueOf(calendar.get(Calendar.MINUTE)));
                                    completion_status_spinner.setSelection(data.get(i).getItemStatus() - 1);
                                    alert_switch.setChecked(data.get(i).getAlert());
                                    content.setText(data.get(i).getContent());
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ReturnVO<List<TodoItem>>> call, Throwable t) {

                    }
                });
    }

    private void initUI() {
        back = (Button) findViewById(R.id.back_detail);
        save = (Button) findViewById(R.id.save_detail);

        ddl = (LinearLayout) findViewById(R.id.edit_ddl);
        year = (TextView) findViewById(R.id.edit_ddl_year);
        month = (TextView) findViewById(R.id.edit_ddl_month);
        day = (TextView) findViewById(R.id.edit_ddl_day);
        time = (TextView) findViewById(R.id.edit_ddl_time);
        minute = (TextView) findViewById(R.id.edit_ddl_minute);
        completion_status_spinner = (Spinner) findViewById(R.id.completion_status_spinner_edit);
        alert_switch = (Switch) findViewById(R.id.alert_switch_edit);
        ddl_remind = (LinearLayout) findViewById(R.id.ddl_remind_edit);
        year_remind = (TextView) findViewById(R.id.ddl_year_remind_edit);
        month_remind = (TextView) findViewById(R.id.ddl_month_remind_edit);
        day_remind = (TextView) findViewById(R.id.ddl_day_remind_edit);
        time_remind = (TextView) findViewById(R.id.ddl_time_remind_edit);
        minute_remind = (TextView) findViewById(R.id.ddl_minute_remind_edit);
        content = (EditText) findViewById(R.id.content_edit);

        back.setOnClickListener(this);
        save.setOnClickListener(this);
        ddl.setOnClickListener(this);
        completion_status_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                completion_status = position + 1;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ddl_remind.setOnClickListener(this::onClick);
        alert_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    alert = true;
                    currentItem.setAlert(true);
                    ddl_remind.setClickable(true);
                } else {
                    alert = false;
                    currentItem.setAlert(false);
                    ddl_remind.setClickable(false);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_detail:
                finish();
                break;
            case R.id.save_detail:
                if (TextUtils.isEmpty(content.getText())
                        || TextUtils.isEmpty(year.getText()) || TextUtils.isEmpty(month.getText())
                        || TextUtils.isEmpty(day.getText()) || TextUtils.isEmpty(time.getText())) {
                    Toast.makeText(this, "截止日期或待办事项内容不能为空", Toast.LENGTH_SHORT).show();
                } else if (currentItem.getAlert() == true) {
                    if (TextUtils.isEmpty(year_remind.getText()) || TextUtils.isEmpty(month_remind.getText())
                            || TextUtils.isEmpty(day_remind.getText()) || TextUtils.isEmpty(time_remind.getText())) {
                        Toast.makeText(this, "提醒日期不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        String ddl_str = year.getText().toString().trim() + "-"
                                + month.getText().toString().trim() + "-"
                                + day.getText().toString().trim() + " "
                                + time.getText().toString().trim() + ":"
                                + minute.getText().toString().trim() + ":00";
                        currentItem.setDdl(Timestamp.valueOf(ddl_str));
                        currentItem.setItemStatus(completion_status);
                        currentItem.setAlert(alert);
                        String ddl_remind_str = year_remind.getText().toString().trim() + "-"
                                + month_remind.getText().toString().trim() + "-"
                                + day_remind.getText().toString().trim() + " "
                                + time_remind.getText().toString().trim() + ":"
                                + minute_remind.getText().toString().trim() + ":00";
                        currentItem.setAlertTime(Timestamp.valueOf(ddl_remind_str));
                        currentItem.setContent(content.getText().toString().trim());

                        //判断提醒日期和当前时间的大小
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//年-月-日 时:分
                        try {
                            Date currentDate = new Date();
                            Date date1 = dateFormat.parse(dateFormat.format(currentDate));
                            Date date2 = dateFormat.parse(ddl_remind_str);
                            if(date2.getTime() > date1.getTime()){
                                //更新待办事项
                                NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                                        .updateTodoItemById((long) note_id, currentItem)
                                        .enqueue(new Callback<ReturnVO<TodoItem>>() {
                                            @Override
                                            public void onResponse(Call<ReturnVO<TodoItem>> call,
                                                                   Response<ReturnVO<TodoItem>> response) {
                                                ReturnVO<TodoItem> body = response.body();
                                                if (body.getCode() == 500) {
                                                    Toast.makeText(DetailActivity.this, "修改失败",
                                                            Toast.LENGTH_SHORT).show();
                                                } else if (body.getCode() == 200) {
                                                    Toast.makeText(DetailActivity.this, "修改成功",
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
                                Toast.makeText(this, "提醒日期不合法，请重输", Toast.LENGTH_LONG).show();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }
                } else {
                    String ddl_str = year.getText().toString().trim() + "-"
                            + month.getText().toString().trim() + "-"
                            + day.getText().toString().trim() + " "
                            + time.getText().toString().trim() + ":"
                            + minute.getText().toString().trim() + ":00";
                    currentItem.setDdl(Timestamp.valueOf(ddl_str));
                    currentItem.setItemStatus(completion_status);
                    currentItem.setAlert(alert);

                    currentItem.setContent(content.getText().toString().trim());
                    NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                            .updateTodoItemById((long) note_id, currentItem)
                            .enqueue(new Callback<ReturnVO<TodoItem>>() {
                                @Override
                                public void onResponse(Call<ReturnVO<TodoItem>> call,
                                                       Response<ReturnVO<TodoItem>> response) {
                                    ReturnVO<TodoItem> body = response.body();
                                    if (body.getCode() == 500) {
                                        Toast.makeText(DetailActivity.this, "修改失败",
                                                Toast.LENGTH_SHORT).show();
                                    } else if (body.getCode() == 200) {
                                        Toast.makeText(DetailActivity.this, "修改成功",
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
            case R.id.edit_ddl:
                AddTimeDialog dialog = new AddTimeDialog(this, "ddl");
                dialog.ShowDia();
                dialog.setMessage(this);
                break;
            case R.id.ddl_remind_edit:
                AddTimeDialog dialog2 = new AddTimeDialog(this, "ddl_remind");
                dialog2.ShowDia();
                dialog2.setMessage(this);
                break;
        }
    }

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
