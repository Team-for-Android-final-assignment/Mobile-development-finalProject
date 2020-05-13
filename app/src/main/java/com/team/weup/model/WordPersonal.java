package com.team.weup.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Objects;

/**
 * @author xieziwei99
 * 2020-05-09
 */
public class WordPersonal {

    private WordPersonalKey id;

    // 删除记录时，不删除对应 user
    private User user;

    // 删除记录时，不删除对应 word
    private Word word;

    private Integer wordStatus;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WordPersonal that = (WordPersonal) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public WordPersonalKey getId() {
        return id;
    }

    public void setId(WordPersonalKey id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public Integer getWordStatus() {
        return wordStatus;
    }

    public void setWordStatus(Integer wordStatus) {
        this.wordStatus = wordStatus;
    }
}
