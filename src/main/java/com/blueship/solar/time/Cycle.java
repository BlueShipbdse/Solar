package com.blueship.solar.time;

import com.blueship.solar.Constants;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Cycle {
    public static final Cycle DEFAULT = new Cycle("Default", Constants.DAYLIGHT_CYCLE, 1);

    private final @NotNull String name;
    private final @Positive int days;
    private final @Positive long cycleTime;
    private final @Positive double timePerTick;

    public Cycle(@NotNull String name, @Positive long cycleTime, int days) {
        this.name = name;
        this.days = days;
        this.cycleTime = cycleTime;
        this.timePerTick = (double) Constants.DAYLIGHT_CYCLE / cycleTime;
    }

    public @NotNull String name() {
        return name;
    }

    public @Positive long cycleTime() {
        return cycleTime;
    }

    public double timePerTick() {
        return timePerTick;
    }

    public int days() {
        return days;
    }

    @Override
    public @NotNull String toString() {
        return "Cycle: \n  Name: " + name + "\n  Time: " + cycleTime + "\n  Days: " + days;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Cycle that)) return false;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}