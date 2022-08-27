package com.blaec.points.model;


public class Story {
    private final String language;
    private final String link;

    private Story(String language, String link) {
        this.language = language;
        this.link = link;
    }

    public static Story create(String link, String lang) {
        return new Story(lang, link);
    }

    public String getLanguage() {
        return language;
    }

    public String getLink() {
        return link;
    }
}
