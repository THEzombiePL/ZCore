package me.thezombiepl.plugin.zcore.command;

import me.thezombiepl.plugin.zcore.utils.ColorUtil;
import net.kyori.adventure.text.Component;

/**
 * Universal command context supporting Paper and Velocity platforms.
 * Provides cross-platform abstraction for command execution, permissions, and messaging.
 */
public class CommandContext {
    
    private final Object sender;
    private final String[] args;
    private final String label;
    private final Platform platform;
    
    public enum Platform {
        PAPER, VELOCITY
    }
    
    public CommandContext(Object sender, String[] args, String label, Platform platform) {
        this.sender = sender;
        this.args = args;
        this.label = label;
        this.platform = platform;
    }
    
    /**
     * Gets the raw sender object (CommandSender for Paper, CommandSource for Velocity)
     */
    public Object getRawSender() {
        return sender;
    }
    
    /**
     * Gets command arguments
     */
    public String[] getArgs() {
        return args;
    }
    
    /**
     * Gets the number of arguments
     */
    public int getArgsLength() {
        return args.length;
    }
    
    /**
     * Gets argument at specified index (safe - returns null if out of bounds)
     */
    public String getArg(int index) {
        return index < args.length ? args[index] : null;
    }
    
    /**
     * Gets the command label used
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Gets the current platform
     */
    public Platform getPlatform() {
        return platform;
    }
    
    /**
     * Checks if sender has specified permission
     */
    public boolean hasPermission(String permission) {
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        
        switch (platform) {
            case PAPER:
                return ((org.bukkit.command.CommandSender) sender).hasPermission(permission);
            case VELOCITY:
                return ((com.velocitypowered.api.command.CommandSource) sender).hasPermission(permission);
            default:
                return false;
        }
    }
    
    /**
     * Sends a colored message to the sender
     */
    public void sendMessage(String message) {
        Component colored = ColorUtil.colorize(message);
        
        switch (platform) {
            case PAPER:
                // Paper requires serialization to String
                ((org.bukkit.command.CommandSender) sender).sendMessage(
                    ColorUtil.serialize(colored)
                );
                break;
            case VELOCITY:
                // Velocity natively supports Adventure Components
                ((com.velocitypowered.api.command.CommandSource) sender).sendMessage(colored);
                break;
        }
    }
    
    /**
     * Sends multiple messages
     */
    public void sendMessages(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
    
    /**
     * Checks if sender is a player
     */
    public boolean isPlayer() {
        switch (platform) {
            case PAPER:
                return sender instanceof org.bukkit.entity.Player;
            case VELOCITY:
                return sender instanceof com.velocitypowered.api.proxy.Player;
            default:
                return false;
        }
    }
    
    /**
     * Gets the player object if sender is a player
     * 
     * @return Player object or null if sender is not a player
     */
    public Object getPlayer() {
        if (!isPlayer()) {
            return null;
        }
        return sender;
    }
}