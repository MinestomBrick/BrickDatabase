package com.gufli.brickutils.commands;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ArgumentPlayer extends Argument<Player> {

    public final static int INVALID_PLAYER_VALUE_ERROR = 1;

    // proxied object, used for parsing selectors
    private final ArgumentEntity argumentEntity;

    public ArgumentPlayer(@NotNull String id) {
        super(id);
        this.argumentEntity = new ArgumentEntity(id);
    }

    @Override
    public @NotNull Player parse(@NotNull String input) throws ArgumentSyntaxException {
        Player player = argumentEntity.parse(input).findFirstPlayer(null, null);
        if ( player != null ) {
            return player;
        }
        throw new ArgumentSyntaxException("A player with the given name does not exist.", input, INVALID_PLAYER_VALUE_ERROR);

        /*
        Optional<Player> player = MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(input)).findFirst();
        if ( player.isPresent() ) {
            return player.get();
        }
        throw new ArgumentSyntaxException("A player with the given name does not exist.", input, INVALID_PLAYER_VALUE_ERROR);
         */
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:entity";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            byte mask = 0;
            mask |= 0x01;
            mask |= 0x02;
            packetWriter.writeByte(mask);
        });

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

}
