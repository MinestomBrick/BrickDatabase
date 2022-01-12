package com.gufli.brickutils.commands;

import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;

import java.util.HashSet;
import java.util.Set;

public class CommandConditionBuilder {

    private boolean playerOnly;
    private Permission permission;

    private Set<CommandCondition> conditions = new HashSet<>();

    private CommandConditionBuilder() {}

    public static CommandConditionBuilder create() {
        return new CommandConditionBuilder();
    }

    public CommandConditionBuilder playerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
        return this;
    }

    public CommandConditionBuilder permission(Permission permission) {
        this.permission = permission;
        return this;
    }

    public CommandConditionBuilder permission(String permission) {
        this.permission = new Permission(permission);
        return this;
    }

    public CommandConditionBuilder condition(CommandCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public CommandCondition build() {
        Set<CommandCondition> conditions = new HashSet<>(this.conditions);

        if ( playerOnly ) {
            conditions.add((s, cs) -> s instanceof Player);
        }

        if ( permission != null ) {
            conditions.add((s, cs) -> s instanceof ConsoleSender || s.hasPermission(permission) ||
                    (s instanceof Player p && p.getPermissionLevel() == 4));
        }

        return (s, cs) -> conditions.stream().allMatch(cc -> cc.canUse(s, cs));
    }

}
