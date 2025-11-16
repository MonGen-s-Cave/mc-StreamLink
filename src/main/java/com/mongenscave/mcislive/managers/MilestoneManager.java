package com.mongenscave.mcislive.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.data.MilestoneData;
import com.mongenscave.mcislive.identifiers.MilestoneType;
import com.mongenscave.mcislive.identifiers.PlatformType;
import com.mongenscave.mcislive.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MilestoneManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_FILE = "milestones.json";

    private final McIsLive plugin;
    private final File dataFile;
    private final Map<UUID, List<MilestoneData>> milestones;
    private final ReadWriteLock lock;

    public MilestoneManager(@NotNull McIsLive plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), DATA_FILE);
        this.milestones = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();

        loadData();
    }

    public void addMilestone(@NotNull UUID playerUuid, @NotNull PlatformType platform, @NotNull MilestoneType type, int value, @NotNull String commandId) {
        lock.writeLock().lock();
        try {
            MilestoneData milestone = new MilestoneData(playerUuid, platform, type, value, commandId);
            milestones.computeIfAbsent(playerUuid, k -> new ArrayList<>()).add(milestone);
            saveDataAsync();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeMilestone(@NotNull UUID milestoneId) {
        lock.writeLock().lock();
        try {
            for (List<MilestoneData> playerMilestones : milestones.values()) {
                boolean removed = playerMilestones.removeIf(m -> m.getId().equals(milestoneId));
                if (removed) {
                    saveDataAsync();
                    return true;
                }
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @NotNull
    public List<MilestoneData> getPlayerMilestones(@NotNull UUID playerUuid) {
        lock.readLock().lock();
        try {
            List<MilestoneData> playerMilestones = milestones.get(playerUuid);
            return playerMilestones != null ? new ArrayList<>(playerMilestones) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void checkMilestones(@NotNull UUID playerUuid, @NotNull PlatformType platform, int viewerCount, int followerCount) {
        lock.writeLock().lock();
        try {
            List<MilestoneData> playerMilestones = milestones.get(playerUuid);
            if (playerMilestones == null) return;

            for (MilestoneData milestone : playerMilestones) {
                if (milestone.getPlatform() != platform) continue;
                if (milestone.isTriggered()) continue;

                final var shouldTrigger = isTrigger(viewerCount, followerCount, milestone);

                if (shouldTrigger) triggerMilestone(playerUuid, milestone, viewerCount);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static boolean isTrigger(int viewerCount, int followerCount, @NotNull MilestoneData milestone) {
        boolean shouldTrigger = false;

        switch (milestone.getType()) {
            case VIEWER -> {
                if (viewerCount >= milestone.getValue()) shouldTrigger = true;
            }
            case FOLLOWER -> {
                if (followerCount >= milestone.getValue()) shouldTrigger = true;
            }
        }
        return shouldTrigger;
    }

    private void triggerMilestone(@NotNull UUID playerUuid, @NotNull MilestoneData milestone, int currentValue) {
        milestone.setTriggered(true);
        saveDataAsync();

        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;

        String commandTemplate = plugin.getConfiguration().getString("milestone-commands." + milestone.getCommandId());

        if (commandTemplate == null) {
            LoggerUtils.error("No milestone command found for " + milestone.getCommandId());
            return;
        }

        String finalCommand = commandTemplate
                .replace("@STREAMER", player.getName())
                .replace("{viewer_count}", String.valueOf(currentValue))
                .replace("{platform}", milestone.getPlatform().name())
                .replace("{milestone}", milestone.getCommandId())
                .replace("{value}", String.valueOf(milestone.getValue()));

        if (finalCommand.contains("@ALL")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                String playerCommand = finalCommand.replace("@ALL", onlinePlayer.getName());
                executeCommand(playerCommand);
            }
        } else executeCommand(finalCommand);
    }

    private void executeCommand(@NotNull String command) {
        plugin.getScheduler().runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }

    public void resetPlayerMilestones(@NotNull UUID playerUuid, @NotNull PlatformType platform) {
        lock.writeLock().lock();
        try {
            List<MilestoneData> playerMilestones = milestones.get(playerUuid);
            if (playerMilestones == null) return;

            playerMilestones.stream()
                    .filter(m -> m.getPlatform() == platform)
                    .forEach(m -> m.setTriggered(false));

            saveDataAsync();
        } finally {
            lock.writeLock().unlock();
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
                Type type = new TypeToken<Map<UUID, List<MilestoneData>>>(){}.getType();
                Map<UUID, List<MilestoneData>> loaded = GSON.fromJson(reader, type);

                if (loaded != null) {
                    milestones.clear();
                    milestones.putAll(loaded);
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
                GSON.toJson(milestones, writer);
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
}
