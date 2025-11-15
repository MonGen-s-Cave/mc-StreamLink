package com.mongenscave.mcislive.commands;

import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.identifiers.PlatformType;
import com.mongenscave.mcislive.identifiers.keys.MessageKeys;
import com.mongenscave.mcislive.managers.MediaDataManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandLive implements OrphanCommand {
    private static final McIsLive plugin = McIsLive.getInstance();
    private final MediaDataManager dataManager;

    public CommandLive(@NotNull MediaDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Subcommand("reload")
    @CommandPermission("mcislive.reload")
    public void reload(@NotNull CommandSender sender) {
        plugin.getConfiguration().reload();
        plugin.getLanguage().reload();
        dataManager.reload();
        sender.sendMessage(MessageKeys.RELOAD.getMessage());
    }

    @Subcommand("media remove")
    @CommandPermission("mcislive.addmedia")
    public void addMedia(@NotNull CommandSender sender, @NotNull Player target, @NotNull PlatformType platform, @NotNull String channelUrl) {
        if (!isValidUrl(channelUrl, platform)) {
            sender.sendMessage(MessageKeys.INVALID_URL.getMessage());
            return;
        }

        plugin.getScheduler().runTaskAsynchronously(() -> {
            dataManager.addOrUpdateMedia(target.getUniqueId(), platform, channelUrl);
            plugin.getScheduler().runTask(() -> sender.sendMessage(MessageKeys.SUCCESS_ADD.getMessage()));
        });
    }

    @Subcommand("media remove")
    @CommandPermission("mcislive.removemedia")
    public void removeMedia(@NotNull CommandSender sender, @NotNull Player target, @NotNull PlatformType platform) {
        plugin.getScheduler().runTaskAsynchronously(() -> {
            boolean removed = dataManager.removeMedia(target.getUniqueId(), platform);

            plugin.getScheduler().runTask(() -> {
                if (removed) sender.sendMessage(MessageKeys.SUCCESS_REMOVE.getMessage());
                else sender.sendMessage(MessageKeys.NO_DATA.getMessage());
            });
        });
    }

    private boolean isValidUrl(@NotNull String url, @NotNull PlatformType platform) {
        return switch (platform) {
            case YOUTUBE -> url.contains("youtube.com") || url.contains("youtu.be");
            case TWITCH -> url.contains("twitch.tv");
            case TIKTOK -> url.contains("tiktok.com");
        };
    }
}