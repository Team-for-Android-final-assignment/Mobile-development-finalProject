package com.team.weup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.team.weup.model.TodoItem;
import com.team.weup.repo.TodoItemInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 待办事项页面
 */

public class LifeFragment extends Fragment implements View.OnClickListener{
    private ListView listView;
    private Button addBt;//添加按钮
    ArrayList<HashMap<String,String>> list = new ArrayList<>();
    private boolean isFirstLoading = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_life, container, false);
        listView = (ListView)view.findViewById(R.id.listView);
        addBt = (Button)view.findViewById(R.id.addButton);
        addBt.setOnClickListener(this);

        //点击某个待办事项进入详情页面
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long i) {
                String id = list.get(position).get("id");
                Intent intent = new Intent();
                intent.setClass(getContext(), DetailActivity.class);
                intent.putExtra("note_id", Integer.parseInt(id));
                startActivity(intent);
            }
        });
        Log.i("列表刷新","待办列表设置进入详情界面的监视器");

        //长按实现对列表的删除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("确定删除？");
                builder.setTitle("提示");

                //添加AlterDialog.Builder对象的setPositiveButton()方法
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        long id = Long.parseLong(list.get(position).get("id"));
                        //删除某一待办事项
                        NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                                .deleteTodoItemById(id)
                                .enqueue(new Callback<ReturnVO<TodoItem>>() {
                                    @Override
                                    public void onResponse(Call<ReturnVO<TodoItem>> call,
                                                           Response<ReturnVO<TodoItem>> response) {
                                        ReturnVO<TodoItem> body = response.body();
                                        assert body != null;
                                        if(body.getCode()==500){
                                            Toast.makeText(getActivity(),"删除失败",Toast.LENGTH_LONG).show();
                                        }else if(body.getCode()==200){
                                            Toast.makeText(getActivity(),"删除成功",
                                                    Toast.LENGTH_SHORT).show();
                                            list.remove(position);
                                            //ListView加载list数据
                                            ListAdapter listAdapter = new SimpleAdapter(getContext(), list,
                                                    R.layout.item, new String[]{"id", "content","year","month","day","time", "minute", "completion_status"},
                                                    new int[]{R.id.note_id, R.id.note_title, R.id.note_ddl_year, R.id.note_ddl_month,
                                                            R.id.note_ddl_day, R.id.note_ddl_time, R.id.note_ddl_minute, R.id.note_completion_status});
                                            listView.setAdapter(listAdapter);
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<ReturnVO<TodoItem>> call, Throwable t) {

                                    }
                                });
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.create().show();
                return true;
            }
        });

        updateUI();
        isFirstLoading = false;
        Log.i("列表刷新","onCreateView");
        return view;
    }

    //列表刷新
    private void updateUI() {
        list.clear();
        long userId = Long.parseLong(SystemStatus.getNow_account());
        //获取某一个用户的所有待办事项的数据，存入list
        NetworkUtil.getRetrofit().create(TodoItemInterface.class)
                .getTodoItemsByUserId(userId)
                .enqueue(new Callback<ReturnVO<List<TodoItem>>>() {
                    @Override
                    public void onResponse(Call<ReturnVO<List<TodoItem>>> call,
                                           Response<ReturnVO<List<TodoItem>>> response) {
                        ReturnVO<List<TodoItem>> body = response.body();
                        assert body != null;
                        if (ReturnVO.ERROR.equals(body.getCode())) {
                            Toast.makeText(getActivity(), "获得待办列表失败", Toast.LENGTH_LONG).show();
                        } else if(body.getCode()==200){
                            Log.i("列表刷新","body.getCode==200");
                            List<TodoItem> data = body.getData();
                            for (int i = 0; i < data.size(); i++) {
                                HashMap<String, String> note = new HashMap<String, String>();
                                note.put("id", String.valueOf(data.get(i).getId()));
                                note.put("content", data.get(i).getContent());
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(new Date(data.get(i).getDdl().getTime()));
                                note.put("year", String.valueOf(calendar.get(Calendar.YEAR)));
                                note.put("month", String.valueOf(calendar.get(Calendar.MONTH)+1));
                                note.put("day", String.valueOf(calendar.get(Calendar.DATE)));
                                note.put("time", String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)));
                                note.put("minute", String.valueOf(calendar.get(Calendar.MINUTE)));
                                if(data.get(i).getItemStatus() == 1){
                                    note.put("completion_status","未完成");
                                }else if(data.get(i).getItemStatus() == 2){
                                    note.put("completion_status","已完成");
                                }else{
                                    note.put("completion_status","暂时搁置");
                                }
                                list.add(note);
                            }
                            Log.i("列表刷新","生成list");

                            //有意思的bug1：访问服务器完成之前，就进行了ListView加载list数据的操作，导致列表刷新的时候为空
                            //ListView加载list数据
                            ListAdapter listAdapter = new SimpleAdapter(getContext(), list,
                                    R.layout.item, new String[]{"id", "content","year","month","day","time", "minute", "completion_status"},
                                    new int[]{R.id.note_id, R.id.note_title, R.id.note_ddl_year, R.id.note_ddl_month,
                                            R.id.note_ddl_day, R.id.note_ddl_time, R.id.note_ddl_minute, R.id.note_completion_status});
                            listView.setAdapter(listAdapter);
                            Log.i("列表刷新","ListView加载list");
                        }
                    }
                    @Override
                    public void onFailure(Call<ReturnVO<List<TodoItem>>> call, Throwable t) {
                    }
                });

    }

    @Override
    public void onResume(){
        super.onResume();
        //回到待办事项页面时，不会导致待办列表刷新两次
        if(isFirstLoading == true){
            updateUI();
            Log.i("列表刷新","test");
        }
        isFirstLoading = true;
        Log.i("列表刷新","onResume");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addButton:
                //点击添加按钮进入添加待办事项页面
                Intent intent = new Intent(getActivity(), AddToDoItemActivity.class);
                startActivity(intent);
                break;
        }
    }
}
