package com.gufli.brickutils.translation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minestom.server.adventure.Localizable;
import net.minestom.server.command.CommandSender;

import java.util.Locale;

public interface TranslationManager {

    static TranslationManager get() {
        return SimpleTranslationManager.INSTANCE;
    }

    Component translate(Locale locale, TranslatableComponent component);

    Component translate(Localizable localizable, TranslatableComponent component);

    Component translate(Localizable localizable, String key);

    Component translate(Localizable localizable, String key, Object... args);

    Component translate(Localizable localizable, String key, Component... args);

    void send(CommandSender sender, TranslatableComponent component);

    void send(CommandSender sender, String key);

    void send(CommandSender sender, String key, Object... args);

    void send(CommandSender sender, String key, Component... args);

}
