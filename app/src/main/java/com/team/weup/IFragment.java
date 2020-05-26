package com.team.weup;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.team.weup.model.User;
import com.team.weup.repo.UserInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class IFragment extends Fragment {
    //UI控件
    private ImageView imageView = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_i, container, false);

        //根据登陆者信息,设置UI
        TextView nameText = view.findViewById(R.id.i_main_h1);
        TextView accoText = view.findViewById(R.id.i_main_h2);
        imageView = view.findViewById(R.id.head_img);
        if (SystemStatus.getNow_name() != null)
            nameText.setText(SystemStatus.getNow_name());
        if (SystemStatus.getNow_account() != null)
            accoText.setText(SystemStatus.getNow_account());
        if (SystemStatus.getUserhead() != null) {
            imageView.setImageBitmap(SystemStatus.getUserhead());
        }

        //设置子元素的点击监听器
        LinearLayout upload_head = view.findViewById(R.id.upload_head);
        LinearLayout update_password = view.findViewById(R.id.update_password);
        LinearLayout login_out = view.findViewById(R.id.login_out);

        upload_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadHead();
            }
        });

        update_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdatePassword();
            }
        });

        login_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginOut();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadHead();
            }
        });


        return view;

    }

    //跳转至相关页面
    private void UploadHead() {
        Intent intent = new Intent();
        //开启Pictures画面Type设定为image
        intent.setType("image/*");
        //选择相片
        //也可使用Intent.ACTION_GET_CONTENT
        intent.setAction(Intent.ACTION_PICK);
        // 取得相片后返回本画面
        startActivityForResult(intent, 1);
    }

    private void UpdatePassword() {
        startActivity(new Intent(getActivity(), UpdatePasswordActivity.class));
    }

    private void LoginOut() {
        SystemStatus.setNow_account(null);
        SystemStatus.setNow_name(null);
        SystemStatus.setUserhead(null);
        SystemStatus.setLogin(false);
        assert getActivity() != null;
        SystemStatus.SaveSetting(getActivity());
        Toast.makeText(getContext(), "退出成功", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getActivity(), LoginInActivity.class));
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            try {
                //获取图片
                Uri uri = data.getData();
                assert uri != null;
                ContentResolver cr = getActivity().getContentResolver();
                Bitmap source = BitmapFactory.decodeStream(cr.openInputStream(uri));

                //裁剪成正方形
                Bitmap bitmap = compressImg(source);
                // 将图片设定到ImageView
                imageView.setImageBitmap(bitmap);

                //系统设置
                SystemStatus.setUserhead(bitmap);
                //将设置保存至本地
                SystemStatus.SaveSetting(getActivity());

                String[] filename = Uri2Path(uri).split("/");
                String[] filename1 = filename[filename.length - 1].split("\\.");
                File file = Bitmap2File(bitmap, filename1[0]);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part bodyFile = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                //上传至云数据库端
                NetworkUtil.getRetrofit().create(UserInterface.class)
                        .upload(bodyFile)
                        .enqueue(new Callback<ReturnVO<String>>() {
                            @Override
                            @EverythingIsNonNull
                            public void onResponse(Call<ReturnVO<String>> call, Response<ReturnVO<String>> response) {
                                ReturnVO<String> body = response.body();
                                assert body != null;
                                if (body.getCode().equals(ReturnVO.OK)) {
                                    setHead2Cloud(body.getData());
                                } else {
                                    Toast.makeText(getContext(), "头像保存失败", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            @EverythingIsNonNull
                            public void onFailure(Call<ReturnVO<String>> call, Throwable t) {
                                Toast.makeText(getContext(), "头像保存失败", Toast.LENGTH_SHORT).show();
                                t.printStackTrace();
                            }
                        });
            } catch (Exception e) {
                Toast.makeText(getContext(), "头像设置失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Bitmap转file
    private File Bitmap2File(Bitmap bitmap, String filename) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        File file = new File(Environment.getExternalStorageDirectory(), filename + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //将图片调整成正方形，边长不超过300，压缩大小不超过5MB
    private Bitmap compressImg(Bitmap source) {
        //裁剪成正方形
        int side_length = Math.min(source.getWidth(), source.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(source, (source.getWidth() - side_length) / 2, (source.getHeight() - side_length) / 2, side_length, side_length);

        //调整Bitmap大小
        int max_size = 1000;
        if (side_length > max_size) {
            bitmap = Bitmap.createScaledBitmap(bitmap, max_size, max_size, false);
        }

        //压缩大小
        int max_mb = 5;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.reset();
        int options = 100;
        while (options > 0 && bitmap.getByteCount() > 1024 * 1024 * max_mb) {
            options -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()));
            baos.reset();
        }

        return bitmap;
    }

    //uri转化成路径
    private String Uri2Path(Uri uri) {
        assert uri != null;
        String imagePath = null;

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
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }

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

    //将新的头像信息设置到云服务器上
    private void setHead2Cloud(String fileName) {
        //读取用户信息
        NetworkUtil.getRetrofit().create(UserInterface.class)
                .getUser(Long.parseLong(SystemStatus.getNow_account()))
                .enqueue(new Callback<ReturnVO<User>>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<ReturnVO<User>> call, Response<ReturnVO<User>> response) {
                        ReturnVO<User> body = response.body();
                        assert body != null;
                        if (body.getCode().equals(ReturnVO.OK)) {
                            User user = body.getData();
                            user.setProfilePhoto(fileName);
                            //将新的头像信息设置到云服务器上
                            NetworkUtil.getRetrofit().create(UserInterface.class)
                                    .updateUser(Long.parseLong(SystemStatus.getNow_account()), user)
                                    .enqueue(new Callback<ReturnVO<User>>() {
                                        @Override
                                        @EverythingIsNonNull
                                        public void onResponse(Call<ReturnVO<User>> call, Response<ReturnVO<User>> response) {
                                            ReturnVO<User> body = response.body();
                                            assert body != null;
                                            //修改失败
                                            if (!body.getCode().equals(ReturnVO.OK))
                                                Toast.makeText(getActivity(), "头像保存失败", Toast.LENGTH_SHORT).show();

                                        }

                                        //修改失败
                                        @Override
                                        @EverythingIsNonNull
                                        public void onFailure(Call<ReturnVO<User>> call, Throwable t) {
                                            Toast.makeText(getActivity(), "头像保存失败", Toast.LENGTH_SHORT).show();
                                            t.printStackTrace();
                                        }
                                    });

                        } else {
                            Toast.makeText(getActivity(), "头像保存失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //修改失败
                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<ReturnVO<User>> call, Throwable t) {
                        Toast.makeText(getActivity(), "头像保存失败", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }
}
