package me.thezombiepl.plugin.zcore.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaperCommandRegistrar {

    public static void register(Object pluginObj, UniversalCommand command) {
        JavaPlugin plugin = (JavaPlugin) pluginObj;
        
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            Command paperCommand = new Command(
                command.getName(),
                command.getDescription(),
                "", // Pusty usage zapobiega wysyłaniu domyślnej wiadomości przez silnik
                Arrays.asList(command.getAliases())
            ) {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    try {
                        // Sprawdzenie uprawnień dla głównej komendy
                        if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
                            String errorMsg = "&cYou don't have permission!";
                            if (command instanceof CommandHandler) {
                                CommandContext dummy = new CommandContext(sender, args, label, CommandContext.Platform.PAPER);
                                errorMsg = ((CommandHandler) command).getNoPermissionMessage(dummy);
                            }
                            sender.sendMessage(me.thezombiepl.plugin.zcore.utils.ColorUtil.serialize(
                                me.thezombiepl.plugin.zcore.utils.ColorUtil.colorize(errorMsg)
                            ));
                            return true;
                        }

                        // Wykonanie komendy
                        command.execute(new CommandContext(sender, args, label, CommandContext.Platform.PAPER));
                        
                        // Zawsze zwracamy true, ponieważ CommandHandler sam obsługuje wiadomości o błędnym użyciu
                        return true;
                    } catch (Exception e) {
                        sender.sendMessage("§cWystąpił błąd wewnętrzny podczas wykonywania tej komendy.");
                        e.printStackTrace();
                        return true;
                    }
                }

                @Override
				public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
					if (!(command instanceof CommandHandler)) return new ArrayList<>();
					
					CommandContext context = new CommandContext(sender, args, alias, CommandContext.Platform.PAPER);
					List<String> suggestions = ((CommandHandler) command).getSubCommandNames(context);

					// Jeśli wpisujemy pierwszy argument (np. /zguard [tab])
					if (args.length <= 1) {
						String lastArg = args.length == 1 ? args[0].toLowerCase() : "";
						List<String> filtered = new ArrayList<>();
						for (String s : suggestions) {
							if (s.toLowerCase().startsWith(lastArg)) filtered.add(s);
						}
						return filtered;
					}
					
					// Jeśli wpisujemy kolejne argumenty, zwracamy pustą listę, 
					// co pozwoli na wpisywanie dowolnego tekstu (nie blokuje Brigadiera)
					return new ArrayList<>();
				}
            };

            // Rejestracja komendy w mapie serwera
            commandMap.register(plugin.getName().toLowerCase(), paperCommand);
            plugin.getLogger().info("Registered Bukkit command: " + command.getName());

        } catch (Exception e) {
            plugin.getLogger().severe("Could not register command: " + command.getName());
            e.printStackTrace();
        }
    }
}