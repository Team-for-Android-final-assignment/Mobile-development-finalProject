package com.team.weup;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.team.weup.model.Note;
import com.team.weup.repo.NoteInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class MainActivity extends AppCompatActivity {
    TextView helloTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTextView = findViewById(R.id.textView_hello);

        // 演示API调用
        NetworkUtil.getRetrofit().create(NoteInterface.class)
                .getNotesByUserId(1L)
                .enqueue(new Callback<ReturnVO<List<Note>>>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<ReturnVO<List<Note>>> call, Response<ReturnVO<List<Note>>> response) {
                        ReturnVO<List<Note>> body = response.body();
                        assert body != null;
                        helloTextView.setText(body.toString());
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<ReturnVO<List<Note>>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

    }
}
