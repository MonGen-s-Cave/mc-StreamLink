package com.mongenscave.mcislive.identifiers;

import com.mongenscave.mcislive.identifiers.keys.ConfigKeys;
import org.jetbrains.annotations.NotNull;

public enum PlatformType {
    YOUTUBE(ConfigKeys.PLACEHOLDER_YOUTUBE),
    TWITCH(ConfigKeys.PLACEHOLDER_TWITCH);

    private final ConfigKeys placeholderKey;

    PlatformType(ConfigKeys placeholderKey) {
        this.placeholderKey = placeholderKey;
    }

    @NotNull
    public String getFormatted() {
        return placeholderKey.getString();
    }
}
