package com.team.weup.repo;

import com.team.weup.model.User;
import com.team.weup.util.ReturnVO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserInterface {

    @POST("/user")
    Call<ReturnVO<User>> addUser(@Body User user);

    @PUT("/user/{id}")
    Call<ReturnVO<User>> updateUser(@Path("id") Long id, @Body User user);
}
