package com.team.weup.repo;

import com.team.weup.model.Note;
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

public interface NoteInterface {

    @POST("/note")
    Call<ReturnVO<Note>> addNoteOfUser(@Query("userId") Long id, @Body Note note);

    @GET("/notes")
    Call<ReturnVO<List<Note>>> getNotesByUserId(@Query("userId") Long id);

    @DELETE("/note/{id}")
    Call<ReturnVO<Note>> deleteNoteOfUser(@Path("id") Long id);

    @PUT("/note/{id}")
    Call<ReturnVO<Note>> updateNote(@Path("id") Long id, @Body Note note);
}
