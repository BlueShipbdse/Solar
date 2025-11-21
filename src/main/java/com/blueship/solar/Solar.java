package com.blueship.solar;

import com.blueship.solar.compatibility.Compatibility;
import com.blueship.solar.compatibility.MultiverseCompatibility;

import com.blueship.solar.compatibility.VanillaCompatibility;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Solar extends JavaPlugin {
    private static SolarHandler handler;
    public static SolarHandler getHandler() {
        return handler;
    }
    private final Set<Compatibility> compatibilities = new HashSet<>();

    @Override
    public void onEnable() {
        handler = new SolarHandler(this);

        handler.enable();

        Bukkit.getPluginManager().registerEvents(handler, this);

        compatibilities.add(new VanillaCompatibility());
        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            compatibilities.add(new MultiverseCompatibility());
        }

        compatibilities.forEach(compat -> Bukkit.getPluginManager().registerEvents(compat, this));
    }

    @Override
    public void onDisable() {
        handler.disable();
    }
}
