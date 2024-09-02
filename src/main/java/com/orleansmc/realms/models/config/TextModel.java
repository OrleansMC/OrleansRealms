package com.orleansmc.realms.models.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TextModel {
    public final String en;
    public final String tr;
    private final List<String[]> replacements = new ArrayList<String[]>();

    public TextModel(String en, String tr) {
        this.en = en;
        this.tr = tr;
    }

    public TextModel addReplacement(String from, String to) {
        replacements.add(new String[]{from, to});
        return this;
    }

    public String get(Locale locale) {
        String text = en;
        if (locale.getLanguage().equals("tr")) text = tr;
        for (String[] replacement : replacements) {
            text = text.replace(replacement[0], replacement[1]);
        }
        return text;
    }

    public TextModel clone() {
        TextModel clone = new TextModel(en, tr);
        for (String[] replacement : replacements) {
            clone.addReplacement(replacement[0], replacement[1]);
        }
        return clone;
    }
}
