package com.blueship.solar.command.arguments;

import com.blueship.solar.Solar;
import com.blueship.solar.time.Cycle;
import com.blueship.solar.time.Schedule;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CycleArgumentType implements CustomArgumentType<@NotNull Cycle, String> {
    private CycleArgumentType() {}

    public static CycleArgumentType cycle() {
        return new CycleArgumentType();
    }

    @Override
    public Cycle parse(StringReader reader) throws CommandSyntaxException {
        String cycleName = reader.readString();
        reader.skipWhitespace();
        long cycleDuration = reader.readLong();
        return new Cycle(cycleName, cycleDuration);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }
}
