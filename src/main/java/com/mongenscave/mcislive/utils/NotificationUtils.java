package com.mongenscave.mcislive.utils;

import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.identifiers.PlatformType;
import com.mongenscave.mcislive.identifiers.keys.ConfigKeys;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class NotificationUtils {
    private final McIsLive plugin;

    public NotificationUtils(@NotNull McIsLive plugin) {
        this.plugin = plugin;
    }

    public void notifyLiveStart(@NotNull Player player, @NotNull PlatformType platform) {
        if (!ConfigKeys.NOTIFICATIONS_ENABLED.getBoolean()) return;

        if (ConfigKeys.NOTIFICATIONS_ACTIONBAR_ENABLED.getBoolean()) {
            String message = ConfigKeys.NOTIFICATIONS_ACTIONBAR_MESSAGE.getString();

            String formatted = message
                    .replace("{player}", player.getName())
                    .replace("{platform}", platform.name());

            plugin.getScheduler().runTask(() -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendActionBar(formatted);
                }
            });
        }

        if (ConfigKeys.NOTIFICATIONS_CHAT_ENABLED.getBoolean()) {
            String message = ConfigKeys.NOTIFICATIONS_CHAT_MESSAGE.getString();

            String formatted = message
                    .replace("{player}", player.getName())
                    .replace("{platform}", platform.name());

            plugin.getScheduler().runTask(() -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(formatted);
                }
            });
        }
    }
}
