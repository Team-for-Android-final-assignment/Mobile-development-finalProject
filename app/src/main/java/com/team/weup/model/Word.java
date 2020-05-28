package com.team.weup.model;

/**
 * 单词
 *
 * @author xieziwei99
 * 2020-05-09
 */
public class Word {

    private Long id;

    private String english;

    private String chinese;

    private String yinbiao;

    private String option1;

    private String option2;

    private String option3;

    private String option4;

    public Word(Word word){
        this.id = word.getId();
        this.english = word.getEnglish();
        this.chinese = word.getChinese();
        this.yinbiao = word.getYinbiao();
        this.option1 = word.getOption1();
        this.option2 = word.getOption2();
        this.option3 = word.getOption3();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getChinese() {
        return chinese;
    }

    public void setChinese(String chinese) {
        this.chinese = chinese;
    }

    public String getYinbiao() {
        return yinbiao;
    }

    public void setYinbiao(String yinbiao) {
        this.yinbiao = yinbiao;
    }

    public String getOption1() {
        return option1;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public String getOption2() {
        return option2;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public String getOption3() {
        return option3;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public String getOption4() {
        return option4;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }
}
