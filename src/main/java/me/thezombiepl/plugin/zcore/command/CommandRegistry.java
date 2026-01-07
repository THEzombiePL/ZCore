package me.thezombiepl.plugin.zcore.command;

/**
 * Universal command registry.
 * Clean entry point that delegates to platform-specific implementations via reflection.
 * NO IMPORTS from Bukkit or Velocity allowed here!
 */
public class CommandRegistry {

    public static void register(Object plugin, UniversalCommand command) {
        String platform = detectPlatform(plugin);
        
        System.out.println("[ZCore] Registering command " + command.getName() + " on " + platform);

        try {
            if ("PAPER".equals(platform)) {
                Class<?> clazz = Class.forName("me.thezombiepl.plugin.zcore.command.PaperCommandRegistrar");
                clazz.getMethod("register", Object.class, UniversalCommand.class)
                     .invoke(null, plugin, command);
            } 
            else if ("VELOCITY".equals(platform)) {
                Class<?> clazz = Class.forName("me.thezombiepl.plugin.zcore.command.VelocityCommandRegistrar");
                clazz.getMethod("register", Object.class, UniversalCommand.class)
                     .invoke(null, plugin, command);
            } 
            else {
                throw new IllegalStateException("Unsupported platform: " + platform);
            }
        } catch (Exception e) {
            System.err.println("[ZCore] Failed to register command: " + command.getName());
            e.printStackTrace();
        }
    }

    private static String detectPlatform(Object plugin) {
        String className = plugin.getClass().getName();
        
        // Check imports/superclasses strings to avoid class loading
        if (className.contains("org.bukkit") || className.contains("JavaPlugin")) {
            return "PAPER";
        }
        
        try {
            Class.forName("org.bukkit.Bukkit");
            return "PAPER";
        } catch (ClassNotFoundException ignored) {}

        if (className.contains("velocitypowered") || className.contains("ProxyServer")) {
            return "VELOCITY";
        }
        
        try {
            Class.forName("com.velocitypowered.api.proxy.ProxyServer");
            return "VELOCITY";
        } catch (ClassNotFoundException ignored) {}

        return "UNKNOWN";
    }

    public static void registerAll(Object plugin, UniversalCommand... commands) {
        for (UniversalCommand command : commands) {
            register(plugin, command);
        }
    }
}