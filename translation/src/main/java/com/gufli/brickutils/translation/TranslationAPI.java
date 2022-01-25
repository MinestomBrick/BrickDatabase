package com.gufli.brickutils.translation;

public class TranslationAPI {

    private static TranslationManager translationManager;

    public static void setTranslationManager(TranslationManager manager) {
        translationManager = manager;
    }

    public static TranslationManager get() {
        return translationManager;
    }

}
