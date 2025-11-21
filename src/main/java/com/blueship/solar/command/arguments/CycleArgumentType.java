package com.blueship.solar.command.arguments;

import com.blueship.solar.time.Cycle;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

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
        reader.skipWhitespace();
        int days = reader.readInt();
        return new Cycle(cycleName, cycleDuration, days);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }
}
