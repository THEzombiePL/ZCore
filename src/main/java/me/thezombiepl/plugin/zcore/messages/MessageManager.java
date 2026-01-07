package me.thezombiepl.plugin.zcore.messages;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import me.thezombiepl.plugin.zcore.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageManager {

    private final JavaPlugin plugin;
    private YamlDocument messages;
    private String currentLanguage;
    private final String defaultLang;
    private final File dataFolder;
    private final InputStreamProvider streamProvider; // Nowy interfejs

    // Interfejs do pobierania InputStream (żeby nie zależeć od konkretnej implementacji)
    @FunctionalInterface
    public interface InputStreamProvider {
        InputStream getStream(String fileName) throws IOException;
    }

    // Konstruktor dla Bukkit
    public MessageManager(JavaPlugin plugin, ConfigManager configManager, String defaultLang) throws IOException {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.defaultLang = defaultLang;
        this.currentLanguage = configManager.getConfig().getString("settings.language", defaultLang);
        this.streamProvider = fileName -> plugin.getResource("messages/" + fileName);
        loadMessages();
    }

    // Konstruktor dla Velocity z własnym providerem
    public MessageManager(File dataFolder, ConfigManager configManager, String defaultLang, InputStreamProvider streamProvider) throws IOException {
        this.plugin = null;
        this.dataFolder = dataFolder;
        this.defaultLang = defaultLang;
        this.currentLanguage = configManager.getConfig().getString("settings.language", defaultLang);
        this.streamProvider = streamProvider;
        loadMessages();
    }

    private void loadMessages() throws IOException {
		File messageDir = new File(dataFolder, "messages");
		if (!messageDir.exists() && !messageDir.mkdirs()) {
			throw new IOException("Cannot create messages folder: " + messageDir.getAbsolutePath());
		}

		String fileName = "messages_" + currentLanguage + ".yml";
		File messageFile = new File(messageDir, fileName);

		// Pobierz stream z providera (może być z pluginu lub z zewnątrz)
		InputStream defaultsForCopy = streamProvider.getStream(fileName);

		if (!messageFile.exists()) {
			if (defaultsForCopy != null) {
				Files.copy(defaultsForCopy, messageFile.toPath());
			} else {
				messageFile.createNewFile();
			}
		}

		// Ponownie pobierz stream dla updatera (poprzedni mógł być zużyty)
		InputStream defaultsForUpdater = streamProvider.getStream(fileName);

		if (defaultsForUpdater != null) {
			try {
				// Mamy domyślny plik + versioning
				this.messages = YamlDocument.create(
						messageFile,
						defaultsForUpdater,
						GeneralSettings.DEFAULT,
						LoaderSettings.builder().setAutoUpdate(true).build(),
						DumperSettings.DEFAULT,
						UpdaterSettings.builder()
								.setVersioning(new BasicVersioning("messages-version"))
								.build()
				);
			} catch (NullPointerException e) {
				// Fallback: brak wersji w defaults (np. na Velocity), tworzymy bez versioning
				defaultsForUpdater = streamProvider.getStream(fileName);
				this.messages = YamlDocument.create(
						messageFile,
						defaultsForUpdater,
						GeneralSettings.DEFAULT,
						LoaderSettings.builder().setAutoUpdate(true).build(),
						DumperSettings.DEFAULT,
						UpdaterSettings.DEFAULT
				);
			}
		} else {
			// Nie mamy defaults -> używamy wersji create() BEZ InputStream
			this.messages = YamlDocument.create(
					messageFile,
					GeneralSettings.DEFAULT,
					LoaderSettings.DEFAULT,
					DumperSettings.DEFAULT,
					UpdaterSettings.DEFAULT
			);
		}

		if (plugin != null) {
			plugin.getLogger().info("Loaded language file: " + fileName);
		} else {
			System.out.println("[ZCore] Loaded language file: " + fileName);
		}
	}

    public String getMessage(String key, String defaultMsg) {
        return messages.getString(key, defaultMsg);
    }

    public String getMessage(String key, String defaultMsg, Map<String, String> placeholders) {
        String msg = getMessage(key, defaultMsg);
        for (Map.Entry<String, String> e : placeholders.entrySet())
            msg = msg.replace("{" + e.getKey() + "}", e.getValue());
        return msg;
    }

    public List<String> getMessageList(String key, List<String> defaultList) {
        return messages.contains(key) ? messages.getStringList(key) : defaultList;
    }

    public List<String> getMessageList(String key, List<String> defaultList, Map<String, String> placeholders) {
        return getMessageList(key, defaultList).stream()
                .map(line -> {
                    for (Map.Entry<String, String> e : placeholders.entrySet())
                        line = line.replace("{" + e.getKey() + "}", e.getValue());
                    return line;
                }).collect(Collectors.toList());
    }

    public void save() throws IOException {
        messages.save();
    }

    public void reload(ConfigManager configManager) throws IOException {
        String langFromConfig = configManager.getConfig().getString("settings.language", defaultLang);
        if (!langFromConfig.equals(currentLanguage)) {
            currentLanguage = langFromConfig;
            messages.save();
            loadMessages();
            if (plugin != null) {
                plugin.getLogger().info("Reloaded language file after changing language to: " + currentLanguage);
            } else {
                System.out.println("[ZCore] Reloaded language file after changing language to: " + currentLanguage);
            }
        } else {
            messages.reload();
            if (plugin != null) {
                plugin.getLogger().info("Reloaded language file without changing language");
            } else {
                System.out.println("[ZCore] Reloaded language file without changing language");
            }
        }
    }
}