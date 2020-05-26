package com.team.weup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class BottomFragment extends Fragment implements View.OnClickListener {
    //四个子LinearLayout
    private LinearLayout life;
    private LinearLayout study;
    private LinearLayout sport;
    private LinearLayout i;

    //调用主页Activity方法
    private HomeActivity homeActivity;

    //四个子元素的img和text
    private static ImageView[] imageArray = new ImageView[4];
    private static TextView[] textArray = new TextView[4];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_muen, container, false);

        //获取Activity方法
        homeActivity = (HomeActivity) getActivity();

        //获取LinearLayout
        life = view.findViewById(R.id.bottom_life);
        study = view.findViewById(R.id.bottom_study);
        sport = view.findViewById(R.id.bottom_sport);
        i = view.findViewById(R.id.bottom_i);

        //获取img和text
        imageArray[0] = view.findViewById(R.id.bottom_life_img);
        textArray[0] = view.findViewById(R.id.bottom_life_text);

        imageArray[1] = view.findViewById(R.id.bottom_study_img);
        textArray[1] = view.findViewById(R.id.bottom_study_text);

        imageArray[2] = view.findViewById(R.id.bottom_sport_img);
        textArray[2] = view.findViewById(R.id.bottom_sport_text);

        imageArray[3] = view.findViewById(R.id.bottom_i_img);
        textArray[3] = view.findViewById(R.id.bottom_i_text);

        //调整元素颜色
        UIMotify(0, homeActivity);

        setClickListener();
        return view;
    }

    //配置监听器
    public void setClickListener() {
        life.setOnClickListener(this);
        study.setOnClickListener(this);
        sport.setOnClickListener(this);
        i.setOnClickListener(this);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        TextView textView;
        ImageView imageView;

        switch (v.getId()) {
            case R.id.bottom_life: {
                homeActivity.changePager(0);
                break;
            }
            case R.id.bottom_study: {
                homeActivity.changePager(1);
                break;
            }
            case R.id.bottom_sport: {
                homeActivity.changePager(2);
                break;
            }
            case R.id.bottom_i: {
                homeActivity.changePager(3);
                break;
            }
            default:
                break;
        }
    }

    //UI更新
    public void UIMotify(int flag, Context context) {
        try {
            for (int i = 0; i < 4; i++) {
                if (i == flag) {
                    if (i == 3)
                        imageArray[i].setImageDrawable(context.getDrawable(R.drawable.svg_ic_i_in));
                    else
                        imageArray[i].setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.qidao_blue)));
                    textArray[i].setTextColor(context.getColor(R.color.qidao_blue));
                } else {
                    if (i == 3)
                        imageArray[i].setImageDrawable(context.getDrawable(R.drawable.svg_ic_i_out));
                    else
                        imageArray[i].setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_grey)));
                    textArray[i].setTextColor(context.getColor(R.color.text_grey));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
