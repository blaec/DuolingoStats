package com.blaec.points.model;


public class Params {
    private final String language;
    private final String link;

    private Params(String language, String link) {
        this.language = language;
        this.link = link;
    }

    public static Params create(String link, String lang) {
        return new Params(lang, link);
    }

    public String getLanguage() {
        return language;
    }

    public String getLink() {
        return link;
    }
}
