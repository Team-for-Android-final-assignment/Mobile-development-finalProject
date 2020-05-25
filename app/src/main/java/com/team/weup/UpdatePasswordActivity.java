package com.team.weup;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class UpdatePasswordActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_password_layout);

        //修改密码按键交互
        Button button = findViewById(R.id.con_update_password_bt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateOp();
            }
        });
    }

    //修改密码逻辑和操作
    private void UpdateOp() {
        //旧密码
        EditText old_password_et = findViewById(R.id.old_password_et);
        String old_password = old_password_et.getText().toString();
        //新密码
        EditText new_password_et = findViewById(R.id.new_password_et);
        String new_password = new_password_et.getText().toString();
        //再次确认密码
        EditText con_password_et = findViewById(R.id.con_password_et);
        String con_password = con_password_et.getText().toString();

        //未输入旧密码
        if (TextUtils.isEmpty(old_password) || "".equals(old_password)) {
            old_password_et.requestFocus();
            Toast.makeText(this, "请输入旧密码", Toast.LENGTH_SHORT).show();
        }
        //未输入新密码
        else if (TextUtils.isEmpty(new_password) || "".equals(new_password)) {
            new_password_et.requestFocus();
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
        }
        //未输入确认密码
        else if (TextUtils.isEmpty(con_password) || "".equals(con_password)) {
            con_password_et.requestFocus();
            Toast.makeText(this, "请输入确认密码", Toast.LENGTH_SHORT).show();
        }
        //新密码与确认密码不同
        else if (!new_password.equals(con_password)) {
            con_password_et.setText("");
            con_password_et.requestFocus();
            Toast.makeText(this, "确认密码与新密码不同，请检查", Toast.LENGTH_SHORT).show();
        } else {
            //查询帐号,获取账号密码
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
                                if (old_password.equals(user.getPassword())) {
                                    //若新密码和旧密码相同
                                    if (new_password.equals(old_password)) {
                                        new_password_et.setText("");
                                        con_password_et.setText("");
                                        new_password_et.requestFocus();
                                        Toast.makeText(UpdatePasswordActivity.this, "新密码不能与旧密码相同，请重新输入", Toast.LENGTH_SHORT).show();
                                    }
                                    //一切正确，则修改密码
                                    else {
                                        user.setPassword(new_password);
                                        NetworkUtil.getRetrofit().create(UserInterface.class)
                                                .updateUser(Long.parseLong(SystemStatus.getNow_account()), user)
                                                .enqueue(new Callback<ReturnVO<User>>() {
                                                    //修改成功
                                                    @Override
                                                    @EverythingIsNonNull
                                                    public void onResponse(Call<ReturnVO<User>> call, Response<ReturnVO<User>> response) {
                                                        ReturnVO<User> body = response.body();
                                                        assert body != null;
                                                        //修改成功
                                                        if (body.getCode().equals(ReturnVO.OK)) {
                                                            new_password_et.setText("");
                                                            old_password_et.setText("");
                                                            con_password_et.setText("");
                                                            old_password_et.requestFocus();
                                                            Toast.makeText(UpdatePasswordActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                                                        }
                                                        //修改失败
                                                        else {
                                                            Toast.makeText(UpdatePasswordActivity.this, "密码修改失败，请检查手机环境", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }

                                                    //修改失败
                                                    @Override
                                                    @EverythingIsNonNull
                                                    public void onFailure(Call<ReturnVO<User>> call, Throwable t) {
                                                        Toast.makeText(UpdatePasswordActivity.this, "密码修改失败，请检查手机环境", Toast.LENGTH_SHORT).show();
                                                        t.printStackTrace();
                                                    }
                                                });
                                    }
                                } else {
                                    //旧密码不正确
                                    old_password_et.setText("");
                                    old_password_et.requestFocus();
                                    Toast.makeText(UpdatePasswordActivity.this, "旧密码输入有误，请检查", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        @EverythingIsNonNull
                        public void onFailure(Call<ReturnVO<User>> call, Throwable t) {
                            Toast.makeText(UpdatePasswordActivity.this, "密码修改失败，请检查手机环境", Toast.LENGTH_SHORT).show();
                            t.printStackTrace();
                        }
                    });
        }
    }
}
