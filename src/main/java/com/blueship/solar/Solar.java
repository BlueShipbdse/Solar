package com.blueship.solar;

import com.blueship.solar.event.ScheduleChangeEvent;
import com.blueship.solar.time.Cycle;
import com.blueship.solar.time.Schedule;
import com.blueship.solar.time.Schedule;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;

import com.blueship.solar.compatibility.VanillaCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Solar extends JavaPlugin {
    private static SolarHandler handler;
    public static SolarHandler getHandler() {
        return handler;
    }
    private Set<Compatibility> compatibilities = new HashSet<>();

    @Override
    public void onEnable() {
        handler = new SolarHandler(this);

        handler.enable();

        Bukkit.getPluginManager().registerEvents(handler, this);

        Bukkit.getPluginManager().registerEvents(this, this);
        tickTask = Bukkit.getScheduler().runTaskTimer(this, this::tick, 0L, 1L);
        compatibilities.add(new VanillaCompatibility());

        compatibilities.forEach(compat -> Bukkit.getPluginManager().registerEvents(compat, this));
    }

    @Override
    public void onDisable() {
        handler.disable();
    }
}
