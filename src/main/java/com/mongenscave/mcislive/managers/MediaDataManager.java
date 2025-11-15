package com.mongenscave.mcislive.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.data.PlayerMediaData;
import com.mongenscave.mcislive.identifiers.PlatformType;
import com.mongenscave.mcislive.utils.LoggerUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MediaDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE = "playerdata.json";

    private final McIsLive plugin;
    private final File dataFile;
    private final Map<UUID, Map<PlatformType, PlayerMediaData>> dataCache;
    private final ReadWriteLock lock;

    public MediaDataManager(@NotNull McIsLive plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        this.dataCache = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();

        loadData();
    }

    public void addOrUpdateMedia(@NotNull UUID playerUuid, @NotNull PlatformType platform, @NotNull String channelUrl) {
        lock.writeLock().lock();
        try {
            PlayerMediaData data = new PlayerMediaData(playerUuid, platform, channelUrl);
            dataCache.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>()).put(platform, data);
            saveDataAsync();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeMedia(@NotNull UUID playerUuid, @NotNull PlatformType platform) {
        lock.writeLock().lock();
        try {
            Map<PlatformType, PlayerMediaData> playerData = dataCache.get(playerUuid);
            if (playerData != null) {
                PlayerMediaData removed = playerData.remove(platform);
                if (playerData.isEmpty()) {
                    dataCache.remove(playerUuid);
                }
                if (removed != null) {
                    saveDataAsync();
                    return true;
                }
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setLiveStatus(@NotNull UUID playerUuid, @NotNull PlatformType platform, boolean isLive) {
        lock.writeLock().lock();
        try {
            Map<PlatformType, PlayerMediaData> playerData = dataCache.get(playerUuid);
            if (playerData != null) {
                PlayerMediaData data = playerData.get(platform);

                if (data != null) {
                    data.setLive(isLive);
                    data.setLastChecked(System.currentTimeMillis());
                    saveDataAsync();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nullable
    public PlayerMediaData getMediaData(@NotNull UUID playerUuid, @NotNull PlatformType platform) {
        lock.readLock().lock();
        try {
            Map<PlatformType, PlayerMediaData> playerData = dataCache.get(playerUuid);
            return playerData != null ? playerData.get(platform) : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    @NotNull
    public Map<PlatformType, PlayerMediaData> getAllPlayerData(@NotNull UUID playerUuid) {
        lock.readLock().lock();
        try {
            Map<PlatformType, PlayerMediaData> playerData = dataCache.get(playerUuid);
            return playerData != null ? new HashMap<>(playerData) : Collections.emptyMap();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isLive(@NotNull UUID playerUuid, @NotNull PlatformType platform) {
        lock.readLock().lock();
        try {
            PlayerMediaData data = getMediaData(playerUuid, platform);
            return data != null && data.isLive();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isLiveOnAnyPlatform(@NotNull UUID playerUuid) {
        lock.readLock().lock();
        try {
            Map<PlatformType, PlayerMediaData> playerData = dataCache.get(playerUuid);
            if (playerData == null) return false;

            return playerData.values().stream().anyMatch(PlayerMediaData::isLive);
        } finally {
            lock.readLock().unlock();
        }
    }

    @NotNull
    public Set<UUID> getAllLivePlayers() {
        lock.readLock().lock();
        try {
            Set<UUID> livePlayers = new HashSet<>();
            dataCache.forEach((uuid, platformMap) -> {
                if (platformMap.values().stream().anyMatch(PlayerMediaData::isLive)) livePlayers.add(uuid);
            });

            return livePlayers;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void loadData() {
        lock.writeLock().lock();
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                saveData();
                return;
            }

            try (Reader reader = new FileReader(dataFile)) {
                Type type = new TypeToken<Map<UUID, Map<PlatformType, PlayerMediaData>>>(){}.getType();
                Map<UUID, Map<PlatformType, PlayerMediaData>> loaded = GSON.fromJson(reader, type);

                if (loaded != null) {
                    dataCache.clear();
                    loaded.forEach((uuid, platformMap) ->
                            dataCache.put(uuid, new ConcurrentHashMap<>(platformMap))
                    );
                }
            }
        } catch (IOException exception) {
            LoggerUtils.error(exception.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveData() {
        lock.readLock().lock();
        try {
            try (Writer writer = new FileWriter(dataFile)) {
                GSON.toJson(dataCache, writer);
            }
        } catch (IOException exception) {
            LoggerUtils.error(exception.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }

    private void saveDataAsync() {
        plugin.getScheduler().runTaskAsynchronously(this::saveData);
    }

    public void reload() {
        loadData();
    }
}
