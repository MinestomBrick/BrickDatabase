package com.gufli.brickutils.commands;

import com.gufli.brickutils.translation.TranslationManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import net.minestom.server.utils.callback.CommandCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BrickCommand extends Command {

    private final List<CommandCondition> conditions = new ArrayList<>();

    public BrickCommand(@NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setupDefaultConditions();
    }

    public BrickCommand(@NotNull String name) {
        super(name);
        setupDefaultConditions();
    }

    private void setupDefaultConditions() {
        setCondition((sender, commandString) -> conditions.stream()
                .allMatch(cc -> cc.canUse(sender, commandString)));
    }

    // COMMAND GROUP

    protected void setupCommandGroupDefaults() {
        // default condition check
        addCondition((sender, commandString) -> getSubcommands().stream().anyMatch(sub -> sub.getCondition() == null
                || sub.getCondition().canUse(sender, commandString)));

        // sub command not found
        setDefaultExecutor((sender, context) -> {
            CommandCallback cb = MinecraftServer.getCommandManager().getUnknownCommandCallback();
            if (cb != null) cb.apply(sender, context.getInput());
        });
    }

    // CREATE CONDITIONS

    protected CommandCondition createPermissionCondition(String permission, String errorMsgKey) {
        return createPermissionCondition(new Permission(permission), errorMsgKey);
    }

    protected CommandCondition createPermissionCondition(Permission permission, String errorMsgKey) {
        return createCondition((sender, commandString) -> sender instanceof ConsoleSender   // console can do it all
                        || sender.hasPermission(permission)                                 // or you have specific permission
                        || (sender instanceof Player p && p.getPermissionLevel() == 4),     // or you have permission level 4 (operator)
                errorMsgKey);
    }

    protected CommandCondition createPlayerOnlyCondition(String errorMsgKey) {
        return createCondition((sender, commandString) -> sender instanceof Player, errorMsgKey);
    }

    protected CommandCondition createConsoleOnlyCondition(String errorMsgKey) {
        return createCondition((sender, commandString) -> sender instanceof ConsoleSender, errorMsgKey);
    }

    protected CommandCondition createCondition(CommandCondition condition, String errorMsgKey) {
        return (sender, commandString) -> {
            if (!condition.canUse(sender, commandString)) {
                TranslationManager.get().send(sender, errorMsgKey);
                return false;
            }
            return true;
        };
    }

    // ADD CONDITION

    protected void addCondition(CommandCondition condition) {
        conditions.add(condition);
    }

    protected void addCondition(CommandCondition condition, String errorMsgKey) {
        conditions.add(createCondition(condition, errorMsgKey));
    }

    protected void addPermissionCondition(String permission, String errorMsgKey) {
        conditions.add(createPermissionCondition(permission, errorMsgKey));
    }

    protected void addPermissionCondition(Permission permission, String errorMsgKey) {
        conditions.add(createPermissionCondition(permission, errorMsgKey));
    }

    protected void addPlayerOnlyCondition(String errorMsgKey) {
        conditions.add(createPlayerOnlyCondition(errorMsgKey));
    }

    protected void addConsoleOnlyCondition(String errorMsgKey) {
        conditions.add(createConsoleOnlyCondition(errorMsgKey));
    }

    // INVALID MESSAGES

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
