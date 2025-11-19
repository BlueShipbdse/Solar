package com.blueship.solar.command;

import com.blueship.solar.Solar;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@FunctionalInterface
interface Command extends Supplier<CommandNode<CommandSourceStack>> {
}
