package com.team.weup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.team.weup.adapter.MainAdapter;
import com.team.weup.doubledatepicker.DoubleDatePickerDialog;
import com.team.weup.model.Note;
import com.team.weup.repo.NoteInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;
import com.team.weup.view.MyGridView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;


public class DataSearchActivity extends Activity {
	private MyGridView lv_notes;
	private TextView tv_note_id;
	public TextView et_keyword;
	public String startData, endData;
	private List<Note> notes;
	Intent intent;
	private int startYear,startMonth,startDay,endYear,endMonth,endDay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datasearch);
		et_keyword = (TextView) findViewById(R.id.et_keyword);
		intent = getIntent();
		startData = intent.getStringExtra("startData");
		endData = intent.getStringExtra("endData");

		// 数据库操作
		et_keyword.setText("开始时间：" + startData + " 00:00:00"+" \n" + "结束时间：" + endData + " 00:00:00");
		lv_notes = (MyGridView) findViewById(R.id.lv_notes);
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
		loadDataFromCloud();  //从云端把本地没有的数据取来
	}

	// 显示记事列表
	private void showNotesList() {
		String[] starts = startData.split("-");
		String[] ends = endData.split("-");

		startYear = Integer.parseInt(starts[0]);
		startMonth = Integer.parseInt(starts[1]);
		startDay = Integer.parseInt(starts[2]);
		endYear = Integer.parseInt(ends[0]);
		endMonth = Integer.parseInt(ends[1]);
		endDay = Integer.parseInt(ends[2]);

		if (! notes.isEmpty() ) {
			List<Note> list = new ArrayList<Note>();//日记信息集合里
			Timestamp start = Timestamp.valueOf(startData + " 00:00:00");
			Timestamp end = Timestamp.valueOf(endData + " 00:00:00");
			//倒序显示数据
			for(int i=0;i<notes.size();i++){
				Timestamp time = notes.get(i).getEditTime();
				if(start.before(time) && time.before(end)){
					list.add(notes.get(i));
				}
			}
			if(!list.isEmpty()) {
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
								Intent intent = new Intent(DataSearchActivity.this,
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
								//SystemClock.sleep(100);
								// 刷新列表显示
								lv_notes.invalidate();
								//loadDataFromCloud();
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
			Intent intent = new Intent(DataSearchActivity.this, AddActivity.class);
			intent.putExtra("editModel", "update");
			intent.putExtra("noteId", item_id);
			intent.putExtra("title",title);
			intent.putExtra("text",text);
			startActivity(intent);

		}
	}

	// 搜索
	public void onSearch(View v) {
		// 最后一个false表示不显示日期，如果要显示日期，最后参数可以是true或者不用输入
		Calendar c = Calendar.getInstance();
		new DoubleDatePickerDialog(DataSearchActivity.this, 0,
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
							if(st<10){
								startData = startYear + "-0" + st + "-" + getDays(startDayOfMonth);
							}else{
								startData = startYear + "-" + st + "-" + getDays(startDayOfMonth);
							}
							if(et<10){
								endData = endYear + "-0" + et + "-" + getDays(endDayOfMonth);
							}else{
								endData = endYear + "-" + et + "-" + getDays(endDayOfMonth);
							}

							et_keyword.setText("开始时间：" + startData + " 00:00:00"+" \n" + "结束时间：" + endData + " 00:00:00");
							showNotesList();
						} else {
							Toast.makeText(DataSearchActivity.this,
									"日期选择错误请重新选择！", Toast.LENGTH_LONG).show();
						}

					}
				}, startYear,startMonth-1,startDay,endYear,endMonth-1,endDay, false).show();

	}

	public void onBack(View v) {
		DataSearchActivity.this.finish();
	}

	public String getDays(int days){
		if(days<10){
			return "0"+days;
		}
		return ""+days;
	}

}
