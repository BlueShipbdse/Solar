package com.blueship.solar.util;

import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class AudienceUtil {
    public static @NotNull String getName(@NotNull Audience audience) {
        if (audience instanceof CommandSender sender) {
            return sender.getName();
        }
        return audience.toString();
    }
}
