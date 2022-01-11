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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Stream;

public class TranslationManager {

    private final Logger LOGGER = LoggerFactory.getLogger(TranslationManager.class);

    private final TranslationRegistry registry;
    private final Locale defaultLocale;

    public TranslationManager(String namespace, Locale defaultLocale) {
        final Key key = Key.key(namespace.toLowerCase() + ":translations");
        this.registry = TranslationRegistry.create(key);
        this.defaultLocale = defaultLocale;

        this.registry.defaultLocale(defaultLocale);
        GlobalTranslator.get().addSource(registry);
    }

    public TranslationManager(Extension extension, Locale defaulLocale) {
        this(extension.getOrigin().getName(), defaulLocale);
    }

    public void loadTranslations(Extension extension, String pathToResources) {
        URL url = extension.getOrigin().getClassLoader().getResource(pathToResources);
        if (url == null) {
            throw new RuntimeException("Resource not found.");
        }

        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid resource path.", e);
        }

        // load files from in jar (lowest priority)
        Set<Path> paths = new HashSet<>();
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            Stream<Path> pathStream = Files.walk(fs.getPath(pathToResources));
            for (Iterator<Path> it = pathStream.iterator(); it.hasNext(); ) {
                Path path = it.next();
                if ( !path.getFileName().toString().contains(".") ) {
                    continue;
                }

                paths.add(path);
                loadTranslation(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot traverse files of given path.", e);
        }

        // load files from data directory (highest priority)
        for ( Path path : paths ) {
            Path target = Path.of(pathToResources, path.getFileName().toString()); // change path provider
            Path targetFile = extension.getDataDirectory().resolve(target);
            try {
                if ( !Files.exists(targetFile) && !extension.savePackagedResource(targetFile)) {
                    LOGGER.warn("Cannot save packaged resource '" + target + "' of extension '" + extension.getOrigin().getName() + "'.");
                    continue;
                }

                loadTranslation(targetFile);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot read resource '" + path + "'", ex);
            }
        }
    }

    public void loadTranslation(Path path) throws MalformedURLException {
        Locale locale = Translator.parseLocale(path.getFileName().toString().split("\\.")[0]);
        if (locale == null) locale = defaultLocale;
        loadTranslation(path.toUri().toURL(), locale);
    }

    public void loadTranslation(URL resource) {
        String[] parts = resource.getFile().split("/");
        Locale locale = Translator.parseLocale(parts[parts.length - 1].split("\\.")[0]);
        if (locale == null) locale = defaultLocale;
        loadTranslation(resource, locale);
    }

    public void loadTranslation(URL resource, Locale locale) {
        try {
            load(resource.openStream(), locale);
        } catch (IOException e) {
            throw new RuntimeException("Cannot parse json of file '" + resource.getFile() + "'.", e);
        }

    }

    private void load(InputStream inputStream, Locale locale) throws IOException {
        try (
                inputStream;
                InputStreamReader isr = new InputStreamReader(inputStream);
        ) {
            JsonObject config = JsonParser.parseReader(isr).getAsJsonObject();
            registry.registerAll(locale, config.keySet(), key -> new MessageFormat(config.get(key).getAsString()));
        }
    }

//
//        URL url = classLoader.getResource(pathToResources);
//        if (url == null) {
//            throw new RuntimeException("Resource not found.");
//        }
//
//        URI uri;
//        try {
//            uri = url.toURI();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("Invalid resource path.", e);
//        }
//
//        try (FileSystem fs = getFileSystem(uri)) {
//            Stream<Path> paths;
//
//            if ( fs != null ) {
//                paths = Files.walk(fs.getPath(pathToResources));
//            } else {
//                paths = Files.walk(Path.of(uri));
//            }
//
//            Set<Locale> locales = new HashSet<>();
//
//            for (Iterator<Path> it = paths.iterator(); it.hasNext(); ) {
//                Path path = it.next();
//                if ( !path.getFileName().toString().contains(".") ) {
//                    continue;
//                }
//
//                String language = path.getFileName().toString().split("\\.")[0];
//                Locale locale = Translator.parseLocale(language);
//                if (locale == null) {
//                    locale = Locale.ENGLISH;
//                }
//                locales.add(locale);
//
//                try (
//                        BufferedReader br = Files.newBufferedReader(path);
//                ) {
//                    JsonObject config = JsonParser.parseReader(br).getAsJsonObject();
//                    registry.registerAll(locale, config.keySet(), key -> new MessageFormat(config.get(key).getAsString()));
//                } catch (IOException e) {
//                    throw new RuntimeException("Cannot parse json of file '" + path.getFileName().toString() + "'.", e);
//                }
//            }
//
//            if (locales.contains(Locale.ENGLISH)) {
//                registry.defaultLocale(Locale.ENGLISH);
//            } else if (!locales.isEmpty()) {
//                registry.defaultLocale(locales.iterator().next());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Cannot traverse files of given path.", e);
//        }
//    }
//
//    private FileSystem getFileSystem(URI uri) throws IOException {
//        if ("jar".equals(uri.getScheme())) {
//            return FileSystems.newFileSystem(uri, Collections.<String, String>emptyMap());
//        }
//        return null;
//    }

}
