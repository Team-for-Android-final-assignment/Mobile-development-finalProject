package com.team.weup;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.team.weup.model.User;
import com.team.weup.repo.UserInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.internal.EverythingIsNonNull;

public class LoginInActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(SystemStatus.isLogin()){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
        setContentView(R.layout.login_in_layout);

        //登记监听器
        Button button = findViewById(R.id.login_in_bt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginIn();
            }
        });

    }

    //处理登录事件
    private void LoginIn() {
        //帐号
        EditText account_et = findViewById(R.id.login_in_account_et);
        String account = account_et.getText().toString();
        //密码
        EditText password_et = findViewById(R.id.login_in_password_et);
        String password = password_et.getText().toString();

        //未输入帐号
        if (TextUtils.isEmpty(account) || "".equals(account)) {
            account_et.requestFocus();
            Toast.makeText(this, "请输入学号", Toast.LENGTH_SHORT).show();
        }
        //未输入密码
        else if (TextUtils.isEmpty(password) || "".equals(password)) {
            password_et.requestFocus();
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
        } else {
            //查询帐号
            NetworkUtil.getRetrofit().create(UserInterface.class)
                    .getUser(Long.parseLong(account))
                    .enqueue(new Callback<ReturnVO<User>>() {
                        @Override
                        @EverythingIsNonNull
                        public void onResponse(Call<ReturnVO<User>> call, Response<ReturnVO<User>> response) {
                            ReturnVO<User> body = response.body();
                            assert body != null;
                            if (body.getCode().equals(ReturnVO.OK)) {
                                User loginuser = body.getData();

                                //帐号密码不匹配
                                if (!loginuser.getPassword().equals(password)) {
                                    password_et.setText("");
                                    password_et.requestFocus();
                                    Toast.makeText(LoginInActivity.this, "密码错误，请检查", Toast.LENGTH_SHORT).show();
                                }
                                //一切正确，则登录
                                else {
                                    SystemStatus.setLogin(true);
                                    SystemStatus.setNow_account(account);
                                    SystemStatus.setNow_name(loginuser.getUsername());
                                    if (loginuser.getProfilePhoto() != null) {
                                        try {
                                            Bitmap source = SystemStatus.downloadHeadFromCloud(loginuser.getProfilePhoto());
                                            SystemStatus.setUserhead(source);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else SystemStatus.setUserhead(null);
                                    SystemStatus.SaveSetting(LoginInActivity.this);

                                    startActivity(new Intent(LoginInActivity.this, HomeActivity.class));
                                    finish();
                                }
                            } else {
                                //帐号不存在
                                account_et.setText("");
                                password_et.setText("");
                                account_et.requestFocus();
                                Toast.makeText(LoginInActivity.this, "用户不存在，请检查", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        @EverythingIsNonNull
                        public void onFailure(Call<ReturnVO<User>> call, Throwable t) {
                            Toast.makeText(LoginInActivity.this, "登录失败，请检查手机环境", Toast.LENGTH_SHORT).show();
                            t.printStackTrace();
                        }
                    });
        }
    }

}
