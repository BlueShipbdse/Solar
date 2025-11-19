package com.blueship.solar.compatibility;

import com.blueship.solar.Solar;
import com.blueship.solar.SolarHandler;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;

public class VanillaCompatibility implements Compatibility {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTimeSkip(@NotNull TimeSkipEvent event) {
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.CUSTOM) return;
        var worldTimeOpt = Solar.getHandler().getWorld(event.getWorld().getName());
        if (worldTimeOpt.isPresent()) {
            event.setCancelled(true);
            worldTimeOpt.get().addTime(event.getSkipAmount());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(@NotNull WorldLoadEvent event) {
        Solar.getHandler().getWorld(event.getWorld().getName()).ifPresent(worldTime -> worldTime.setTicking(!worldTime.wasTimeStopped()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(@NotNull WorldUnloadEvent event) {
        Solar.getHandler().getWorld(event.getWorld().getName()).ifPresent(worldTime -> worldTime.setTicking(false));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldGameRuleChange(@NotNull WorldGameRuleChangeEvent event) {
        if (event.getGameRule() == GameRule.DO_DAYLIGHT_CYCLE && !event.isCancelled()) {
            var worldTimeOpt = Solar.getHandler().getWorld(event.getWorld().getName());
            if (worldTimeOpt.isPresent()) {
                event.setCancelled(true);
                worldTimeOpt.get().setTicking(Boolean.parseBoolean(event.getValue()));
            }
        }
    }
}
