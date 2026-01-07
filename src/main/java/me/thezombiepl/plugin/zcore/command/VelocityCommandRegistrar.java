package me.thezombiepl.plugin.zcore.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.List;
import java.util.Collections;

public class VelocityCommandRegistrar {

    public static void register(Object proxyObj, UniversalCommand command) {
        try {
            // Pobieranie CommandManager (obsługa ProxyServer, pluginu przez getter lub pole)
            CommandManager cmdMgr = null;

            if (proxyObj instanceof ProxyServer) {
                cmdMgr = ((ProxyServer) proxyObj).getCommandManager();
            } else if (proxyObj instanceof com.velocitypowered.api.plugin.PluginContainer) {
                // Jeśli przekazano PluginContainer (rzadkie, ale możliwe)
                throw new IllegalArgumentException("Expected plugin instance or ProxyServer, got PluginContainer");
            } else {
                try {
                    // 1. Próba przez getter getServer()
                    java.lang.reflect.Method getServer = proxyObj.getClass().getMethod("getServer");
                    cmdMgr = ((ProxyServer) getServer.invoke(proxyObj)).getCommandManager();
                } catch (Exception e1) {
                    try {
                        // 2. Próba przez bezpośredni dostęp do pola 'server'
                        java.lang.reflect.Field serverField = proxyObj.getClass().getDeclaredField("server");
                        serverField.setAccessible(true);
                        cmdMgr = ((ProxyServer) serverField.get(proxyObj)).getCommandManager();
                    } catch (Exception e2) {
                        try {
                            // 3. Próba przez bezpośredni getter getCommandManager()
                            java.lang.reflect.Method getMgr = proxyObj.getClass().getMethod("getCommandManager");
                            cmdMgr = (CommandManager) getMgr.invoke(proxyObj);
                        } catch (Exception e3) {
                            throw new IllegalStateException("Could not find CommandManager or ProxyServer in plugin instance.");
                        }
                    }
                }
            }

            if (cmdMgr == null) throw new IllegalStateException("CommandManager is null");

            // Tworzymy implementację SimpleCommand, która idealnie pasuje do Twojego systemu
            SimpleCommand velocityCommand = new SimpleCommand() {
                @Override
                public void execute(Invocation invocation) {
                    // Konwersja Invocation (Velocity) na Twój uniwersalny CommandContext
                    CommandContext context = new CommandContext(
                        invocation.source(),
                        invocation.arguments(),
                        command.getName(),
                        CommandContext.Platform.VELOCITY
                    );
                    command.execute(context);
                }

                @Override
                public List<String> suggest(Invocation invocation) {
                    // Obsługa Tab Completion dla subkomend
                    if (command instanceof CommandHandler) {
                        CommandContext context = new CommandContext(
                            invocation.source(),
                            invocation.arguments(),
                            command.getName(),
                            CommandContext.Platform.VELOCITY
                        );
                        return ((CommandHandler) command).getSubCommandNames(context);
                    }
                    return Collections.emptyList();
                }

                @Override
                public boolean hasPermission(Invocation invocation) {
                    // Sprawdzanie uprawnień głównej komendy
                    if (command.getPermission() == null) return true;
                    return invocation.source().hasPermission(command.getPermission());
                }
            };

            // Budowanie metadanych (aliasy)
            CommandMeta meta = cmdMgr.metaBuilder(command.getName())
                    .aliases(command.getAliases())
                    .build();

            // Rejestracja w Velocity
            cmdMgr.register(meta, velocityCommand);
            System.out.println("[ZCore] Registered Velocity command (SimpleCommand): " + command.getName());

        } catch (Exception e) {
            System.err.println("[ZCore] Error registering Velocity command: " + command.getName());
            e.printStackTrace();
        }
    }
}