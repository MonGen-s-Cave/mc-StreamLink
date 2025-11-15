package com.mongenscave.mcislive.data;

import com.mongenscave.mcislive.identifiers.PlatformType;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerMediaData {
    private UUID playerUuid;
    private PlatformType platform;
    private String channelUrl;
    private boolean isLive;
    private long lastChecked;

    public PlayerMediaData(@NotNull UUID playerUuid, @NotNull PlatformType platform, @NotNull String channelUrl) {
        this.playerUuid = playerUuid;
        this.platform = platform;
        this.channelUrl = channelUrl;
        this.isLive = false;
        this.lastChecked = System.currentTimeMillis();
    }
}
