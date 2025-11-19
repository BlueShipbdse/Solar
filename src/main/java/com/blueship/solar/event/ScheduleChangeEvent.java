package com.blueship.solar.event;

import com.blueship.solar.time.Schedule;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ScheduleChangeEvent extends Event {
    private static final @NotNull HandlerList HANDLER_LIST = new HandlerList();
    private final @NotNull Schedule schedule;

    public ScheduleChangeEvent(@NotNull Schedule schedule) {
        this.schedule = schedule;
    }

    public @NotNull Schedule getSchedule() {
        return schedule;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
