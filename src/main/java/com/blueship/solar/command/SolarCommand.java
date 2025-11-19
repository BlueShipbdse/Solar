package com.blueship.solar.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class SolarCommand {
    private final LiteralArgumentBuilder<CommandSourceStack> SOLAR_COMMAND_ROOT = Commands.literal("solar");
    private final Map<Class<? extends Command>, CommandNode<CommandSourceStack>> commandNodeReferenceMap = new HashMap<>();

    private void addCommand(@NotNull Command command) {
        var commandNode = command.get();
        SOLAR_COMMAND_ROOT.then(commandNode);
        commandNodeReferenceMap.put(command.getClass(), commandNode);
    }

    public @NotNull LiteralCommandNode<CommandSourceStack> getBuilder() {
        addCommand(new ScheduleCommand());
        addCommand(new WorldCommand());

        return SOLAR_COMMAND_ROOT.build();
    }
}
