package com.team.weup.repo;

import com.team.weup.model.Note;
import com.team.weup.util.ReturnVO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NoteInterface {

    @POST("/note")
    Call<ReturnVO<Note>> addNoteOfUser(@Query("userId") Long id, @Body Note note);

    @GET("/notes")
    Call<ReturnVO<List<Note>>> getNotesByUserId(@Query("userId") Long id);
}
