package com.team.weup.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Set;

/**
 * @author xieziwei99
 * 2020-05-09
 */
public class User {

    private Long id;

    private String username;

    private String password;

    /**
     * 用户头像，存储图片所在路径，而非二进制图片
     */
    private String profilePhoto;

    /**
     * 今日步数
     */
    private Integer stepCount;

    private Integer progress;

    // 删除 user 时，对应记录都被删除
    @JsonIgnore
    private Set<WordPersonal> wordRecords;

    // 删除用户，删除其所有笔记
    @JsonIgnore
    private List<Note> noteList;

    // 删除用户，删除其所有代办
    @JsonIgnore
    private List<TodoItem> todoItemList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public Set<WordPersonal> getWordRecords() {
        return wordRecords;
    }

    public void setWordRecords(Set<WordPersonal> wordRecords) {
        this.wordRecords = wordRecords;
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    public List<TodoItem> getTodoItemList() {
        return todoItemList;
    }

    public void setTodoItemList(List<TodoItem> todoItemList) {
        this.todoItemList = todoItemList;
    }
}
