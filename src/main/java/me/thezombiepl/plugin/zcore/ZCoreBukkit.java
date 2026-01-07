package me.thezombiepl.plugin.zcore;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * ZCore - Core utility library plugin
 * Libraries included:
 * - Adventure API (text components, MiniMessage)
 * - BoostedYAML (config management)
 * - org.json (JSON parsing)
 * - ConfigManager, MessageManager, ColorUtil (utility classes)
 */
public class ZCoreBukkit extends JavaPlugin {
	
	@Override
    public void onEnable() {
        printStartupLogs("Bukkit/Paper (" + getServer().getVersion() + ")");
    }
    @Override
    public void onDisable() {
        getLogger().info("ZCore disabled!");
    }

    // Wspólna metoda logowania
    private void printStartupLogs(String platform) {
        getLogger().info("========================================");
        getLogger().info("  ZCore v" + getDescription().getVersion());
        getLogger().info("  Platform: " + platform);
        getLogger().info("  Shared libraries included & relocated:");
        getLogger().info("  ✓ Adventure API + Platform (Legacy Support)");
        getLogger().info("  ✓ BoostedYAML");
        getLogger().info("  ✓ ConfigManager, MessageManager, ColorUtil");
        getLogger().info("========================================");
    }
}