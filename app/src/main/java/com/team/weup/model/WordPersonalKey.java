package com.team.weup.model;

import java.io.Serializable;

/**
 * @author xieziwei99
 * 2020-05-09
 */
public class WordPersonalKey implements Serializable {

    private Long userId;

    private Long wordId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WordPersonalKey that = (WordPersonalKey) o;

        if (!userId.equals(that.userId)) {
            return false;
        }
        return wordId.equals(that.wordId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + wordId.hashCode();
        return result;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }
}
