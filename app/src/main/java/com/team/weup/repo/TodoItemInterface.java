package com.team.weup.repo;

import com.team.weup.model.TodoItem;
import com.team.weup.util.ReturnVO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TodoItemInterface {
    @POST("/todoItem")
    Call<ReturnVO<TodoItem>> addTodoItemOfUser(@Query("userId") Long id, @Body TodoItem todoItem);

    @DELETE("/todoItem/{id}")
    Call<ReturnVO<TodoItem>> deleteTodoItemById(@Path("id") Long id);

    @PUT("/todoItem/{id}")
    Call<ReturnVO<TodoItem>> updateTodoItemById(@Path("id") Long id, @Body TodoItem todoItem);

    @GET("/todoItems")
    Call<ReturnVO<List<TodoItem>>> getTodoItemsByUserId(@Query("userId") Long id);
}
