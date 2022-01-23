package com.gufli.brickutils.translation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.AttributedCharacterIterator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public abstract class BrickComponentRenderer extends TranslatableComponentRenderer<Locale> {

    public static @NotNull BrickComponentRenderer usingTranslationSource(final @NotNull Translator source) {
        requireNonNull(source, "source");
        return new BrickComponentRenderer() {
            @Override
            protected @Nullable MessageFormat translate(final @NotNull String key, final @NotNull Locale context) {
                return source.translate(key, context);
            }
        };
    }

    @Override
    protected @NotNull Component renderTranslatable(@NotNull TranslatableComponent component, @NotNull Locale context) {
        final @Nullable MessageFormat format = this.translate(component.key(), context);
        if ( format == null ) {
            return super.render(component, context);
        }

        Component result = MiniMessage.get().parse(format.toPattern());
        final List<Component> args = component.args();
        for ( int i = 0; i < args.size(); i++ ) {
            final int index = i;
            result = result .replaceText(builder -> builder.match("(\\{[" + Pattern.quote(index + "") + "]})")
                    .replacement((matchResult, textbuilder) -> args.get(index)));
        }

        return result;
    }
}
