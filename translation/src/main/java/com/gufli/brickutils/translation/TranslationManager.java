package com.gufli.brickutils.translation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.minestom.server.extensions.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TranslationManager {

    private final Logger LOGGER = LoggerFactory.getLogger(TranslationManager.class);

    private final TranslationRegistry registry;

    public TranslationManager(String namespace) {
        final Key key = Key.key(namespace.toLowerCase() + ":translations");
        this.registry = TranslationRegistry.create(key);
        GlobalTranslator.get().addSource(registry);
    }

    public TranslationManager(Extension extension) {
        this(extension.getOrigin().getName());
    }

    public void loadTranslations(ClassLoader classLoader, String pathToResources) {
        URL url = classLoader.getResource(pathToResources);
        if ( url == null ) {
            throw new RuntimeException("");
        }

        loadTranslations(URI.create(url.toExternalForm()));
    }

    public void loadTranslations(URI resources) {
        File directory = new File(resources);
        if ( !directory.exists() || !directory.isDirectory() ) {
            throw new RuntimeException("The given path is not a directory.");
        }

        File[] files = directory.listFiles();
        if ( files == null ) {
            throw new RuntimeException("There are no file in the given directory.");
        }

        Set<Locale> locales = new HashSet<>();

        for ( File file : files ) {
            if ( !file.isFile() ) {
                continue;
            }

            String language = file.getName().split("\\.")[0];
            Locale locale = Translator.parseLocale(language);
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
                throw new RuntimeException("Cannot parse json of file '" + file.getName() + "'.", e);
            }
        }


        if ( locales.contains(Locale.ENGLISH) ) {
            registry.defaultLocale(Locale.ENGLISH);
        } else if ( !locales.isEmpty() ) {
            registry.defaultLocale(locales.iterator().next());
        }
    }

}
