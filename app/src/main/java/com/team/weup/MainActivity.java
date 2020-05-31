package com.team.weup;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.team.weup.adapter.MainAdapter;
import com.team.weup.doubledatepicker.DoubleDatePickerDialog;
import com.team.weup.model.Note;
import com.team.weup.repo.NoteInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;
import com.team.weup.view.MyGridView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;


public class MainActivity extends Activity {
	private Button bt_add;// 添加按钮
	private MyGridView lv_notes;// 消息列表
	private TextView tv_note_id;
	public EditText et_keyword;// 搜索框
	private List<Note> notes;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bt_add = (Button) findViewById(R.id.bt_add);
		bt_add.setOnClickListener(new ClickEvent());
		et_keyword = (EditText) findViewById(R.id.et_keyword);
		lv_notes = (MyGridView) findViewById(R.id.lv_notes);
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
				PackageManager.PERMISSION_GRANTED ){
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
		}else {

		}
	}

	private void loadDataFromCloud(){
		NetworkUtil.getRetrofit().create(NoteInterface.class)
				.getNotesByUserId((long)(Integer.parseInt(SystemStatus.getNow_account())))
				.enqueue(new Callback<ReturnVO<List<Note>>>() {
					@Override
					@EverythingIsNonNull
					public void onResponse(Call<ReturnVO<List<Note>>> call, Response<ReturnVO<List<Note>>> response) {
						ReturnVO<List<Note>> body = response.body();
						assert body != null;
						List<Note> ln = body.getData();
						notes = ln;
						Log.d("MainActivityTest", "loadDataFromCloud:  success.");
						showNotesList();
					}

					@Override
					@EverythingIsNonNull
					public void onFailure(Call<ReturnVO<List<Note>>> call, Throwable t) {
						t.printStackTrace();
					}
				});
	}

	private void deleteNote(long id){
		NetworkUtil.getRetrofit().create(NoteInterface.class)
				.deleteNoteOfUser(id)
				.enqueue(new Callback<ReturnVO<Note>>() {
					@Override
					public void onResponse(Call<ReturnVO<Note>> call, Response<ReturnVO<Note>> response) {
						Log.d("MainActivityTest", "deleteNote: success.");
						loadDataFromCloud();
					}

					@Override
					public void onFailure(Call<ReturnVO<Note>> call, Throwable t) {

					}
				});
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 显示记事列表
		loadDataFromCloud();  //从云端把本地没有的数据取来
	}


	// 显示记事列表
	private void showNotesList() {
		//lv_notes.invalidate();
		if (! notes.isEmpty() ) {
			List<Note> list = new ArrayList<Note>(notes);//日记信息集合里
			//倒序显示数据
			Collections.reverse(list);
			MainAdapter adapter = new MainAdapter(list, this);//装载日记信息到首页
			lv_notes.setAdapter(adapter);//日记列表设置日记信息适配器
			// 为记事列表添加监听器
			lv_notes.setOnItemClickListener(new ItemClickEvent());
			// 为记事列表添加长按事件
			lv_notes.setOnItemLongClickListener(new ItemLongClickEvent());
		}
		else{
			lv_notes.setAdapter(null);
		}
	}

	// 记事列表长按监听器
	class ItemLongClickEvent implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long id) {
			//初始化日记id保存控件
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			//获取控件上id信息转换成int类型
			long item_id = Long.parseLong(tv_note_id.getText().toString());
			Note note = null;
			for(int i=0;i<notes.size();i++){
				if(notes.get(i).getId() == item_id){
					note = notes.get(i);
					break;
				}
			}
			String title = note.getTitle();
			String text = note.getText();
			//弹出选择操作框方法
			simpleList(item_id,title,text);
			return true;
		}
	}

	// 简单列表对话框，用于选择操作
	public void simpleList(final long item_id,String title,String text) {
		//实例化AlertDialog
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
				R.style.custom_dialog);
		//设置弹窗标题
		alertDialogBuilder.setTitle("选择操作");
		//设置弹窗图片
		alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
		//设置弹窗选项内容
		alertDialogBuilder.setItems(R.array.itemOperation,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							// 编辑
							case 0:
								Intent intent = new Intent(MainActivity.this,
										AddActivity.class);//跳转到添加日记页
								intent.putExtra("editModel", "update");//传递编辑信息
								intent.putExtra("noteId", item_id);//传递id信息
								intent.putExtra("title",title);
								intent.putExtra("text",text);
								startActivity(intent);//开始跳转
								break;
							// 删除
							case 1:
								deleteNote(item_id);
								// 刷新列表显示
								lv_notes.invalidate();
								break;
						}
					}
				});
		alertDialogBuilder.create();//创造弹窗
		alertDialogBuilder.show();//显示弹窗
	}

	// 记事列表单击监听器
	class ItemClickEvent implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			Log.d("MainActivityTest", ":position="+position+"   id="+id);
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);

			long item_id = Long.parseLong(tv_note_id.getText().toString());
			Note note = null;
			for(int i=0;i<notes.size();i++){
				if(notes.get(i).getId() == item_id){
					note = notes.get(i);
					break;
				}
			}
			String title = note.getTitle();
			String text = note.getText();
			Intent intent = new Intent(MainActivity.this, AddActivity.class);
			intent.putExtra("editModel", "update");
			intent.putExtra("noteId", item_id);
			intent.putExtra("title",title);
			intent.putExtra("text",text);
			startActivity(intent);

		}
	}

	// 点击事件
	class ClickEvent implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				// 添加记事
				case R.id.bt_add:
					Intent intent = new Intent(MainActivity.this, AddActivity.class);
					intent.putExtra("editModel", "newAdd");
					startActivity(intent);
			}
		}
	}
	// 搜索功能
	public void onSearch(View v) {
		//获取搜索关键词
		String ek = et_keyword.getText().toString();
		if ("".equals(ek)) {//判断搜索关键词是否为空
			Toast.makeText(MainActivity.this, "请输入关键词！", Toast.LENGTH_LONG)
					.show();
		} else {//搜索不为空
			//进入搜索结果页
			Intent intent = new Intent(MainActivity.this, SearchActivity.class);
			intent.putExtra("keword", ek);//传递关键词
			startActivity(intent);//开始跳转
		}
	}
	// 日期范围搜索  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!有修改
	public void onData(View v) {
		// 最后一个false表示不显示日期，如果要显示日期，最后参数可以是true或者不用输入
		Calendar c = Calendar.getInstance();
		new DoubleDatePickerDialog(MainActivity.this, 0,
				new DoubleDatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker startDatePicker,
										  int startYear, int startMonthOfYear,
										  int startDayOfMonth, DatePicker endDatePicker,
										  int endYear, int endMonthOfYear, int endDayOfMonth) {
						if (startYear < endYear || startYear == endYear
								&& startMonthOfYear < endMonthOfYear || startYear == endYear
								&& startMonthOfYear == endMonthOfYear && startDayOfMonth < endDayOfMonth) {
							int st = startMonthOfYear + 1;
							int et = endMonthOfYear + 1;
							Intent intent = new Intent(MainActivity.this,
									DataSearchActivity.class);
							// sql判断 需要在月份前补0 否则sql语句判断不正确。
							if (st < 10) {
								intent.putExtra("startData", startYear + "-0"
										+ st + "-" + getDays(startDayOfMonth));
							} else {
								intent.putExtra("startData", startYear + "-"
										+ st + "-" + getDays(startDayOfMonth));
							}
							if (et < 10) {
								intent.putExtra("endData", endYear + "-0" + et
										+ "-" + getDays(endDayOfMonth));
							} else {
								intent.putExtra("endData", endYear + "-" + et
										+ "-" + getDays(endDayOfMonth));
							}
							startActivity(intent);
						} else {
							Toast.makeText(MainActivity.this, "日期选择错误请重新选择！",
									Toast.LENGTH_LONG).show();
						}
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
				.get(Calendar.DATE), false).show();
	}

	public String getDays(int days){
		if(days<10){
			return "0"+days;
		}
		return ""+days;
	}
}
