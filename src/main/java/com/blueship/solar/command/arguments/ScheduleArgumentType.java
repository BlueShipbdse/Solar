package com.blueship.solar.command.arguments;

import com.blueship.solar.Solar;
import com.blueship.solar.time.Schedule;
import com.blueship.solar.util.SuggestionUtil;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

public class ScheduleArgumentType implements CustomArgumentType.Converted<Schedule, String> {
    private ScheduleArgumentType() {}

    public static ScheduleArgumentType schedule() {
        return new ScheduleArgumentType();
    }

    @Override
    public Schedule convert(String nativeType) throws CommandSyntaxException {
        return Solar.getHandler().getSchedule(nativeType).orElseThrow(() -> new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text(nativeType + " is not a valid schedule."))).create());
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SuggestionUtil.trimInvalids(Solar.getHandler().getScheduleNames(), builder.getRemainingLowerCase())
                      .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
