package com.gufli.brickutils.commands;

import com.gufli.brickutils.translation.TranslationManager;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.checkerframework.checker.units.qual.A;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class CommandConditionBuilder {

    private final Set<CommandCondition> conditions = new HashSet<>();

    public CommandConditionBuilder() {}

    //

    public CommandConditionBuilder with(Collection<CommandCondition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    public CommandConditionBuilder with(CommandCondition... conditions) {
        return with(List.of(conditions));
    }

    public CommandConditionBuilder with(CommandCondition condition, String errorMsgKey) {
        return with((sender, commandString) -> {
            if ( condition.canUse(sender, commandString) ) {
                return true;
            }

            if ( commandString != null ) {
                TranslationManager.get().send(sender, errorMsgKey);
            }
            return false;
        });
    }

    public CommandConditionBuilder permission(String permission) {
        return permission(new Permission(permission));
    }

    public CommandConditionBuilder permission(String permission, String errorMsgKey) {
        return permission(new Permission(permission), errorMsgKey);
    }

    public CommandConditionBuilder permission(Permission permission) {
        return permission(permission, "cmd.error.permission");
    }

    public CommandConditionBuilder permission(Permission permission, String errorMsgKey) {
        return with((sender, commandString) -> sender instanceof ConsoleSender      // console can do it all
                        || sender.hasPermission(permission)                                 // or you have specific permission
                        || (sender instanceof Player p && p.getPermissionLevel() == 4),     // or you have permission level 4 (operator)
                errorMsgKey);
    }

    public CommandConditionBuilder playerOnly() {
        return playerOnly("cmd.error.player-only");
    }

    public CommandConditionBuilder playerOnly(String errorMsgKey) {
        return with((sender, commandString) -> sender instanceof Player, errorMsgKey);
    }

    public CommandConditionBuilder consoleOnly() {
        return consoleOnly("cmd.error.console-only");
    }

    public CommandConditionBuilder consoleOnly(String errorMsgKey) {
        return with((sender, commandString) -> sender instanceof ConsoleSender, errorMsgKey);
    }

    // logical gates

    public CommandConditionBuilder and() {
        final CommandConditionBuilder parent = this;
        return new CommandConditionBuilder() {

            @Override
            public CommandCondition build() {
                return end().build();
            }

            @Override
            public CommandConditionBuilder end() {
                parent.conditions.add(build());
                return parent;
            }
        };
    }

    public CommandConditionBuilder or() {
        final CommandConditionBuilder parent = this;
        return new CommandConditionBuilder() {

            @Override
            public CommandCondition build() {
                return end().build();
            }

            @Override
            public CommandConditionBuilder end() {
                parent.conditions.add((sender, commandString) -> {
                    boolean result = conditions.stream().anyMatch(cc -> cc.canUse(sender, null));
                    if ( commandString == null ) {
                        return result;
                    }

                    // extra call to apply possible feedback
                    return result || conditions.stream().anyMatch(cc -> !cc.canUse(sender, commandString));
                });
                return parent;
            }
        };
    }

    public CommandConditionBuilder end() {
        throw new UnsupportedOperationException();
    }

    // build

    public CommandCondition build() {
        return (sender, commandString) ->  conditions.stream().allMatch(condition -> condition.canUse(sender, commandString));
    }

}