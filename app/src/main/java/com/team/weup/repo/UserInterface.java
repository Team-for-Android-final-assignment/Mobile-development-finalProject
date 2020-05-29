package com.team.weup.repo;

import com.team.weup.model.User;
import com.team.weup.util.ReturnVO;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserInterface {

    @POST("/user")
    Call<ReturnVO<User>> addUser(@Body User user);

    @PUT("/user/{id}")
    Call<ReturnVO<User>> updateUser(@Path("id") Long id, @Body User user);

    @Multipart
    @POST("upload")
    Call<ReturnVO<String>> upload(@Part MultipartBody.Part file);

    @GET("/user/{id}")
    Call<ReturnVO<User>> getUser(@Path("id") Long id);

    @GET("/user/{id}")
    Call<ReturnVO<User>> getUserById(@Path("id") Long id);

    @GET("users/getTopStepCountList")
    Call<ReturnVO<List<User>>> getTopListByStepCount();

}
