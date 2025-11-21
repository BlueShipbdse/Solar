package com.blueship.solar.time;

import org.jetbrains.annotations.NotNull;

public sealed interface Clock permits ClockImpl {
    @NotNull Clock DEFAULT = of(Schedule.DEFAULT);

    static @NotNull Clock of(@NotNull Schedule schedule) {
        return new ClockImpl(schedule);
    }

    static @NotNull Clock of(@NotNull Schedule schedule, float time) {
        return new ClockImpl(schedule, time);
    }

    void tick();

    void setTime(float time);
    @NotNull Cycle getCurrentCycle();

    /**
     * Gets the percentage completed of a cycle.
     * @return the completed percent of the current cycle.
     */
    float getCycleProgress();

    /**
     * Gets the percentage completed of the schedule.
     *
     * @return the completed percent of the schedule.
     */
    float getScheduleProgress();
}
