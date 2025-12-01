package com.mongenscave.mcstreamlink.identifiers.keys;

import com.mongenscave.mcstreamlink.McStreamLink;
import com.mongenscave.mcstreamlink.config.Config;
import com.mongenscave.mcstreamlink.processor.MessageProcessor;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@Getter
public enum ConfigKeys {
    ALIASES("aliases"),

    API_YOUTUBE_ENABLED("api.youtube.enabled"),
    API_YOUTUBE_API_KEY("api.youtube.api-key"),

    API_TWITCH_ENABLED("api.twitch.enabled"),
    API_TWITCH_CLIENT_ID("api.twitch.client-id"),
    API_TWITCH_CLIENT_SECRET("api.twitch.client-secret"),

    CHECK_INTERVAL("check.interval"),
    CHECK_ENABLED("check.enabled"),

    NOTIFICATIONS_ENABLED("notifications.enabled"),
    NOTIFICATIONS_ACTIONBAR_ENABLED("notifications.actionbar.enabled"),
    NOTIFICATIONS_ACTIONBAR_MESSAGE("notifications.actionbar.message"),
    NOTIFICATIONS_CHAT_ENABLED("notifications.chat.enabled"),
    NOTIFICATIONS_CHAT_MESSAGES("notifications.chat.messages"),

    PLACEHOLDER_TWITCH("placeholders.twitch"),
    PLACEHOLDER_YOUTUBE("placeholders.youtube"),
    PLACEHOLDER_LIVE("placeholders.live"),
    PLACEHOLDER_OFFLINE("placeholders.offline"),

    MILESTONE_PROGRESS_VIEWER_ENABLED("milestone-progress.viewer.enabled"),
    MILESTONE_PROGRESS_VIEWER_COLOR("milestone-progress.viewer.color"),
    MILESTONE_PROGRESS_VIEWER_STYLE("milestone-progress.viewer.style"),
    MILESTONE_PROGRESS_VIEWER_TITLE("milestone-progress.viewer.title"),

    MILESTONE_PROGRESS_FOLLOWER_ENABLED("milestone-progress.follower.enabled"),
    MILESTONE_PROGRESS_FOLLOWER_COLOR("milestone-progress.follower.color"),
    MILESTONE_PROGRESS_FOLLOWER_STYLE("milestone-progress.follower.style"),
    MILESTONE_PROGRESS_FOLLOWER_TITLE("milestone-progress.follower.title");

    private final String path;
    private static final Config config = McStreamLink.getInstance().getConfiguration();

    ConfigKeys(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getString() {
        return MessageProcessor.process(config.getString(path));
    }

    public static @NotNull String getString(@NotNull String path) {
        return config.getString(path);
    }

    public boolean getBoolean() {
        return config.getBoolean(path);
    }

    public int getInt() {
        return config.getInt(path);
    }

    public List<String> getList() {
        return config.getList(path);
    }

    public Section getSection() {
        return config.getSection(path);
    }

    @NotNull
    public Set<String> getKeys() {
        Section section = config.getSection(path);
        return section != null ? section.getRoutesAsStrings(false) : Set.of();
    }
}
