package com.blueship.solar;

import com.blueship.solar.time.Clock;
import com.blueship.solar.time.Cycle;
import com.blueship.solar.time.Schedule;
import com.blueship.solar.time.Schedule;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;


public class WorldTime {
    private final @NotNull World world;
    private final boolean wasTimeStopped;
    private @NotNull Schedule schedule;
    private @NotNull Clock clock;
    private long time;
    private boolean ticking;

    public WorldTime(@NotNull World world, @NotNull Schedule schedule) {
        this.world = world;
        this.wasTimeStopped = world.isFixedTime();
        this.ticking = !wasTimeStopped;
        this.time = world.getFullTime();
        this.schedule = schedule;
        this.clock = Clock.of(schedule, time);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    }

    void tick() {
        if (!ticking) return;
        time += (long) clock.getCurrentCycle().timePerTick();
        clock.tick();
        updateWorldTime();
    }

    void addTime(long time) {
        this.time += time;
        clock.setTime(this.time);
        updateWorldTime();
    }

    private void updateWorldTime() {
        world.setFullTime(time);
    }

    void onRemove() {
        if (!wasTimeStopped) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }
    }

    void onScheduleUpdate() {
        this.clock = Clock.of(schedule, time);
    }

    public void setTicking(boolean ticking) {
        this.ticking = ticking;
    }

    public void setSchedule(@NotNull Schedule schedule) {
        this.schedule = schedule;
        this.clock = Clock.of(schedule, time);
    }

    public @NotNull Schedule getSchedule() {
        return schedule;
    }

    public @NotNull World getWorld() {
        return world;
    }

    public boolean wasTimeStopped() {
        return wasTimeStopped;
    }

    public boolean isTicking() {
        return ticking;
    }

    public long getTime() {
        return time;
    }

    public @NotNull Cycle getCurrentCycle() {
        return clock.getCurrentCycle();
    }

    public double getCycleProgress() {
        return clock.getCycleProgress();
    }
}
