package com.mongenscave.mcislive.identifiers.keys;

import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.config.Config;
import com.mongenscave.mcislive.processor.MessageProcessor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public enum MessageKeys {
    RELOAD("messages.reload"),
    NO_PERMISSION("messages.no-permission");

    private final String path;
    private static final Config config = McIsLive.getInstance().getLanguage();

    MessageKeys(@NotNull String path) {
        this.path = path;
    }

    public @NotNull String getMessage() {
        return MessageProcessor.process(config.getString(path))
                .replace("%prefix%", MessageProcessor.process(config.getString("prefix")));
    }

    public List<String> getMessages() {
        return config.getStringList(path)
                .stream()
                .toList();
    }
}
