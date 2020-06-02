package com.team.weup;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.team.weup.model.Note;
import com.team.weup.repo.NoteInterface;
import com.team.weup.repo.UserInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;



public class AddActivity extends Activity {
    private Button bt_back;
    private Button bt_save;
    private TextView tv_title;
    private EditText note_title;
    private EditText et_Notes;
    private GridView bottomMenu;
    private int[] bottomItems = {
            R.drawable.tabbar_paint, R.drawable.tabbar_microphone,
            R.drawable.tabbar_photo, R.drawable.tabbar_camera,};
    InputMethodManager imm;//控制手机键盘
    Intent intent;
    String editModel = null;
    long item_Id;
    String item_title;
    String item_text;

    private ScrollView sclv;
    // 记录editText中的图片，用于单击时判断单击的是那一个图片
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();

    private static final int REQUEST_CHOOSE_PHOTO_PERMISSION = 1;
    private static final int REQUEST_OPEN_CAMERA_PERMISSION = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 3;
    private static final int REQUEST_PAINT_PERMISSION = 4;
    static final int  CHOOSE_PHOTO = 11;
    static final int OPEN_CAMERA = 12;
    static final int RECORD_AUDIO = 13;
    static final int DRAW_PIC = 14;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        bt_back = (Button) findViewById(R.id.bt_back);
        bt_back.setOnClickListener(new ClickEvent());
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new ClickEvent());
        tv_title = (TextView) findViewById(R.id.tv_title);
        note_title = (EditText) findViewById(R.id.note_title);
        et_Notes = (EditText) findViewById(R.id.et_note);
        bottomMenu = (GridView) findViewById(R.id.bottomMenu);
        sclv = (ScrollView) findViewById(R.id.sclv);

        // 配置菜单
        initBottomMenu();
        // 为菜单设置监听器
        bottomMenu.setOnItemClickListener(new MenuClickEvent());
        // 默认关闭软键盘,可以通过失去焦点设置
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_Notes.getWindowToken(), 0);
        intent = getIntent();
        editModel = intent.getStringExtra("editModel");
        item_Id = intent.getLongExtra("noteId", 0);

        // 加载数据
        loadData();
        // 给editText添加单击事件
        et_Notes.setOnClickListener(new TextClickEvent());


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("AddActivityTest", "onDestroy: ");
    }

    private void getFileFromServer(String path){
        Log.d("AddActivityTest", "File not exist. path=" + path);

        String[] ss = path.split("/");

        Log.d("AddActivityTest", "server path=" + ss[ss.length-1]);
        String serverPath = "http://123.56.85.195/upload/"+ss[ss.length-1];
        Log.d("AddActivityTest", "server path="+ serverPath);

        HttpURLConnection conn = null;
        String type = serverPath.substring(serverPath.length() - 3, serverPath.length());

        if(!type.equals("amr")) {
            Bitmap bitmap = null;
            try {
                conn = (HttpURLConnection) new URL(serverPath).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                InputStream inputStream = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                Log.d("AddActivityTest", "success download pic " + serverPath);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            if (bitmap != null) {
                String str = ss[ss.length - 1];
                File dir = new File("/storage/emulated/0/notes/");
                File file = new File("/storage/emulated/0/notes/", str);
                String newPath = "/storage/emulated/0/notes/" + str;
                Log.d("AddActivityTest", "download picture path in local=" + newPath);
                if (!dir.exists()) {
                    dir.mkdir();
                } else {
                    if (file.exists()) {
                        file.delete();
                    }
                }
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 将照片添加到图库
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(newPath);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }else{
            String str = ss[ss.length - 1];
            File dir = new File("/storage/emulated/0/notes/");
            File file = new File("/storage/emulated/0/notes/", str);
            String newPath = "/storage/emulated/0/notes/" + str;
            Log.d("AddActivityTest", "download amr path in local=" + newPath);
            if (!dir.exists()) {
                dir.mkdir();
            } else {
                if (file.exists()) {
                    file.delete();
                }
            }
            InputStream inputStream = null;
            FileOutputStream out = null;
            try {
                conn = (HttpURLConnection) new URL(serverPath).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                inputStream = conn.getInputStream();

                out = new FileOutputStream(file);
                byte[] buffer=new byte[1024];
                int len = 0;
                while(( len = inputStream.read(buffer) )!=-1){
                    out.write(buffer,0,len);
                }
                out.flush();
                Log.d("AddActivityTest", "success download amr " + serverPath);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }

    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    reLoadData();
            }
        }
    };


    public class NetWorkThread extends Thread{
        private String path;
        public NetWorkThread(String path)
        {
            this.path = path;
        }
        public void run()
        {
            getFileFromServer(path);
        }
    }

    // 1、加载数据
    private void loadData() {
        // 如果是新增记事模式，则将editText清空
        if (editModel.equals("newAdd")) {
            et_Notes.setText("");
        }
        // 如果编辑的是已存在的记事，则将数据库的保存的数据取出，并显示在EditText中
        else if (editModel.equals("update")) {
            tv_title.setText("编辑记事");
            item_title = intent.getStringExtra("title");
            item_text = intent.getStringExtra("text");
            note_title.setText(item_title);
            Log.d("AddActivityTest", "loadData: context="+item_text);
            Log.d("AddActivityTest", "loadData: len="+item_text.length());
            // 定义正则表达式，用于匹配路径
            Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
            Matcher m = p.matcher(item_text);
            int startIndex = 0;
            while (m.find()) {
                // 取出路径前的文字
                if (m.start() > 0) {
                    et_Notes.append(item_text.substring(startIndex, m.start()));
                }
                SpannableString ss = new SpannableString(m.group().toString());
                // 取出路径
                String path = m.group().toString();
                File f =new File(path);
                if(!f.exists()) {
                    new NetWorkThread(path).start();
//                    String newPath = getFileFromServer(path);
//                    Log.d("AddActivityTest", "LoadData: FileNoteExist----" + path);
//                    Log.d("AddActivityTest", "newPath= " + newPath);
//                    if(newPath != null) {
//                        path = newPath;
//                    }
                }
                // 取出路径的后缀
                String type = path.substring(path.length() - 3, path.length());
                Bitmap bm = null;
                Bitmap rbm = null;
                // 判断附件的类型，如果是录音文件，则从资源文件中加载图片
                if (type.equals("amr")) {
                    bm = BitmapFactory.decodeResource(getResources(),
                            R.drawable.record_icon);
                    // 缩放图片
                    if(bm != null) {
                        rbm = resize(bm, 400);
                    }
                } else {
                    // 取出图片

                    bm = BitmapFactory.decodeFile(m.group());
                    if(bm != null) {
                        // 缩放图片
                        rbm = resize(bm, 480);
                        // 为图片添加边框效果
                        rbm = getBitmapHuaSeBianKuang(rbm);
                    }
                }
                if(bm != null) {
                    ImageSpan span = new ImageSpan(this, rbm);
                    ss.setSpan(span, 0, m.end() - m.start(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //System.out.println(m.start() + "-------" + m.end());
                Log.d("AddActivityTest", "loadData: "+m.start() + "-------" + m.end());
                et_Notes.append(ss);
                startIndex = m.end();
                // 用List记录该录音的位置及所在路径，用于单击事件
                Map<String, String> map = new HashMap<String, String>();
                map.put("location", m.start() + "-" + m.end());
                map.put("path", path);
                imgList.add(map);
            }
            // 将最后一个图片之后的文字添加在TextView中
            et_Notes.append(item_text.substring(startIndex, item_text.length()));
        }
    }

    private void reLoadData(){
        et_Notes.setText("");
        loadData();
    }

    ///2、为EidtText设置监听器
    class TextClickEvent implements OnClickListener {
        @Override
        public void onClick(View v) {
            Spanned s = et_Notes.getText();
            String context = et_Notes.getText().toString();
            Log.d("AddActivityTest", "TextClickEvent: context="+context);
            int selectionStart = et_Notes.getSelectionStart();
            Log.d("AddActivityTest", "TextClickEvent: subcontext="+context.substring(selectionStart));
            Pattern p = Pattern.compile("/([^\\.]*)\\.\\w{3}");
            Matcher m = p.matcher(context.substring(selectionStart));
            if(m.find() && m.start() == 0){
                String path = m.group();
                if(path == null){
                    Toast.makeText(AddActivity.this, "error! path==null", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    File f =new File(path);
                    if(f.exists()) {
                        // 接着判断当前图片是否是录音，如果为录音，则跳转到试听录音的Activity，如果不是，则跳转到查看图片的界面
                        // 录音，则跳转到试听录音的Activity
                        if (path.substring(path.length() - 3, path.length())
                                .equals("amr")) {
                            Intent intent = new Intent(AddActivity.this,
                                    ShowRecord.class);
                            intent.putExtra("audioPath", path);
                            startActivity(intent);
                        }
                        // 图片，则跳转到查看图片的界面
                        else {
                            // 有两种方法，查看图片，第一种就是直接调用系统的图库查看图片，第二种是自定义Activity
                            // 调用系统图库查看图片
                            Log.d("AddActivityTest", "2-TextClickEvent  path=" + path);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            Log.d("AddActivityTest", "getImageContentUri  uri=" + getImageContentUri(AddActivity.this, path));
                            intent.setDataAndType(getImageContentUri(AddActivity.this, path), "image/*");
                            startActivity(intent);

                        }
                    }else{
                        Log.d("AddActivityTest", "2-TextClickEvent  FileNoteExist");
                    }
                }
            }

        }
    }

    //图片文件绝对路径 --> content://media/external/images/media 开头的Uri
    public static Uri getImageContentUri(Context context, String filePath) {

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        }
        return null;
    }


    // 4、设置按钮监听器
    class ClickEvent implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_back:

                    String databasecontext = null;
                    if(editModel.equals("update")) {
                        databasecontext = item_text;
                    }
                    if((databasecontext!=null && databasecontext.equals(et_Notes.getText().toString()) == false)
                            ||(databasecontext==null && !et_Notes.getText().toString().isEmpty())){
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this,R.style.custom_dialog);
                        builder.setTitle("返回提示");
                        builder.setMessage("有修改，是否保存本次编辑？");

                        builder.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AddActivity.this.finish();
                            }
                        });

                        builder.setNeutralButton("取消", null);
                        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                save();
                            }
                        });
                        builder.show();
                    }
                    else{
                        // 当前Activity结束，则返回上一个Activity
                        AddActivity.this.finish();
                    }
                    break;
                // 将记事添加到数据库中
                case R.id.bt_save:
                    // 取得EditText中的内容
                    save();
                    break;
            }
        }
    }

    //按主机键返回时
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            String databasecontext = null;
            if(editModel.equals("update")) {
                databasecontext = item_text;
            }
            if((databasecontext!=null && databasecontext.equals(et_Notes.getText().toString()) == false)
                    ||(databasecontext==null && !et_Notes.getText().toString().isEmpty())){
                AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this,R.style.custom_dialog);
                builder.setTitle("返回提示");
                builder.setMessage("有修改，是否保存本次编辑？");

                builder.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddActivity.this.finish();
                    }
                });

                builder.setNeutralButton("取消", null);
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        save();
                    }
                });
                builder.show();
            }
            else{
                // 当前Activity结束，则返回上一个Activity
                AddActivity.this.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void addNote(Note note){
        NetworkUtil.getRetrofit().create(NoteInterface.class)
                .addNoteOfUser((long)(Integer.parseInt(SystemStatus.getNow_account())),note)
                .enqueue(new Callback<ReturnVO<Note>>() {
                    @Override
                    public void onResponse(Call<ReturnVO<Note>> call, Response<ReturnVO<Note>> response) {

                    }

                    @Override
                    public void onFailure(Call<ReturnVO<Note>> call, Throwable t) {

                    }
                });
    }

    private void updateNote(Note note){
        NetworkUtil.getRetrofit().create(NoteInterface.class)
                .updateNote(item_Id,note)
                .enqueue(new Callback<ReturnVO<Note>>() {
                    @Override
                    public void onResponse(Call<ReturnVO<Note>> call, Response<ReturnVO<Note>> response) {

                    }

                    @Override
                    public void onFailure(Call<ReturnVO<Note>> call, Throwable t) {

                    }
                });
    }

    private void uploadFile(String realPathFromURI)  {
        File file = new File(realPathFromURI);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part bodyFile = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        NetworkUtil.getRetrofit().create(UserInterface.class)
                .upload(bodyFile)
                .enqueue(new Callback<ReturnVO<String>>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<ReturnVO<String>> call, Response<ReturnVO<String>> response) {
                        ReturnVO<String> body = response.body();
                        assert body != null;
                        if (body.getCode().equals(ReturnVO.OK)) {
                            Log.i("AddActivityTest", "uploadFile: 文件名为" + body.getData());
                        } else {
                            Log.e("AddActivityTest", "uploadFile: " + body.getMessage(), new Exception("上传文件出错"));
                        }
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<ReturnVO<String>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    private void save(){
        String context = et_Notes.getText().toString();
        String title = note_title.getText().toString();
        if(title.isEmpty()){
            Toast.makeText(AddActivity.this, "标题为空!", Toast.LENGTH_LONG)
                    .show();
        }
        else if (context.isEmpty()) {
            Toast.makeText(AddActivity.this, "记事为空!", Toast.LENGTH_LONG)
                    .show();
        } else {
            // 取得当前时间
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String time = formatter.format(curDate);


            // 判断是更新还是新增记事
            if (editModel.equals("newAdd")) {
                // 将记事插入到数据库中

                Note note = new Note();
                //note.setId(item_Id);
                note.setTitle(title);
                note.setText(context);
                note.setEditTime(Timestamp.valueOf(time));
                addNote(note);
            }
            // 如果是编辑则更新记事即可
            else if (editModel.equals("update")) {
                Note note = new Note();
                note.setId(item_Id);
                note.setTitle(title);
                note.setText(context);
                note.setEditTime(Timestamp.valueOf(time));
                updateNote(note);
            }

            // 结束当前activity
            AddActivity.this.finish();
        }
    }

    ///6、配置菜单
    private void initBottomMenu() {
        //菜单集合
        ArrayList<Map<String, Object>> menus = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < bottomItems.length; i++) {//循环菜单集合
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("image", bottomItems[i]);//循环图片集合添加到菜单中
            menus.add(item);//添加图片菜单到底部菜单
        }
        //菜单长度
        bottomMenu.setNumColumns(bottomItems.length);
        //底部菜单
        bottomMenu.setSelector(R.drawable.bottom_item);
        //实例化底部菜单适配器
        SimpleAdapter mAdapter = new SimpleAdapter(AddActivity.this, menus,
                R.layout.item_button, new String[]{"image"},
                new int[]{R.id.item_image});
        bottomMenu.setAdapter(mAdapter);//为底部菜单添加适配器
    }

    // 7、设置菜单项监听器
    class MenuClickEvent implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent;
            switch (position) {
                // 绘图
                case 0:
                    if (ContextCompat.checkSelfPermission(AddActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED ){
                        ActivityCompat.requestPermissions(AddActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PAINT_PERMISSION);
                    }else {
                        paint();
                    }
                    break;
                // 语音
                case 1:
                    if (ContextCompat.checkSelfPermission(AddActivity.this, Manifest.permission.RECORD_AUDIO) !=
                            PackageManager.PERMISSION_GRANTED ){
                        ActivityCompat.requestPermissions(AddActivity.this, new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_RECORD_AUDIO_PERMISSION);
                    }else {
                        recordAudio();
                    }
                    break;
                // 照片
                case 2:
                    if (ContextCompat.checkSelfPermission(AddActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED ){
                        ActivityCompat.requestPermissions(AddActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CHOOSE_PHOTO_PERMISSION);
                    }else {
                        choosePhoto();
                    }
                    break;
                // 拍照
                case 3:
                    List<String> permissionList = new ArrayList<>();

                    if(ContextCompat.checkSelfPermission(AddActivity.this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ) {
                        permissionList.add(Manifest.permission.CAMERA);
                    }
                    if(ContextCompat.checkSelfPermission(AddActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if (!permissionList.isEmpty()) {
                        ActivityCompat.requestPermissions(AddActivity.this,
                                permissionList.toArray(new String[permissionList.size()]), REQUEST_OPEN_CAMERA_PERMISSION);
                    } else {
                        openCamera();
                    }
                    break;
            }
        }
    }

    private void paint(){
        intent = new Intent(AddActivity.this, PaintActivity.class);
        startActivityForResult(intent, DRAW_PIC);
    }

    private void openCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, OPEN_CAMERA);
        }
    }

    private  void choosePhoto(){
        // 添加图片的主要代码
        intent = new Intent();
        // 设定类型为image
        intent.setType("image/*");
        // 设置action
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Log.d("AddActivityTest", "MenuClickEvent: choose pics button");
        // 选中相片后返回本Activity
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    private  void recordAudio(){
        intent = new Intent(AddActivity.this, ActivityRecord.class);
        startActivityForResult(intent, RECORD_AUDIO);
    }

    private void openCamera2(){
        // 调用系统拍照界面
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 区分选择相片
        // 请注意，startActivityForResult() 方法受调用 resolveActivity()（返回可处理 Intent 的第一个 Activity 组件）的条件保护。
        // 执行此检查非常重要，因为如果您使用任何应用都无法处理的 Intent 调用 startActivityForResult()，您的应用就会崩溃。
        // 所以只要结果不是 Null，就可以放心使用 Intent。
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created

            if (photoFile != null) {
                //mCameraImagePath = photoFile.getAbsolutePath();
                //Log.d("AddActivityTest", "openCamera: mCameraImagePath="+mCameraImagePath);
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                //mCameraUri = photoURI;
                //Log.d("AddActivityTest", "openCamera: mCameraUri=" + mCameraUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(takePictureIntent, 2);
            }


            //startActivityForResult(intent, 2);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OPEN_CAMERA_PERMISSION) {
            if (grantResults.length > 0) {
                // 因为是多个权限，所以需要一个循环获取每个权限的获取情况
                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    // PERMISSION_DENIED 这个值代表是没有授权，我们可以把被拒绝授权的权限显示出来
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                        flag = false;
                        Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                    }
                }
                if(flag == true){
                    openCamera();
                }
            }
        }
        else if(requestCode == REQUEST_CHOOSE_PHOTO_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                choosePhoto();
            }else{
                Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQUEST_RECORD_AUDIO_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                recordAudio();
            }else{
                Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == REQUEST_PAINT_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                paint();
            }else{
                Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
            }
        }
    }


    //9、数据回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 取得数据
            Uri uri = data.getData();
            //图片用于储存选择后转换成Bitmap类型
            Bitmap bitmap = null;
            //接收返回信息
            Bundle extras = null;
            // 如果是选择照片
            if (requestCode == CHOOSE_PHOTO) {
                String path = handleImageOnKitKat(data);
                bitmap = BitmapFactory.decodeFile(path);
                String[] ss = path.split("/");
                String newPath = ss[ss.length-1];
                try {
                    // 将拍的照片存入指定的文件夹下
                    // 获得系统当前时间，并以该时间作为文件名
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyyMMddHHmmss");
                    // 获取当前时间
                    Date curDate = new Date(System.currentTimeMillis());
                    // 当前时间保存成String类型
                    String str = formatter.format(curDate);
                    //图片路径
                    str = str + "picture.png";
                    //新建文件夹
                    File dir = new File("/storage/emulated/0/notes/");
                    //新建文件
                    File file = new File("/storage/emulated/0/notes/", str);
                    if (!dir.exists()) {// 判断文件夹创建是否成功
                        dir.mkdir();// 创建文件夹
                    } else {
                        if (file.exists()) {// 判断文件是否创建
                            file.delete();// 删除文件
                        }
                    }

                    //新建文件流
                    FileOutputStream fos = new FileOutputStream(file);
                    // 将 bitmap 压缩成其他格式的图片数据
                    bitmap.compress(CompressFormat.PNG, 100, fos);
                    fos.flush();//结束流传输
                    fos.close();//关闭流

                    //图片路径
                    String imgPath = "/storage/emulated/0/notes/"+str;

                    // 将照片添加到图库
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(imgPath);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);

                    //插入图片
                    InsertBitmap(bitmap, 480, imgPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 插入图片;
                //InsertBitmap(bitmap, 480, path);
            }
            // 选择的是拍照
            else if (requestCode == OPEN_CAMERA) {
                Log.d("AddActivityTest", "onActivityResult:  2");
                extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");
                try {
                    // 将拍的照片存入指定的文件夹下
                    // 获得系统当前时间，并以该时间作为文件名
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyyMMddHHmmss");
                    // 获取当前时间
                    Date curDate = new Date(System.currentTimeMillis());
                    // 当前时间保存成String类型
                    String str = formatter.format(curDate);
                    //图片路径
                    str = str + "photo.png";
                    //新建文件夹
                    File dir = new File("/storage/emulated/0/notes/");
                    //新建文件
                    File file = new File("/storage/emulated/0/notes/", str);
                    if (!dir.exists()) {// 判断文件夹创建是否成功
                        dir.mkdir();// 创建文件夹
                    } else {
                        if (file.exists()) {// 判断文件是否创建
                            file.delete();// 删除文件
                        }
                    }

                    //新建文件流
                    FileOutputStream fos = new FileOutputStream(file);
                    // 将 bitmap 压缩成其他格式的图片数据
                    bitmap.compress(CompressFormat.PNG, 100, fos);
                    fos.flush();//结束流传输
                    fos.close();//关闭流

                    //图片路径
                    String imgPath = "/storage/emulated/0/notes/"+str;

                    Log.d("AddActivityTest", "9-onActivityResult-OPEN_CAMERA  path=" + imgPath);
                    Log.d("AddActivityTest", "getExternalStorageDirectory="+Environment.getExternalStorageDirectory());
                    // 将照片添加到图库
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(imgPath);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);

                    //插入图片
                    InsertBitmap(bitmap, 480, imgPath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // 返回的是绘图后的结果
            else if (requestCode == DRAW_PIC) {
                //创建接收器
                extras = data.getExtras();
                //接收返回的信息
                String path = extras.getString("paintPath");
                // 将照片添加到图库
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(path);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                Log.d("AddActivityTest", "9-onActivityResult-DRAW_PIC  path=" + path);
                // 通过路径取出图片，放入bitmap中
                bitmap = BitmapFactory.decodeFile(path);
                // 插入绘图文件
                InsertBitmap(bitmap, 480, path);
            }
            // 返回的是录音文件
            else if (requestCode == RECORD_AUDIO) {
                //创建接收器
                extras = data.getExtras();
                //接收返回的信息
                String path = extras.getString("audio");
                //转换图片成bitmap形式
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.record_icon);
                // 插入录音图标
                InsertBitmap(bitmap, 200, path);
            }
            // 返回的是手写文件
            else if (requestCode == 5) {
                extras = data.getExtras();
                String path = extras.getString("handwritePath");
                // 通过路径取出图片，放入bitmap中
                bitmap = BitmapFactory.decodeFile(path);
                // 插入绘图文件
                InsertBitmap(bitmap, 480, path);
            }
        }
    }

    //取相册照片功能用的函数-获取文件路径   content开头URI --> 文件绝对路径
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("AddActivityTest", "handleImageOnKitKat  uri="+uri.toString());
        if(DocumentsContract.isDocumentUri(this,uri)){
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                Toast.makeText(this,"A",Toast.LENGTH_SHORT).show();
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
                Toast.makeText(this,"B",Toast.LENGTH_SHORT).show();
            }
        } else if("content".equalsIgnoreCase(uri.getScheme())){
            //如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri,null);
            Toast.makeText(this,"C",Toast.LENGTH_SHORT).show();
        } else if("file".equalsIgnoreCase(uri.getScheme())){
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
            Toast.makeText(this,"D",Toast.LENGTH_SHORT).show();
        }
        Log.d("AddActivityTest", "handleImageOnKitKat  imagePath="+imagePath);
        return  imagePath;
    }
    public String getImagePath(Uri uri, String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    ///10、将图片等比例缩放到合适的大小并添加在EditText中
    void InsertBitmap(Bitmap bitmap, int S, String imgPath) {
        uploadFile(imgPath);
        bitmap = resize(bitmap, S);
        // 添加边框效果
        // bitmap = getBitmapHuaSeBianKuang(bitmap);
        // bitmap = addBigFrame(bitmap,R.drawable.line_age);
        final ImageSpan imageSpan = new ImageSpan(this, bitmap);
        SpannableString spannableString = new SpannableString(imgPath);
        Log.d("AddActivityTest", "InsertBitmap: spannableStringLen="+spannableString.length());
        spannableString.setSpan(imageSpan, 0, spannableString.length(),
                SpannableString.SPAN_MARK_MARK);
        // 光标移到下一行
        //et_Notes.append("a");
        Editable editable = et_Notes.getEditableText();
        int selectionIndex = et_Notes.getSelectionStart();
        spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
        // 将图片添加进EditText中
        editable.insert(selectionIndex, spannableString);
        // 添加图片后自动空出两行
        et_Notes.append("\n");
        // 用List记录该录音的位置及所在路径，用于单击事件
        Map<String, String> map = new HashMap<String, String>();
        map.put("location", selectionIndex + "-"
                + (selectionIndex + spannableString.length()));
        map.put("path", imgPath);
        imgList.add(map);
        Log.d("AddActivityTest", "InsertBitmap  path="+imgPath);
    }

    ///11、等比例缩放图片
    private Bitmap resize(Bitmap bitmap, int S) {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        double partion = imgWidth * 1.0 / imgHeight;
        double sqrtLength = Math.sqrt(partion * partion + 1);
        // 新的缩略图大小
        double newImgW = S * (partion / sqrtLength);
        double newImgH = S * (1 / sqrtLength);
        float scaleW = (float) (newImgW / imgWidth);
        float scaleH = (float) (newImgH / imgHeight);
        Matrix mx = new Matrix();
        // 对原图片进行缩放
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx,
                true);
        return bitmap;
    }

    ///12、给图片加边框，并返回边框后的图片
    public Bitmap getBitmapHuaSeBianKuang(Bitmap bitmap) {
        float frameSize = 0.2f;
        Matrix matrix = new Matrix();

        // 用来做底图
        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 设置底图为画布
        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        float scale_x = (bitmap.getWidth() - 2 * frameSize - 2) * 1f
                / (bitmap.getWidth());
        float scale_y = (bitmap.getHeight() - 2 * frameSize - 2) * 1f
                / (bitmap.getHeight());
        matrix.reset();
        matrix.postScale(scale_x, scale_y);
        // 对相片大小处理(减去边框的大小)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Style.FILL);

        // 绘制底图边框
        canvas.drawRect(
                new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()),
                paint);

        // 绘制灰色边框
        paint.setColor(Color.GRAY);
        canvas.drawRect(
                new Rect((int) (frameSize), (int) (frameSize), bitmapbg
                        .getWidth() - (int) (frameSize), bitmapbg.getHeight()
                        - (int) (frameSize)), paint);

        canvas.drawBitmap(bitmap, frameSize + 1, frameSize + 1, paint);
        return bitmapbg;
    }

}
