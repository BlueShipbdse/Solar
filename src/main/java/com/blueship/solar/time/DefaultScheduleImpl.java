package com.blueship.solar.time;

import java.util.List;

final class DefaultScheduleImpl extends ScheduleImpl {
    public DefaultScheduleImpl() {
        super("Default", List.of(Cycle.DEFAULT));
    }
}
