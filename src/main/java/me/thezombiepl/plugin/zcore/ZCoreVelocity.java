package me.thezombiepl.plugin.zcore;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "zcore",
    name = "ZCore",
    version = "1.0.0",
    description = "Core utilities library",
    authors = {"THEzombiePL"},
	url = "https://github.com/THEzombiePL/ZCore"
)
public class ZCoreVelocity {

    private final Logger logger;

    @Inject
    public ZCoreVelocity(Logger logger, @DataDirectory Path dataDirectory) {
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("========================================");
        logger.info("  ZCore v1.0.1");
        logger.info("  Platform: Velocity Proxy");
        logger.info("  Shared libraries included & relocated:");
        logger.info("  ✓ Adventure API (Native)");
        logger.info("  ✓ BoostedYAML");
        logger.info("  ✓ ConfigManager, MessageManager, ColorUtil");
        logger.info("========================================");
    }
}