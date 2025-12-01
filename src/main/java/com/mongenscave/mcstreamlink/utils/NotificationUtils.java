package com.mongenscave.mcstreamlink.utils;

import com.mongenscave.mcstreamlink.McStreamLink;
import com.mongenscave.mcstreamlink.identifiers.PlatformType;
import com.mongenscave.mcstreamlink.identifiers.keys.ConfigKeys;
import com.mongenscave.mcstreamlink.managers.MediaDataManager;
import net.coma112.easiermessages.EasierMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class NotificationUtils {
    private final McStreamLink plugin;

    public NotificationUtils(@NotNull McStreamLink plugin) {
        this.plugin = plugin;
    }

    public void notifyLiveStart(@NotNull Player player, @NotNull PlatformType platform) {
        if (!ConfigKeys.NOTIFICATIONS_ENABLED.getBoolean()) return;

        if (ConfigKeys.NOTIFICATIONS_ACTIONBAR_ENABLED.getBoolean()) {
            String message = ConfigKeys.NOTIFICATIONS_ACTIONBAR_MESSAGE.getString();

            String formatted = message
                    .replace("{player}", player.getName())
                    .replace("{platform}", platform.getFormatted());

            plugin.getScheduler().runTask(() -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendActionBar(formatted);
                }
            });
        }

        if (ConfigKeys.NOTIFICATIONS_CHAT_ENABLED.getBoolean()) {
            List<String> messages = ConfigKeys.NOTIFICATIONS_CHAT_MESSAGES.getList();

            for (String message : messages) {
                String channelURL = Objects.requireNonNull(plugin.getMediaDataManager().getMediaData(player.getUniqueId(), platform)).getChannelUrl();

                String formatted = message
                        .replace("{player}", player.getName())
                        .replace("{platform}", platform.getFormatted())
                        .replace("{platformProfile}", channelURL == null ? "Ismeretlen" : channelURL);

                Component component = EasierMessages.translateMessage(formatted).build();

                plugin.getScheduler().runTask(() -> {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.sendMessage(component);
                    }
                });
            }
        }
    }
}
