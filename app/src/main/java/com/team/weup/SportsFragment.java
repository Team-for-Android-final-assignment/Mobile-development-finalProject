package com.team.weup;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.team.weup.repo.UserInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static android.app.Activity.RESULT_OK;

public class SportsFragment extends Fragment {
    // 常量
    private final int ACTIVITY_CHOOSE_FILE = 100;
    private final String TAG = "网络请求";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sports, container, false);
        Button uploadButton = view.findViewById(R.id.button_upload);

        uploadButton.setOnClickListener(this::upload);

        return view;
    }

    private void upload(View view) {
        //Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        Intent chooseFile = new Intent(Intent.ACTION_PICK);
        chooseFile.setType("image/*");
        Intent intent = Intent.createChooser(chooseFile, "选择图片");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ACTIVITY_CHOOSE_FILE) {
            if (resultCode == RESULT_OK) {
                assert data != null;

                String realPathFromURI = handleImageOnKitKat(data);
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
                                    Log.i(TAG, "onResponse: 文件名为" + body.getData());
                                } else {
                                    Log.e(TAG, "onResponse: " + body.getMessage(), new Exception("上传文件出错"));
                                }
                            }

                            @Override
                            @EverythingIsNonNull
                            public void onFailure(Call<ReturnVO<String>> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
            }
        }
    }

    //取相册照片功能用的函数-获取文件路径   content开头URI --> 文件绝对路径
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        assert uri != null;
        Log.d("AddActivityTest", "handleImageOnKitKat  uri=" + uri.toString());
        if (DocumentsContract.isDocumentUri(getContext(), uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                Toast.makeText(getContext(), "A", Toast.LENGTH_SHORT).show();
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(docId));
                imagePath = getImagePath(contentUri, null);
                Toast.makeText(getContext(), "B", Toast.LENGTH_SHORT).show();
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
            Toast.makeText(getContext(), "C", Toast.LENGTH_SHORT).show();
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
            Toast.makeText(getContext(), "D", Toast.LENGTH_SHORT).show();
        }
        Log.d("AddActivityTest", "handleImageOnKitKat  imagePath=" + imagePath);
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = Objects.requireNonNull(getContext()).getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
