package com.blueship.solar.command.arguments;

import com.blueship.solar.Solar;
import com.blueship.solar.WorldTime;
import com.blueship.solar.util.SuggestionUtil;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class WorldTimeArgumentType implements CustomArgumentType.Converted<WorldTime, String> {
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
        SuggestionUtil.trimInvalids(Solar.getHandler().getWorlds(), (worldTime -> worldTime.getWorld().key().value()), builder.getRemainingLowerCase())
                      .forEach(builder::suggest);
        return builder.buildFuture();

    }

    @Override
    public WorldTime convert(String nativeType) throws CommandSyntaxException {
        return Solar.getHandler().getWorld(nativeType).orElseThrow(() -> new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                Component.text(nativeType + " is not a valid world."))).create());
    }
}
