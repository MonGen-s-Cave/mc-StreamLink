package com.mongenscave.mcislive.commands;

import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.data.PlayerMediaData;
import com.mongenscave.mcislive.identifiers.PlatformType;
import com.mongenscave.mcislive.identifiers.keys.MessageKeys;
import com.mongenscave.mcislive.managers.MediaDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.Map;

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

    @Subcommand("addmedia")
    @CommandPermission("mcislive.addmedia")
    public void addMedia(@NotNull CommandSender sender, @NotNull OfflinePlayer target, @NotNull PlatformType platform, @NotNull String channelUrl) {
        if (!isValidUrl(channelUrl, platform)) {
            sender.sendMessage(Component.text("Érvénytelen URL formátum a(z) " + platform.name() + " platformhoz!", NamedTextColor.RED));
            return;
        }

        plugin.getScheduler().runTaskAsynchronously(() -> {
            dataManager.addOrUpdateMedia(target.getUniqueId(), platform, channelUrl);

            plugin.getScheduler().runTask(() ->
                    sender.sendMessage(Component.text()
                            .append(Component.text("Sikeresen hozzáadva: ", NamedTextColor.GREEN))
                            .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(platform.name(), NamedTextColor.AQUA))
                            .build())
            );
        });
    }

    @Subcommand("removemedia")
    @CommandPermission("mcislive.removemedia")
    public void removeMedia(@NotNull CommandSender sender, @NotNull OfflinePlayer target, @NotNull PlatformType platform) {
        plugin.getScheduler().runTaskAsynchronously(() -> {
            boolean removed = dataManager.removeMedia(target.getUniqueId(), platform);

            plugin.getScheduler().runTask(() -> {
                if (removed) {
                    sender.sendMessage(Component.text()
                            .append(Component.text("Sikeresen eltávolítva: ", NamedTextColor.GREEN))
                            .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(platform.name(), NamedTextColor.AQUA))
                            .build());
                } else {
                    sender.sendMessage(Component.text("Nem található adat erre a játékosra ezen a platformon!", NamedTextColor.RED));
                }
            });
        });
    }

    @Subcommand("setlive")
    @CommandPermission("mcislive.setlive")
    public void setLive(@NotNull CommandSender sender, @NotNull OfflinePlayer target, @NotNull PlatformType platform, boolean isLive) {

        plugin.getScheduler().runTaskAsynchronously(() -> {
            PlayerMediaData data = dataManager.getMediaData(target.getUniqueId(), platform);

            if (data == null) {
                plugin.getScheduler().runTask(() ->
                        sender.sendMessage(Component.text("A játékosnak nincs beállítva ezen a platformon csatornája!", NamedTextColor.RED))
                );
                return;
            }

            dataManager.setLiveStatus(target.getUniqueId(), platform, isLive);

            plugin.getScheduler().runTask(() ->
                    sender.sendMessage(Component.text()
                            .append(Component.text("Live státusz frissítve: ", NamedTextColor.GREEN))
                            .append(Component.text(target.getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(platform.name(), NamedTextColor.AQUA))
                            .append(Component.text(" -> ", NamedTextColor.GRAY))
                            .append(Component.text(isLive ? "LIVE" : "OFFLINE", isLive ? NamedTextColor.GREEN : NamedTextColor.RED))
                            .build())
            );
        });
    }

    @Subcommand("info")
    @CommandPermission("mcislive.info")
    public void info(@NotNull CommandSender sender, @Optional OfflinePlayer target) {
        Player finalTarget = target instanceof Player ? (Player) target :
                sender instanceof Player ? (Player) sender : null;

        if (finalTarget == null) {
            sender.sendMessage(Component.text("Meg kell adnod egy játékost!", NamedTextColor.RED));
            return;
        }

        plugin.getScheduler().runTaskAsynchronously(() -> {
            Map<PlatformType, PlayerMediaData> playerData = dataManager.getAllPlayerData(finalTarget.getUniqueId());

            plugin.getScheduler().runTask(() -> {
                if (playerData.isEmpty()) {
                    sender.sendMessage(Component.text("Nincs adat ehhez a játékoshoz!", NamedTextColor.YELLOW));
                    return;
                }

                sender.sendMessage(Component.text()
                        .append(Component.text("═══════════════════════", NamedTextColor.GRAY))
                        .build());
                sender.sendMessage(Component.text()
                        .append(Component.text(finalTarget.getName(), NamedTextColor.YELLOW))
                        .append(Component.text(" média információi:", NamedTextColor.GRAY))
                        .build());
                sender.sendMessage(Component.text()
                        .append(Component.text("═══════════════════════", NamedTextColor.GRAY))
                        .build());

                playerData.forEach((platform, data) -> {
                    sender.sendMessage(Component.text()
                            .append(Component.text("▪ ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(platform.name(), NamedTextColor.AQUA))
                            .append(Component.text(": ", NamedTextColor.GRAY))
                            .append(Component.text(data.isLive() ? "LIVE" : "OFFLINE",
                                    data.isLive() ? NamedTextColor.GREEN : NamedTextColor.RED))
                            .build());
                    sender.sendMessage(Component.text()
                            .append(Component.text("  URL: ", NamedTextColor.GRAY))
                            .append(Component.text(data.getChannelUrl(), NamedTextColor.WHITE))
                            .build());
                });

                sender.sendMessage(Component.text()
                        .append(Component.text("═══════════════════════", NamedTextColor.GRAY))
                        .build());
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