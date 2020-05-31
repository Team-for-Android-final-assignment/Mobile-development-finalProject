package com.team.weup.repo;

import com.team.weup.model.Word;
import com.team.weup.model.WordPersonal;
import com.team.weup.util.ReturnVO;

import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WordInterface {
    //返回的是个人的背单词情况
    //插入或者更新个人学习情况，0表示答对的单词，1表示答错的单词
    @POST("/userRememberWord")
    Call<ReturnVO<WordPersonal>> addUserWordRecord(@Body WordPersonal wp);

    //获取新单词
    @GET("/word/{id}")
    Call <ReturnVO<Word>> getWordById(@Path("id")Long id);

    @GET("/words")
    Call <ReturnVO<Set<WordPersonal>>> getWordPersonalSetByUserId(@Query("userId")Long userId);

}
