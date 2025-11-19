package com.blueship.solar.time;

import com.blueship.solar.event.ScheduleChangeEvent;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

sealed class ScheduleImpl implements Schedule permits DefaultScheduleImpl {
    private final @NotNull String name;
    private final @NotNull List<Cycle> cycles;

    ScheduleImpl(@NotNull String name, @NotNull List<Cycle> cycles) {
        this.name = name;
        this.cycles = new ArrayList<>(cycles);
    }

    public @NotNull String name() {
        return name;
    }

    public @UnmodifiableView @NotNull List<Cycle> cycles() {
        return Collections.unmodifiableList(cycles);
    }

    @Override
    public void addCycle(@NotNull Cycle cycle) {
        cycles.addLast(cycle);
        Bukkit.getPluginManager().callEvent(new ScheduleChangeEvent(this));
    }

    @Override
    public void addCycle(@NotNull Cycle cycle, int index) {
        cycles.add(index, cycle);
        Bukkit.getPluginManager().callEvent(new ScheduleChangeEvent(this));
    }

    @Override
    public boolean removeCycle(@NotNull String name) {
        AtomicBoolean hasFound = new AtomicBoolean(false);
        cycles.removeIf(cycle -> {
            if (cycle.name().equals(name)) {
                hasFound.compareAndSet(false, true);
                return true;
            }
            return false;
        });
        if (hasFound.get()) {
            Bukkit.getPluginManager().callEvent(new ScheduleChangeEvent(this));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Schedule that)) return false;
        return Objects.equals(this.name, that.name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
