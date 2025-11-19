package com.blueship.solar.command.arguments;

import com.blueship.solar.Solar;
import com.blueship.solar.WorldTime;
import com.mojang.brigadier.arguments.ArgumentType;
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

import java.util.concurrent.CompletableFuture;

public class WorldTimeArgumentType implements CustomArgumentType.Converted<WorldTime, Key> {
    private WorldTimeArgumentType() {}

    public static WorldTimeArgumentType worldTime() {
        return new WorldTimeArgumentType();
    }

    @Override
    public WorldTime convert(Key nativeType) throws CommandSyntaxException {
        return Solar.getSolar().getWorld(nativeType).orElseThrow(() -> new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(
                Component.text(nativeType.value() + " is not a valid world."))).create());
    }

    @Override
    public ArgumentType<Key> getNativeType() {
        return ArgumentTypes.key();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Solar.getSolar().getWorlds().forEach(worldTime -> builder.suggest(worldTime.getWorld().key().value()));
        return builder.buildFuture();

    }
}
