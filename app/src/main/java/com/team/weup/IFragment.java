package com.team.weup;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
        if (SystemStatus.getNow_accounts() != null)
            accoText.setText(SystemStatus.getNow_accounts());
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
        SystemStatus.setNow_accounts(null);
        SystemStatus.setNow_name(null);
        SystemStatus.setUserhead(null);
        SystemStatus.setLogin(false);
        SystemStatus.SaveSetting(getContext());
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
                ContentResolver cr = getActivity().getContentResolver();
                Bitmap source = BitmapFactory.decodeStream(cr.openInputStream(uri));

                //裁剪成正方形
                int side_length = Math.min(source.getWidth(), source.getHeight());
                Bitmap bitmap = Bitmap.createBitmap(source, (source.getWidth() - side_length) / 2, (source.getHeight() - side_length) / 2, side_length, side_length);


                //系统设置
                SystemStatus.setUserhead(bitmap);
                //将设置保存至本地
                SystemStatus.SaveSetting(getContext());

                // 将图片设定到ImageView
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Toast.makeText(getContext(), "头像设置失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
