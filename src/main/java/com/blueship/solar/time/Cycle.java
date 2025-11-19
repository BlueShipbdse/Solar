package com.blueship.solar.time;

import com.blueship.solar.Constants;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class Cycle {
    public static final Cycle DEFAULT = new Cycle("Default", Constants.DAYLIGHT_CYCLE);

    private final @NotNull String name;
    private final @Positive int days = 1;
    private final @Positive long cycleTime;
    private final @Positive float timePerTick;

    public Cycle(@NotNull String name, @Positive long cycleTime) {
        this.name = name;
        this.cycleTime = cycleTime;
        this.timePerTick = (float) Constants.DAYLIGHT_CYCLE / cycleTime;
    }

    public @NotNull String name() {
        return name;
    }

    public @Positive long cycleTime() {
        return cycleTime;
    }

    public float timePerTick() {
        return timePerTick;
    }

    @Override
    public @NotNull String toString() {
        return "Name: " + name + "\nTime: " + cycleTime;
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