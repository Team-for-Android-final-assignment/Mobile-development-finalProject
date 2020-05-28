package com.team.weup;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.team.weup.model.User;
import com.team.weup.repo.UserInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class RankActivity extends AppCompatActivity {
    private static final String TAG = "网络请求";
    ListView rankListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        rankListView = findViewById(R.id.listView_rank);

        getTopUserListAndUpdateUI();
    }

    public void updateUI(List<User> stepCountTopUserList) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < stepCountTopUserList.size(); i++) {
            User user = stepCountTopUserList.get(i);
            String s = i + 1 + "   " + user.getUsername() + " -------------- " + user.getStepCount() + "步";
            items.add(s);
        }
        rankListView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
    }

    public void getTopUserListAndUpdateUI() {
        NetworkUtil.getRetrofit().create(UserInterface.class)
                .getTopListByStepCount()
                .enqueue(new Callback<ReturnVO<List<User>>>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<ReturnVO<List<User>>> call, Response<ReturnVO<List<User>>> response) {
                        ReturnVO<List<User>> body = response.body();
                        assert body != null;
                        if (ReturnVO.OK.equals(body.getCode())) {
                            List<User> data = body.getData();
                            updateUI(data);
                        } else {
                            Log.e(TAG, "onResponse: ", new Exception("网络请求异常"));
                        }
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<ReturnVO<List<User>>> call, Throwable t) {

                    }
                });
    }
}
