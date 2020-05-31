package com.team.weup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.team.weup.model.WordPersonal;
import com.team.weup.repo.WordInterface;
import com.team.weup.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

public class WrongActivity extends AppCompatActivity {

    private ListView wrongListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrong);
        wrongListView = (ListView)findViewById(R.id.listView_wrong);
        getWrongWordandUpdateUI();
    }

    private void getWrongWordandUpdateUI(){
        //根据
        List<String> items = new ArrayList<String>();
        int index=1;
        for(WordPersonal w:ReviewFragment.wordPersonal){
            //0是对题，1是错题
            if(w.getWordStatus()==1){
                items.add(index+" "+w.getWord().getEnglish()+"  "+w.getWord().getChinese());
                index++;
            }
        }
        wrongListView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,items));
    }
}
