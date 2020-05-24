package com.team.weup;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginInActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            Toast.makeText(this, "请输入帐号", Toast.LENGTH_SHORT).show();
        }
        //未输入密码
        else if (TextUtils.isEmpty(password) || "".equals(password)) {
            password_et.requestFocus();
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
        }
        //帐号密码不匹配
        else if (false) {
            password_et.setText("");
            password_et.requestFocus();
            Toast.makeText(this, "密码错误，请检查", Toast.LENGTH_SHORT).show();
        }
        //一切正确，则登录
        else {
            SystemStatus.setLogin(true);
            SystemStatus.setNow_accounts(account);
            //SystemStatus.setNow_name();
            //SystemStatus.setUserhead();
            SystemStatus.SaveSetting(this);

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }

}
