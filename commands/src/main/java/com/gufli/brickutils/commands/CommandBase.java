package com.gufli.brickutils.commands;

import com.gufli.brickutils.translation.TranslationManager;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CommandBase extends Command {

    protected static final CommandCondition PLAYER_ONLY = CommandConditionBuilder.create().playerOnly(true).build();

    public CommandBase(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
    }

    public CommandBase(@NotNull String name) {
        super(name);
    }

    protected CommandCondition setCondition(String permission) {
        CommandCondition cc = createCondition(permission);
        setCondition(cc);
        return cc;
    }

    protected CommandCondition setCondition(Permission permission) {
        CommandCondition cc = createCondition(permission);
        setCondition(cc);
        return cc;
    }

    protected CommandCondition setCondition(String permission, boolean playerOnly) {
        CommandCondition cc = createCondition(permission, playerOnly);
        setCondition(cc);
        return cc;
    }

    protected CommandCondition setCondition(Permission permission, boolean playerOnly) {
        CommandCondition cc = createCondition(permission, playerOnly);
        setCondition(cc);
        return cc;
    }

    protected CommandCondition setConditions(CommandCondition... conditions) {
        CommandCondition cc = (s, cs) -> Arrays.stream(conditions).allMatch(c -> c.canUse(s, cs));
        setCondition(cc);
        return cc;
    }

    protected CommandCondition createCondition(String permission) {
        return createCondition(new Permission(permission), false);
    }

    protected CommandCondition createCondition(Permission permission) {
        return createCondition(permission, false);
    }

    protected CommandCondition createCondition(String permission, boolean playerOnly) {
        return createCondition(new Permission(permission), playerOnly);
    }

    protected CommandCondition createCondition(Permission permission, boolean playerOnly) {
        return CommandConditionBuilder.create()
                .permission(permission)
                .playerOnly(playerOnly)
                .build();
    }

    protected void setInvalidUsageMessage(String key) {
        setDefaultExecutor((sender, context) ->
                TranslationManager.get().send(sender, key));
    }

    protected void setInvalidArgumentMessage(Argument<?> argument) {
        setArgumentCallback((sender, exception) ->
                TranslationManager.get().send(sender, "cmd.error.args", argument.getId(), exception.getInput()), argument);
    }

    protected void setInvalidArgumentMessage(Argument<?> argument, String key) {
        setArgumentCallback((sender, exception) ->
                TranslationManager.get().send(sender, key, exception.getInput()), argument);
    }

}
