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
public class ZCorePlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("========================================");
        getLogger().info("  ZCore v" + getDescription().getVersion());
        getLogger().info("  Shared libraries loaded:");
        getLogger().info("  ✓ Adventure API 4.17.0");
        getLogger().info("  ✓ Adventure Platform Bukkit 4.3.4");
        getLogger().info("  ✓ BoostedYAML 1.3.7");
        getLogger().info("  ✓ org.json 20240303");
        getLogger().info("  ✓ ConfigManager, MessageManager, ColorUtil");
        getLogger().info("========================================");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("ZCore disabled!");
    }
}