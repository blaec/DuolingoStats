package com.blaec.points.model;


public class Lesson {
    private final String language;
    private final String link;

    private Lesson(String language, String link) {
        this.language = language;
        this.link = link;
    }

    public static Lesson create(String link, String lang) {
        return new Lesson(lang, link);
    }

    public String getLanguage() {
        return language;
    }

    public String getLink() {
        return link;
    }
}
