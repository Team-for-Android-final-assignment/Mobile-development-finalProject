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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.team.weup.adapter.MainAdapter;
import com.team.weup.model.Note;
import com.team.weup.repo.NoteInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;
import com.team.weup.view.MyGridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class SearchActivity extends Activity {
	private MyGridView lv_notes;
	private TextView tv_note_id;
	public EditText et_keyword;
	public String keword;
	Intent intent;
	private List<Note> notes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		et_keyword = (EditText) findViewById(R.id.et_keyword);
		intent = getIntent();//获取当前传递对象
		keword = intent.getStringExtra("keword");//获取传递参数
		et_keyword.setText(keword);
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
		// 显示记事列表
		loadDataFromCloud();
	}

	// 显示记事列表
	private void showNotesList() {
		if (! notes.isEmpty() ) {
			List<Note> list = new ArrayList<Note>();//日记信息集合里
			//倒序显示数据
			for(int i=0;i<notes.size();i++){
				String text = notes.get(i).getText();
				String title = notes.get(i).getTitle();
				if(text.contains(keword) || title.contains(keword)){
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
								Intent intent = new Intent(SearchActivity.this,
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
			Intent intent = new Intent(SearchActivity.this, AddActivity.class);
			intent.putExtra("editModel", "update");
			intent.putExtra("noteId", item_id);
			intent.putExtra("title",title);
			intent.putExtra("text",text);
			startActivity(intent);

		}
	}


	// 搜索
	public void onSearch(View v) {
		String ek = et_keyword.getText().toString();
		if ("".equals(ek)) {
			Toast.makeText(SearchActivity.this, "请输入关键词！", Toast.LENGTH_LONG)
					.show();
		} else {
			keword=ek;
			showNotesList();
		}
	}

	public void onBack(View v) {
		SearchActivity.this.finish();
	}
}
