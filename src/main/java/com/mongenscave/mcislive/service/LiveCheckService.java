package com.mongenscave.mcislive.service;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.client.TwitchApiClient;
import com.mongenscave.mcislive.client.YoutubeApiClient;
import com.mongenscave.mcislive.data.PlayerMediaData;
import com.mongenscave.mcislive.identifiers.PlatformType;
import com.mongenscave.mcislive.identifiers.keys.ConfigKeys;
import com.mongenscave.mcislive.managers.MediaDataManager;
import com.mongenscave.mcislive.utils.LoggerUtils;
import com.mongenscave.mcislive.utils.NotificationUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LiveCheckService {
    private final McIsLive plugin;
    private final MediaDataManager dataManager;
    private final YoutubeApiClient youtubeClient;
    private final TwitchApiClient twitchClient;
    private final NotificationUtils notificationService;

    private final ConcurrentHashMap<UUID, Map<PlatformType, Boolean>> previousStates;
    private MyScheduledTask task;

    public LiveCheckService(
            @NotNull McIsLive plugin,
            @NotNull MediaDataManager dataManager,
            @NotNull YoutubeApiClient youtubeClient,
            @NotNull TwitchApiClient twitchClient,
            @NotNull NotificationUtils notificationService
    ) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.youtubeClient = youtubeClient;
        this.twitchClient = twitchClient;
        this.notificationService = notificationService;
        this.previousStates = new ConcurrentHashMap<>();
    }

    public void start() {
        if (!ConfigKeys.CHECK_ENABLED.getBoolean()) return;

        int interval = ConfigKeys.CHECK_INTERVAL.getInt();
        long intervalTicks = interval * 20L;

        task = plugin.getScheduler().runTaskTimerAsynchronously(this::checkAllPlayers, 20L, intervalTicks);
    }

    public void stop() {
        if (!task.isCancelled()) task.cancel();
    }

    private void checkAllPlayers() {
        Set<UUID> allPlayers = getAllPlayersWithMedia();

        if (allPlayers.isEmpty()) return;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (UUID playerUuid : allPlayers) {
            Map<PlatformType, PlayerMediaData> playerData = dataManager.getAllPlayerData(playerUuid);

            for (Map.Entry<PlatformType, PlayerMediaData> entry : playerData.entrySet()) {
                PlatformType platform = entry.getKey();
                PlayerMediaData data = entry.getValue();

                CompletableFuture<Void> future = checkPlayerPlatform(playerUuid, platform, data);
                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {})
                .exceptionally(exception -> {
                    LoggerUtils.error("Check all players failed", exception);
                    return null;
                });
    }

    @NotNull
    private CompletableFuture<Void> checkPlayerPlatform(@NotNull UUID playerUuid, @NotNull PlatformType platform, @NotNull PlayerMediaData data) {
        CompletableFuture<Boolean> checkFuture = switch (platform) {
            case YOUTUBE -> {
                if (!ConfigKeys.API_YOUTUBE_ENABLED.getBoolean()) yield CompletableFuture.completedFuture(false);
                yield youtubeClient.isChannelLive(data.getChannelUrl());
            }
            case TWITCH -> {
                if (!ConfigKeys.API_TWITCH_ENABLED.getBoolean()) yield CompletableFuture.completedFuture(false);
                yield twitchClient.isChannelLive(data.getChannelUrl());
            }
            case TIKTOK -> CompletableFuture.completedFuture(false);
        };

        return checkFuture.thenAccept(isLive -> {
            boolean wasLive = previousStates
                    .computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                    .getOrDefault(platform, false);

            if (isLive != data.isLive()) dataManager.setLiveStatus(playerUuid, platform, isLive);

            Player player = Bukkit.getPlayer(playerUuid);

            if (player != null && player.isOnline()) {
                if (isLive && !wasLive) notificationService.notifyLiveStart(player, platform);
            }

            previousStates.get(playerUuid).put(platform, isLive);
        }).exceptionally(exception -> {
            LoggerUtils.error(exception.getMessage());
            return null;
        });
    }

    @NotNull
    private Set<UUID> getAllPlayersWithMedia() {
        Set<UUID> players = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!dataManager.getAllPlayerData(player.getUniqueId()).isEmpty()) players.add(player.getUniqueId());
        }

        return players;
    }

    public void checkPlayerNow(@NotNull UUID playerUuid) {
        Map<PlatformType, PlayerMediaData> playerData = dataManager.getAllPlayerData(playerUuid);

        if (playerData.isEmpty()) {
            return;
        }

        for (Map.Entry<PlatformType, PlayerMediaData> entry : playerData.entrySet()) {
            checkPlayerPlatform(playerUuid, entry.getKey(), entry.getValue());
        }
    }
}
