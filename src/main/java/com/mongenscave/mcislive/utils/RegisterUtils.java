package com.mongenscave.mcislive.utils;

import com.mongenscave.mcislive.McIsLive;
import com.mongenscave.mcislive.commands.CommandLive;
import com.mongenscave.mcislive.exception.CommandExceptionHandler;
import com.mongenscave.mcislive.identifiers.keys.ConfigKeys;
import lombok.experimental.UtilityClass;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.orphan.Orphans;

@UtilityClass
public class RegisterUtils {
    public void registerCommands() {
        var lamp = BukkitLamp.builder(McIsLive.getInstance())
                .exceptionHandler(new CommandExceptionHandler())
                .build();

        lamp.register(Orphans.path(ConfigKeys.ALIASES.getList().toArray(String[]::new)).handler(new CommandLive(McIsLive.getInstance().getMediaDataManager())));
    }
}
