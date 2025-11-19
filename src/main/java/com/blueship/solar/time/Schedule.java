package com.blueship.solar.time;

import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public sealed interface Schedule permits ScheduleImpl {
    @NotNull Schedule DEFAULT = new DefaultScheduleImpl();

    static @NotNull Schedule of(@NotNull String name, @NotNull List<Cycle> cycles) {
        return new ScheduleImpl(name, cycles);
    }

    @NotNull String name();

    @NotNull List<Cycle> cycles();

    void addCycle(@NotNull Cycle cycle);
    void addCycle(@NotNull Cycle cycle, int index);

    boolean removeCycle(@NotNull String name);
}
