package com.gufli.brickutils.translation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.minestom.server.extensions.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TranslationManager {

    private final Logger LOGGER = LoggerFactory.getLogger(TranslationManager.class);

    private final TranslationRegistry registry;

    public TranslationManager(Extension extension) {
        final Key key = Key.key(extension.getOrigin().getName() + ":translations");
        this.registry = TranslationRegistry.create(key);
    }

    public boolean load(URI resources) {
        File directory = new File(resources);
        if ( directory.exists() || !directory.isDirectory() ) {
            LOGGER.warn("The given path is not a directory.");
            return false;
        }

        File[] files = directory.listFiles();
        if ( files == null ) {
            LOGGER.warn("There are no files in the given directory.");
            return false;
        }

        Set<Locale> locales = new HashSet<>();

        for ( File file : files ) {
            if ( !file.isDirectory() ) {
                continue;
            }

            Locale locale = Translator.parseLocale(file.getName());
            if ( locale == null ) {
                locale = Locale.ENGLISH;
            }
            locales.add(locale);

            try (
                    FileReader fr = new FileReader(file);
            ) {
                JsonObject config = JsonParser.parseReader(fr).getAsJsonObject();
                registry.registerAll(locale, config.keySet(), key -> new MessageFormat(config.get(key).getAsString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if ( locales.contains(Locale.ENGLISH) ) {
            registry.defaultLocale(Locale.ENGLISH);
        } else {
            registry.defaultLocale(locales.iterator().next());
        }

        return true;
    }

}
