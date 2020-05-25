package com.team.weup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TopFragment extends Fragment implements View.OnClickListener {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.top_muen, container, false);
        LinearLayout linearLayout = view.findViewById(R.id.top_return);
        linearLayout.setOnClickListener(this);
        return view;
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.top_return: {
                getActivity().finish();
                break;
            }
            default:
                break;
        }
    }
}
