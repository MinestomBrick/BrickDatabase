package com.gufli.brickutils.translation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.minestom.server.adventure.Localizable;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.entity.Player;
import net.minestom.server.extensions.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class SimpleTranslationManager implements TranslationManager {

    static SimpleTranslationManager INSTANCE;

    private final Logger LOGGER = LoggerFactory.getLogger(SimpleTranslationManager.class);

    private final TranslationRegistry registry;
    private final Locale defaultLocale;

    public SimpleTranslationManager(String namespace, Locale defaultLocale) {
        INSTANCE = this;

        final Key key = Key.key(namespace.toLowerCase() + ":translations");
        this.registry = TranslationRegistry.create(key);
        this.defaultLocale = defaultLocale;

        this.registry.defaultLocale(defaultLocale);
        GlobalTranslator.get().addSource(registry);
    }

    public SimpleTranslationManager(Extension extension, Locale defaulLocale) {
        this(extension.getOrigin().getName(), defaulLocale);
    }

    // api

    @Override
    public Component translate(Localizable localizable, TranslatableComponent component) {
        Locale locale = localizable.getLocale();
        if ( locale == null ) locale = defaultLocale;
        return GlobalTranslator.render(component, locale);
    }

    @Override
    public Component translate(Localizable localizable, String key) {
        return translate(localizable, Component.translatable(key));
    }

    @Override
    public Component translate(Localizable localizable, String key, Object... args) {
        Component[] cargs = new Component[args.length];
        for ( int i = 0 ; i < args.length; i++ ) {
            cargs[i] = Component.text(args[i].toString());
        }
        return translate(localizable, Component.translatable(key).args(cargs));
    }

    @Override
    public Component translate(Localizable localizable, String key, Component... args) {
        return translate(localizable, Component.translatable(key).args(args));
    }

    @Override
    public void send(CommandSender sender, TranslatableComponent component) {
        if ( sender instanceof Player p ) {
            sender.sendMessage(translate(p, component));
            return;
        }
        sender.sendMessage(GlobalTranslator.render(component, defaultLocale));
    }

    @Override
    public void send(CommandSender sender, String key) {
        send(sender, Component.translatable(key));
    }

    @Override
    public void send(CommandSender sender, String key, String... args) {
        Component[] cargs = new Component[args.length];
        for ( int i = 0 ; i < args.length; i++ ) {
            cargs[i] = Component.text(args[i]);
        }
        send(sender, Component.translatable(key).args(cargs));
    }

    @Override
    public void send(CommandSender sender, String key, Component... args) {
        send(sender, Component.translatable(key).args(args));
    }

    // loading
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
        Locale locale = Locale.forLanguageTag(path.getFileName().toString().split("\\.")[0]);
        loadTranslation(path.toUri().toURL(), locale);
    }

    public void loadTranslation(URL resource) {
        String[] parts = resource.getFile().split("/");
        Locale locale = Locale.forLanguageTag(parts[parts.length - 1].split("\\.")[0]);
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

}
