package com.team.weup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.team.weup.model.WordPersonal;
import com.team.weup.repo.WordInterface;
import com.team.weup.util.NetworkUtil;
import com.team.weup.util.ReturnVO;

import org.w3c.dom.Text;

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ReviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //锁屏设置按钮
    private CardView setting;
    //错题本按钮
    private CardView wrongBook;
    //笔记本按钮
    private CardView noteBook;

    private TextView mode;
    private TextView difficulty;

    private TextView wrongNum;
    private TextView correctNum;


    public static Set<WordPersonal> wordPersonal;

    public ReviewFragment() {
        // Required empty public constructor
    }

    public static ReviewFragment newInstance(String param1, String param2) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.review_fragment, container, false);
        sharedPreferences = getActivity().getSharedPreferences("share",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        mode=(TextView)view.findViewById(R.id.mode);
        difficulty = (TextView)view.findViewById(R.id.difficulty);

        setting = (CardView)view.findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),SettingActivity.class));
            }
        });

        wrongBook = (CardView)view.findViewById(R.id.wrongBook);
        wrongBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),WrongActivity.class));
            }
        });

        wrongNum = (TextView)view.findViewById(R.id.wrongNum);
        correctNum = (TextView)view.findViewById(R.id.correctNum);
        //mode.setText(sharedPreferences.getString("mode","关闭"));
        //setStudyData();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart(){
        super.onStart();
        setStudyData();
    }

//    @Override
//    public void onPause(){
//        super.onPause();
//        setStudyData();
//    }
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        mode.setText(intent.getExtras().getString("mode"));
    }
};

    @Override
    public void onDestroy(){
        super.onDestroy();
        //getActivity().unregisterReceiver(broadcastReceiver);
    }

    //此处编写显示数据的逻辑,还有难度和开关模式
    private void setStudyData(){
        difficulty.setText(sharedPreferences.getString("difficulty","四级")+"英语");
        mode.setText(sharedPreferences.getString("mode","关闭"));
        //读取wordPersonal数据中，wordstatus=0和1的条目数，并根据单词的id获取单词，把单词写到本地
        NetworkUtil.getRetrofit().create(WordInterface.class)
                .getWordPersonalSetByUserId((long)Integer.parseInt(SystemStatus.getNow_account()))
                .enqueue(new Callback<ReturnVO<Set<WordPersonal>>>() {
                    @Override
                    public void onResponse(Call<ReturnVO<Set<WordPersonal>>> call, Response<ReturnVO<Set<WordPersonal>>> response) {
                        int wrongcount=0;
                        int correctcount=0;
                        ReturnVO<Set<WordPersonal>> body = response.body();
                        wordPersonal = body.getData();
                        if(body.getData()!=null){
                            for(WordPersonal w:wordPersonal){
                                if(w.getWordStatus()==1){
                                    wrongcount++;
                                }
                                else{
                                    correctcount++;
                                }
                            }
                            wrongNum.setText(String.valueOf(wrongcount));
                            correctNum.setText(String.valueOf(correctcount));
                        }
                        else{
                            wrongNum.setText(String.valueOf(0));
                            correctNum.setText(String.valueOf(0));
                        }
                    }

                    @Override
                    public void onFailure(Call<ReturnVO<Set<WordPersonal>>> call, Throwable t) {

                    }
                });
    }
}
