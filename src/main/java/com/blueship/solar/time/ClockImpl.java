package com.blueship.solar.time;

import com.blueship.solar.Solar;
import com.google.common.collect.Iterators;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.blueship.solar.Constants.DAYLIGHT_CYCLE;

final class ClockImpl implements Clock {
    private final @NotNull Schedule schedule;
    private final long totalCycleTime;
    private final @NotNull Iterator<TimePoint> futureTime;
    private final @Positive int size;
    private final int maxChecks;
    private @NotNull TimePoint currentTime;
    private float currentCycleTime = 0;

    private record TimePoint(@NotNull Cycle cycle, @NonNegative long startTime, @NonNegative long endTime) {}

    ClockImpl(@NotNull Schedule schedule) {
        this.schedule = schedule;
        Collection<TimePoint> timePoints = new ArrayList<>();
        long lastTimePoint = 0;
        for (Cycle cycle : schedule.cycles()) {
            timePoints.add(new TimePoint(cycle, lastTimePoint, lastTimePoint + DAYLIGHT_CYCLE));
            lastTimePoint += DAYLIGHT_CYCLE;
        }
        if (!timePoints.isEmpty()) {
            this.totalCycleTime = lastTimePoint;
        } else {
            Solar.getSolar().getSLF4JLogger().warn("Failed to initialize clock for schedule {}. Running default cycle of 24000 ticks.", schedule.name());
            Cycle defaultCycle = Cycle.DEFAULT;
            this.totalCycleTime = DAYLIGHT_CYCLE;
            timePoints = List.of(new TimePoint(defaultCycle, 0, DAYLIGHT_CYCLE));
        }
        this.size = timePoints.size();
        this.maxChecks = size * 2;
        this.futureTime = Iterators.cycle(timePoints);
        this.currentTime = futureTime.next();
    }

    ClockImpl(@NotNull Schedule schedule, long time) {
        this(schedule);
        fastAdvanceCycles(time);
    }

    private void fastAdvanceCycles(long time) {
        this.currentCycleTime = time % totalCycleTime;
        tryAdvanceCycles();
    }

    private void tryAdvanceCycles() {
        if (size == 1) return;
        int checks = 0;
        while (currentCycleTime >= currentTime.endTime || currentCycleTime < currentTime.startTime) {
            currentTime = futureTime.next();
            if (++checks > maxChecks) {
                Solar.getSolar().getSLF4JLogger().warn("Too many advances occurred for schedule {}!", schedule.name());
                return;
            }
        }
    }

    @Override
    public void tick() {
        if (++currentCycleTime > totalCycleTime) {
            currentCycleTime -= totalCycleTime;
        }
        tryAdvanceCycles();
    }

    @Override
    public void setTime(long time) {
        fastAdvanceCycles(time);
    }

    @Override
    public @NotNull Cycle getCurrentCycle() {
        return currentTime.cycle();
    }

    @Override
    public double getCycleProgress() {
        return (currentCycleTime - currentTime.startTime) / (currentTime.endTime - currentTime.startTime);
    }

    @Override
    public double getScheduleProgress() {
        return currentCycleTime / totalCycleTime;
    }
}
