package com.blueship.solar;

import com.blueship.solar.event.ScheduleChangeEvent;
import com.blueship.solar.time.Cycle;
import com.blueship.solar.time.Schedule;
import com.blueship.solar.time.Schedule;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Solar extends JavaPlugin implements Listener {
    private static Solar solar;
    public static @NotNull Solar getSolar() {
        return solar;
    }
    private final @NotNull Map<String, Schedule> schedules = new LinkedHashMap<>();
    private final @NotNull Map<Key, WorldTime> worlds = new LinkedHashMap<>();
    private @Nullable BukkitTask tickTask;

    public void createSchedule(@NotNull String name) {
        schedules.putIfAbsent(name, Schedule.of(name, List.of()));
    }

    public boolean removeSchedule(@NotNull String name) {
        return schedules.remove(name) != null;
    }

    public @NotNull Optional<Schedule> getSchedule(@NotNull String name) {
        return Optional.ofNullable(schedules.get(name));
    }

    public @NotNull Collection<Schedule> getSchedules() {
        return new ArrayList<>(schedules.values());
    }

    public @NotNull Collection<String> getScheduleNames() {
        return new ArrayList<>(schedules.keySet());
    }

    public @NotNull Optional<WorldTime> getWorld(@NotNull Key key) {
        return Optional.of(worlds.get(key));
    }

    public @NotNull Collection<WorldTime> getWorlds() {
        return worlds.values();
    }


    private void tick() {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        synchronized (worlds) {
            worlds.forEach((k, worldTime) -> {
                if (worldTime.getWorld().getLoadedChunks().length > 0) {
                    worldTime.tick();
                }
            });
        }
    }

    private void loadSchedules() {
        File scheduleFile = new File(getDataFolder(), "schedules.yml");
        YamlConfiguration scheduleConfig = YamlConfiguration.loadConfiguration(scheduleFile);
        for (var scheduleName : scheduleConfig.getKeys(false)) {
            ConfigurationSection scheduleSection = scheduleConfig.getConfigurationSection(scheduleName);

            List<Cycle> cycleList = new ArrayList<>();
            var cycleSection = scheduleSection.getConfigurationSection("cycles");
            for (var cycleName : cycleSection.getKeys(false)) {
                long time = scheduleSection.getLong(cycleName);
                if (time <= 0) {
                    getSLF4JLogger().warn(
                            "Failed to read schedules.yml. Cycle {} for defined Schedule {} is not valid! Time must be greater than 0.",
                            cycleName,
                            scheduleName
                    );
                }
                cycleList.add(new Cycle(cycleName, time));
            }
            schedules.put(scheduleName, Schedule.of(scheduleName, cycleList));
        }

        Schedule defaultSchedule = Schedule.DEFAULT;
        schedules.putIfAbsent(defaultSchedule.name(), defaultSchedule);
    }

    private void loadWorlds() {
        File scheduleFile = new File(getDataFolder(), "world-time.yml");
        YamlConfiguration scheduleConfig = YamlConfiguration.loadConfiguration(scheduleFile);

        for (String worldName : scheduleConfig.getKeys(false)) {
            var worldSection = scheduleConfig.getConfigurationSection(worldName);
            String scheduleName;
            if (worldSection.contains("schedule")) {
                scheduleName = worldSection.getString("schedule");
            } else {
                continue;
            }

            var world = Bukkit.getWorld(worldName);
            if (world == null) {
                getSLF4JLogger().warn("Failed to read world-time.yml. For World {}, the world does not exist!", worldName);
                continue;
            }

            var schedule = schedules.get(scheduleName);
            if (schedule == null) {
                getSLF4JLogger().warn("Failed to read world-time.yml. For World {}, Schedule {} is not a valid schedule!", worldName, scheduleName);
                continue;
            }

            worlds.put(Key.key(worldName), new WorldTime(world, schedule));
        }

        for (World world : Bukkit.getWorlds()) {
            worlds.putIfAbsent(world.key(), new WorldTime(world, Schedule.DEFAULT));
        }
    }

    private void saveSchedules() {
        File scheduleFile = new File(getDataFolder(), "schedules.yml");
        YamlConfiguration scheduleConfig = YamlConfiguration.loadConfiguration(scheduleFile);

        for (var schedule : schedules.values()) {
            var scheduleSection = scheduleConfig.createSection(schedule.name());

            var cycleSection = scheduleSection.createSection("cycles");
            for (var cycle : schedule.cycles()) {
                cycleSection.set(cycle.name(), cycle.cycleTime());
            }
        }

        try {
            scheduleConfig.save(scheduleFile);
        } catch (IOException e) {
            getSLF4JLogger().error("", e);
        }
    }

    private void saveWorlds() {
        File worldFile = new File(getDataFolder(), "world-time.yml");
        YamlConfiguration scheduleConfig = YamlConfiguration.loadConfiguration(worldFile);

        for (var worldTime : worlds.values()) {
            var world = worldTime.getWorld();
            var worldSection = scheduleConfig.createSection(world.getName());
            worldSection.set("schedule", worldTime.getSchedule().name());
        }

        try {
            scheduleConfig.save(worldFile);
        } catch (IOException e) {
            getSLF4JLogger().error("", e);
        }
    }

    @Override
    public void onEnable() {
        solar = this;

        loadSchedules();
        loadWorlds();

        Bukkit.getPluginManager().registerEvents(this, this);
        tickTask = Bukkit.getScheduler().runTaskTimer(this, this::tick, 0L, 1L);
    }

    @Override
    public void onDisable() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }

        saveSchedules();
        saveWorlds();

        schedules.clear();
        worlds.forEach((worldKey, worldTime) -> worldTime.onRemove());
        worlds.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onScheduleChange(@NotNull ScheduleChangeEvent event) {
        worlds.forEach((key, worldTime) -> {
            if (event.getSchedule().equals(worldTime.getSchedule())) {
                worldTime.onScheduleUpdate();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTimeSkip(@NotNull TimeSkipEvent event) {
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.CUSTOM) return;
        var worldTime = worlds.get(event.getWorld().key());
        if (worldTime != null) {
            event.setCancelled(true);
            worldTime.addTime(event.getSkipAmount());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(@NotNull WorldLoadEvent event) {
        var worldTime = worlds.get(event.getWorld().key());
        if (worldTime != null) {
            worldTime.setTicking(!worldTime.wasTimeStopped());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(@NotNull WorldUnloadEvent event) {
        var worldTime = worlds.get(event.getWorld().key());
        if (worldTime != null) {
            worldTime.setTicking(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldGameRuleChange(@NotNull WorldGameRuleChangeEvent event) {
        if (event.getGameRule() == GameRule.DO_DAYLIGHT_CYCLE && !event.isCancelled()) {
            var worldTime = worlds.get(event.getWorld().key());
            if (worldTime != null) {
                event.setCancelled(true);
                worldTime.setTicking(Boolean.parseBoolean(event.getValue()));
            }
        }
    }

}
