package com.team.weup;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.team.weup.model.User;
import com.team.weup.repo.UserInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SENSOR_SERVICE;

public class SportsFragment extends Fragment {
    private static final String TAG = "网络请求";
    private TextView stepCountTextView;
    private TextView targetTodayTextView;

    /** 今日步数 */
    private int stepCount = 0;
    private int targetToday = 1000;

    private SensorManager manager = null;
    private Sensor stepCounterSensor = null;

    private static final String PREFERENCE_NAME = "stepCountRecord";
    private static final int MODE = MODE_PRIVATE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sports, container, false);
        stepCountTextView = view.findViewById(R.id.textView_stepCount);
        targetTodayTextView = view.findViewById(R.id.textView_targetToday);
        TextView rankTextView = view.findViewById(R.id.textView_rank);
        ImageButton rankImageButton = view.findViewById(R.id.imageButton_rank);

        manager = (SensorManager) Objects.requireNonNull(getContext()).getSystemService(SENSOR_SERVICE);
        assert manager != null;
        stepCounterSensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        targetTodayTextView.setOnClickListener(this::setTargetToday);
        rankTextView.setOnClickListener(this::gotoRankPage);
        rankImageButton.setOnClickListener(this::gotoRankPage);

        updateDataInUI();

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
    public void onResume() {
        super.onResume();
        manager.registerListener(myListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        manager.unregisterListener(myListener);
    }

    private void updateDataInUI() {
        stepCountTextView.setText(String.valueOf(stepCount));
        targetTodayTextView.setText(String.format(Locale.getDefault(), "%s%s", getResources().getString(R.string.今日目标), targetToday));
    }

    private void setTargetToday(View view) {
        EditText editText = new EditText(getContext());
        editText.setText(String.valueOf(targetToday));
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        new AlertDialog.Builder(getContext())
                .setTitle("设定目标")
                .setView(editText)
                .setPositiveButton("确定", (dialog, which) -> {
                    targetToday = Integer.parseInt(editText.getText().toString().trim());
                    updateDataInUI();
                })
                .create()
                .show();
    }

    private final SensorEventListener myListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            int totalStep = ((int) event.values[0]);

            SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(PREFERENCE_NAME, MODE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            int todayStepRecorded = sharedPreferences.getInt(today(), 0);   // 今日已经记录的步数
            int milestoneStep = sharedPreferences.getInt("lastRecord", totalStep);
            if (totalStep < milestoneStep) {    // 如果重启后走了很多步，使得 totalStep > 上次记录，怎么办
                milestoneStep = totalStep;
                editor.putInt("lastRecord", milestoneStep);
                editor.apply();
            }
            if (0 == todayStepRecorded) {
                editor.putInt(today(), 1);
                editor.apply();
            } else {
                /*
                可以记录今天打开APP开始的步数
                在今天打开了APP之后，如果重启了，步数也不会重置，因为已经记录下了今天的步数

                待测试：第二天中午打开APP，上午的步数可以记录吗，好像可以
                 */
                int additionStep = totalStep - milestoneStep;
                stepCount = todayStepRecorded + additionStep;
                editor.putInt(today(), stepCount);
                milestoneStep = totalStep;
                editor.putInt("lastRecord", milestoneStep);
                editor.apply();
            }
            updateDataInUI();

            long userId = 1L;
            User updateUser = new User();
            updateUser.setStepCount(stepCount);
            NetworkUtil.getRetrofit().create(UserInterface.class)
                    .updateUser(userId, updateUser)
                    .enqueue(new Callback<ReturnVO<User>>() {
                        @Override
                        @EverythingIsNonNull
                        public void onResponse(Call<ReturnVO<User>> call, Response<ReturnVO<User>> response) {
                            ReturnVO<User> body = response.body();
                            assert body != null;
                            if (ReturnVO.ERROR.equals(body.getCode())) {
                                Log.e(TAG, "onResponse: ", new Exception("网络请求异常"));
                            }
                        }

                        @Override
                        @EverythingIsNonNull
                        public void onFailure(Call<ReturnVO<User>> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void gotoRankPage(View view) {
        Intent intent = new Intent(getContext(), RankActivity.class);
        startActivity(intent);
    }

    private String today() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }
}
