package com.gufli.brickutils.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.utils.callback.CommandCallback;
import org.jetbrains.annotations.NotNull;

public class CommandGroup extends Command {

    public CommandGroup(@NotNull String name) {
        super(name);

        // default condition check
        setCondition((sender, commandString) -> {
            return getSubcommands().stream()
                    .anyMatch(sub -> sub.getCondition() == null || sub.getCondition().canUse(sender, commandString));
        });

        // sub command not found
        setDefaultExecutor((sender, context) -> {
            CommandCallback cb = MinecraftServer.getCommandManager().getUnknownCommandCallback();
            if ( cb != null ) cb.apply(sender, context.getInput());
        });
    }

}
