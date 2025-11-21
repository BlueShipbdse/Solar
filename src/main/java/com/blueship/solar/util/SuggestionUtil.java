package com.blueship.solar.util;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class SuggestionUtil {
    public static @NotNull Stream<String> trimInvalids(@NotNull Collection<String> values, @NotNull String input) {
        return values.stream().filter(entry -> entry.startsWith(input));
    }
    public static <T> @NotNull Stream<String> trimInvalids(@NotNull Collection<T> values, @NotNull Function<T, String> serializer, @NotNull String input) {
        return values.stream().map(serializer).filter(entry -> entry.startsWith(input));
    }

    public static @NotNull SuggestionsBuilder suggestTrimmedValues(@NotNull SuggestionsBuilder builder, @NotNull Collection<String> values) {
        trimInvalids(values, builder.getRemainingLowerCase()).forEachOrdered(builder::suggest);
        return builder;
    }

    public static <T> @NotNull SuggestionsBuilder suggestTrimmedValues(@NotNull SuggestionsBuilder builder, @NotNull Collection<T> values, @NotNull Function<T, String> serializer) {
        trimInvalids(values, serializer, builder.getRemainingLowerCase()).forEachOrdered(builder::suggest);
        return builder;

    }
}
