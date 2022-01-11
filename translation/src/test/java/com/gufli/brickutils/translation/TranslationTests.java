package com.gufli.brickutils.translation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URL;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TranslationTests {

    @Test
    public void translationManagerTest() {
        TranslationManager tm = new TranslationManager("Test");
        URL url = getClass().getClassLoader().getResource("languages");
        assertNotNull(url);

        tm.loadTranslations(URI.create(url.toExternalForm()));

        Component translated = GlobalTranslator.render(Component.translatable("test.message")
                .args(Component.text("there")), Locale.ENGLISH);

        String result = PlainTextComponentSerializer.plainText().serialize(translated);
        assertEquals("Hey there!", result);
    }

}
