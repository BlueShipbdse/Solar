package com.blueship.solar.command.arguments;

import com.blueship.solar.Solar;
import com.blueship.solar.WorldTime;
import com.blueship.solar.util.SuggestionUtil;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class WorldTimeArgumentType implements CustomArgumentType.Converted<WorldTime, String> {
    public static final DynamicCommandExceptionType INVALID_WORLD = new DynamicCommandExceptionType(obj -> new LiteralMessage(obj + " is not a valid world."));
    private WorldTimeArgumentType() {}

    public static WorldTimeArgumentType worldTime() {
        return new WorldTimeArgumentType();
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        return SuggestionUtil.suggestTrimmedValues(builder, Solar.getHandler().getWorlds(), (worldTime -> worldTime.getWorld().getName())).buildFuture();
    }

    @Override
    public WorldTime convert(String nativeType) throws CommandSyntaxException {
        return Solar.getHandler().getWorld(nativeType).orElseThrow(() -> INVALID_WORLD.create(nativeType));
    }
}
