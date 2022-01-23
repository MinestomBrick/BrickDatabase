package com.gufli.brickutils.translation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TranslationTests {

    @Test
    public void translationManagerTest() {
        SimpleTranslationManager tm = new SimpleTranslationManager("Test", Locale.ENGLISH);
        tm.loadTranslation(getClass().getClassLoader().getResource("languages/en.json"));

        Component translated = tm.translate(Locale.ENGLISH, Component.translatable("test.message")
                .args(Component.text("there")));

        String result = PlainTextComponentSerializer.plainText().serialize(translated);
        assertEquals("Hey there!", result);
    }

}
