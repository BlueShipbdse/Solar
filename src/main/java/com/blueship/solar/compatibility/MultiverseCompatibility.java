package com.blueship.solar.compatibility;

import com.blueship.solar.Solar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.event.world.MVWorldLoadedEvent;

public class MultiverseCompatibility implements Compatibility {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(@NotNull MVWorldLoadedEvent event) {
        event.getWorld().getBukkitWorld()
             .toJavaOptional()
             .flatMap(world -> Solar.getHandler().getWorld(world.getName()))
             .ifPresent(worldTime -> worldTime.setTicking(!worldTime.wasTimeStopped()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(@NotNull WorldUnloadEvent event) {
        Solar.getHandler().getWorld(event.getWorld().getName()).ifPresent(worldTime -> worldTime.setTicking(false));
    }
}
