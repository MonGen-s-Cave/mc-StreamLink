package com.mongenscave.mcislive.utils;

import com.mongenscave.mcislive.McIsLive;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class LoggerUtils {
    private final Logger logger = LogManager.getLogger("McIsLive");

    public void info(@NotNull String msg, @NotNull Object... objs) {
        logger.info(msg, objs);
    }

    public void warn(@NotNull String msg, @NotNull Object... objs) {
        logger.warn(msg, objs);
    }

    public void error(@NotNull String msg, @NotNull Object... objs) {
        logger.error(msg, objs);
    }

    public void printStartup() {
        String main = "\u001B[38;2;189;62;75m";
        String reset = "\u001B[0m";
        String software = McIsLive.getInstance().getServer().getName();
        String version = McIsLive.getInstance().getServer().getVersion();

        info("");
        info("{}  _____     _      _           {}", main, reset);
        info("{} |_   _|   | |    (_)          {}", main, reset);
        info("{}   | |  ___| |     ___   _____ {}", main, reset);
        info("{}   | | / __| |    | \\\\ \\\\ / / _ \\\\{}", main, reset);
        info("{}  _| |_\\\\__ \\\\ |____| |\\\\ V /  __/{}", main, reset);
        info("{} |_____|___/______|_| \\\\_/ \\\\___|{}", main, reset);
        info("");
        info("{}  The plugin successfully started.{}", main, reset);
        info("{}  mc-IsLive {} {}{}", main, software, version, reset);
        info("{}  Discord @ dc.mongenscave.com{}", main, reset);
        info("");
    }
}