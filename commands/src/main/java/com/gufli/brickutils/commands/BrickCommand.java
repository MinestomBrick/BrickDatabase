package com.gufli.brickutils.commands;

import com.gufli.brickutils.translation.TranslationManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.utils.callback.CommandCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class BrickCommand extends Command {

    public BrickCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setupDefaultCondition();
    }

    public BrickCommand(@NotNull String name) {
        super(name);
        setupDefaultCondition();
    }

    private void setupDefaultCondition() {
        setCondition(groupSyntaxConditions());
    }

    // COMMAND GROUP

    protected void setupCommandGroupDefaults() {
        // default condition check
        setCondition((sender, commandString) -> {
            boolean result = getSubcommands().stream()
                    .map(Command::getCondition)
                    .filter(Objects::nonNull)
                    .anyMatch(cc -> cc.canUse(sender, null));
            if ( commandString == null ) {
                return result;
            }

            if ( !result ) {
                CommandCallback cb = MinecraftServer.getCommandManager().getUnknownCommandCallback();
                if (cb != null) cb.apply(sender, commandString);
            }

            return result;
        });

        // sub command not found
        setDefaultExecutor((sender, context) -> {
            CommandCallback cb = MinecraftServer.getCommandManager().getUnknownCommandCallback();
            if (cb != null) cb.apply(sender, context.getInput());
        });
    }

    // CONDITIONS

    protected void setCondition(@NotNull Consumer<CommandConditionBuilder> consumer) {
        CommandConditionBuilder builder = conditionBuilder();
        consumer.accept(builder);
        setCondition(builder.build());
    }

    protected @NotNull CommandConditionBuilder conditionBuilder() {
        return new CommandConditionBuilder();
    }

    protected @NotNull CommandCondition conditionAny(@NotNull Collection<CommandCondition> conditions) {
        CommandConditionBuilder builder = conditionBuilder().or();
        conditions.forEach(builder::with);
        return builder.end().build();
    }

    protected @NotNull CommandCondition groupSyntaxConditions() {
        return (sender, commandString) -> getSyntaxes().stream()
                .map(CommandSyntax::getCommandCondition)
                .filter(Objects::nonNull)
                .anyMatch(cc -> cc.canUse(sender, null));
    }

    public @NotNull Collection<CommandSyntax> addConditionalSyntax
            (Consumer<CommandConditionBuilder> consumer, @NotNull CommandExecutor executor, @NotNull Argument<?>... args) {
        CommandConditionBuilder builder = conditionBuilder();
        consumer.accept(builder);
        return super.addConditionalSyntax(builder.build(), executor, args);
    }

    // INVALID MESSAGES

    protected void setInvalidUsageMessage(@NotNull String key) {
        setDefaultExecutor((sender, context) ->
                TranslationManager.get().send(sender, key));
    }

    protected void setInvalidArgumentMessage(@NotNull Argument<?> argument) {
        setArgumentCallback((sender, exception) ->
                TranslationManager.get().send(sender, "cmd.error.args", argument.getId(), exception.getInput()), argument);
    }

    protected void setInvalidArgumentMessage(@NotNull Argument<?> argument, @NotNull String key) {
        setArgumentCallback((sender, exception) ->
                TranslationManager.get().send(sender, key, exception.getInput()), argument);
    }

}
