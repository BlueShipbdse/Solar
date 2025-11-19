package com.blueship.solar;

import com.blueship.solar.event.ScheduleChangeEvent;
import com.blueship.solar.time.Cycle;
import com.blueship.solar.time.Schedule;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class SolarHandler implements Listener {
    public @NotNull Logger getLogger() {
        return solar.getSLF4JLogger();
    }
    private final @NotNull Solar solar;
    private final @NotNull Map<String, Schedule> schedules = new LinkedHashMap<>();
    private final @NotNull Map<String, WorldTime> worlds = new LinkedHashMap<>();

    private @Nullable BukkitTask tickTask;

    SolarHandler(@NotNull Solar solar) {
        this.solar = solar;
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

    void enable() {
        loadSchedules();
        loadWorlds();

        tickTask = Bukkit.getScheduler().runTaskTimer(solar, this::tick, 0L, 1L);
    }

    void disable() {
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

    private void loadSchedules() {
        File scheduleFile = new File(solar.getDataFolder(), "schedules.yml");
        YamlConfiguration scheduleConfig = YamlConfiguration.loadConfiguration(scheduleFile);
        for (var scheduleName : scheduleConfig.getKeys(false)) {
            var scheduleSection = scheduleConfig.getConfigurationSection(scheduleName);
            var cycleSection = scheduleSection.getConfigurationSection("cycles");

            List<Cycle> cycleList = new ArrayList<>();
            if (cycleSection != null) {
                for (var cycleName : cycleSection.getKeys(false)) {
                    long time = cycleSection.getLong(cycleName);
                    if (time <= 0) {
                        getLogger().warn(
                                "Failed to read schedules.yml. Cycle {} for defined Schedule {} is not valid! Time must be greater than 0.",
                                cycleName,
                                scheduleName
                        );
                    }
                    cycleList.add(new Cycle(cycleName, time));
                }
            }
            schedules.put(scheduleName, Schedule.of(scheduleName, cycleList));
        }

        Schedule defaultSchedule = Schedule.DEFAULT;
        schedules.putIfAbsent(defaultSchedule.name(), defaultSchedule);
    }

    private void loadWorlds() {
        var scheduleFile = new File(solar.getDataFolder(), "world-time.yml");
        var scheduleConfig = YamlConfiguration.loadConfiguration(scheduleFile);

        for (String worldName : scheduleConfig.getKeys(false)) {
            var worldSection = scheduleConfig.getConfigurationSection(worldName);

            String scheduleName;
            if (worldSection.contains("schedule")) {
                scheduleName = worldSection.getString("schedule");
            } else {
                getLogger().warn("Failed to read world-time.yml. For World {}, no schedule exists!", worldName);
                continue;
            }

            var world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warn("Failed to read world-time.yml. For World {}, the world does not exist!", worldName);
                continue;
            }

            var schedule = schedules.get(scheduleName);
            if (schedule == null) {
                getLogger().warn("Failed to read world-time.yml. For World {}, Schedule {} is not a valid schedule!", worldName, scheduleName);
                continue;
            }

            worlds.put(worldName, new WorldTime(world, schedule));
        }

        for (World world : Bukkit.getWorlds()) {
            worlds.putIfAbsent(world.getName(), new WorldTime(world, Schedule.DEFAULT));
        }
    }

    private void saveSchedules() {
        File scheduleFile = new File(solar.getDataFolder(), "schedules.yml");
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
            getLogger().error("", e);
        }
    }

    private void saveWorlds() {
        File worldFile = new File(solar.getDataFolder(), "world-time.yml");
        YamlConfiguration scheduleConfig = YamlConfiguration.loadConfiguration(worldFile);

        for (var worldTime : worlds.values()) {
            var world = worldTime.getWorld();
            var worldSection = scheduleConfig.createSection(world.getName());
            worldSection.set("schedule", worldTime.getSchedule().name());
        }

        try {
            scheduleConfig.save(worldFile);
        } catch (IOException e) {
            getLogger().error("", e);
        }
    }

    public @NotNull FileConfiguration getConfig() {
        return solar.getConfig();
    }

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

    public @NotNull Optional<WorldTime> getWorld(@NotNull String name) {
        return Optional.of(worlds.get(name));
    }

    public @NotNull Collection<WorldTime> getWorlds() {
        return worlds.values();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onScheduleChange(@NotNull ScheduleChangeEvent event) {
        worlds.forEach((key, worldTime) -> {
            if (event.getSchedule().equals(worldTime.getSchedule())) {
                worldTime.onScheduleUpdate();
            }
        });
    }
}
