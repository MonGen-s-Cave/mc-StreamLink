package com.mongenscave.mcislive.hooks.plugins;

import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.data.PlayerMediaData;
import com.mongenscave.mcislive.identifiers.PlatformType;
import com.mongenscave.mcislive.managers.MediaDataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings("deprecation")
public class PlaceholderAPI {
    public static boolean isRegistered = false;
    private static MediaDataManager dataManager;

    public static void registerHook(@NotNull MediaDataManager manager) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            dataManager = manager;
            new PlaceholderIntegration().register();
            isRegistered = true;
        }
    }

    private static class PlaceholderIntegration extends PlaceholderExpansion {
        @Override
        public @NotNull String getIdentifier() {
            return "mcislive";
        }

        @Override
        public @NotNull String getAuthor() {
            return "coma112";
        }

        @Override
        public @NotNull String getVersion() {
            return McIsLive.getInstance().getDescription().getVersion();
        }

        @Override
        public boolean canRegister() {
            return true;
        }

        @Override
        public boolean persist() {
            return true;
        }

        @NotNull
        @Contract(pure = true)
        @Override
        public String onPlaceholderRequest(@NotNull Player player, @NotNull String params) {
            if (dataManager == null) return "Error";

            if (params.equalsIgnoreCase("youtube")) {
                return dataManager.isLive(player.getUniqueId(), PlatformType.YOUTUBE) ? "§aLIVE" : "§cOFFLINE";
            }

            if (params.equalsIgnoreCase("twitch")) {
                return dataManager.isLive(player.getUniqueId(), PlatformType.TWITCH) ? "§aLIVE" : "§cOFFLINE";
            }

            if (params.equalsIgnoreCase("tiktok")) {
                return dataManager.isLive(player.getUniqueId(), PlatformType.TIKTOK) ? "§aLIVE" : "§cOFFLINE";
            }

            if (params.equalsIgnoreCase("any")) {
                return dataManager.isLiveOnAnyPlatform(player.getUniqueId()) ? "§aLIVE" : "§cOFFLINE";
            }

            if (params.equalsIgnoreCase("youtube_url")) {
                PlayerMediaData data = dataManager.getMediaData(player.getUniqueId(), PlatformType.YOUTUBE);
                return data != null ? data.getChannelUrl() : "Nincs beállítva";
            }

            if (params.equalsIgnoreCase("twitch_url")) {
                PlayerMediaData data = dataManager.getMediaData(player.getUniqueId(), PlatformType.TWITCH);
                return data != null ? data.getChannelUrl() : "Nincs beállítva";
            }

            if (params.equalsIgnoreCase("tiktok_url")) {
                PlayerMediaData data = dataManager.getMediaData(player.getUniqueId(), PlatformType.TIKTOK);
                return data != null ? data.getChannelUrl() : "Nincs beállítva";
            }

            if (params.equalsIgnoreCase("count")) {
                Map<PlatformType, PlayerMediaData> allData = dataManager.getAllPlayerData(player.getUniqueId());
                long liveCount = allData.values().stream().filter(PlayerMediaData::isLive).count();
                return String.valueOf(liveCount);
            }

            if (params.equalsIgnoreCase("platforms")) {
                Map<PlatformType, PlayerMediaData> allData = dataManager.getAllPlayerData(player.getUniqueId());
                if (allData.isEmpty()) return "Nincs";

                StringBuilder sb = new StringBuilder();
                allData.forEach((platform, data) -> {
                    if (!sb.isEmpty()) sb.append(", ");
                    sb.append(platform.name());
                    if (data.isLive()) sb.append(" §a✓§r");
                });
                return sb.toString();
            }

            return "Invalid placeholder";
        }
    }
}