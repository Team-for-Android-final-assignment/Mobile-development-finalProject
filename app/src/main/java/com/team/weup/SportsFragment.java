package com.team.weup;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.Objects;

import static android.content.Context.SENSOR_SERVICE;

public class SportsFragment extends Fragment {
    private TextView stepCountTextView;
    private TextView targetTodayTextView;

    private int stepCount = 0;
    private int targetToday = 1000;

    private SensorManager manager = null;
    private Sensor stepCounterSensor = null;

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
            stepCount = ((int) event.values[0]);
            updateDataInUI();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void gotoRankPage(View view) {
        Intent intent = new Intent(getContext(), RankActivity.class);
        startActivity(intent);
    }
}
