package me.thezombiepl.plugin.zcore.messages;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;
import me.thezombiepl.plugin.zcore.config.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manager do zarządzania wielojęzycznymi wiadomościami.
 * <p>
 * Automatycznie ładuje odpowiedni plik językowy na podstawie ustawień w konfiguracji.
 * Wspiera placeholdery w formacie {klucz} oraz wersjonowanie plików językowych.
 * </p>
 * 
 * <p>
 * Przykładowa struktura pliku językowego:
 * <pre>
 * messages-version: 1
 * 
 * prefix: "&lt;green&gt;[MojPlugin]&lt;/green&gt;"
 * messages:
 *   welcome: "{prefix} Witaj {player}!"
 *   error: "{prefix} &lt;red&gt;Wystąpił błąd!&lt;/red&gt;"
 * </pre>
 * </p>
 * 
 * @author TheZombiePL
 * @version 1.0.0
 */
public class MessageManager {

    private final JavaPlugin plugin;
    private YamlDocument messages;
    private String currentLanguage;
    private final String defaultLang;

    /**
     * Tworzy nowy MessageManager i ładuje plik językowy.
     * <p>
     * Język jest pobierany z konfiguracji pod kluczem "settings.language".
     * Jeśli plik językowy nie istnieje, zostanie skopiowany z resources pluginu.
     * </p>
     * 
     * @param plugin Instancja pluginu Bukkit/Spigot
     * @param configManager Manager konfiguracji (do odczytu wybranego języka)
     * @param defaultLang Domyślny język (np. "pl", "en") używany jako fallback
     * @throws IOException Jeśli wystąpi błąd podczas ładowania pliku językowego
     */
    public MessageManager(JavaPlugin plugin, ConfigManager configManager, String defaultLang) throws IOException {
        this.plugin = plugin;
        this.defaultLang = defaultLang;
        this.currentLanguage = configManager.getConfig().getString("settings.language", defaultLang);
        loadMessages();
    }

    /**
     * Ładuje plik językowy z dysku lub kopiuje domyślny z resources.
     * <p>
     * Metoda wewnętrzna wywoływana przez konstruktor i {@link #reload(ConfigManager)}.
     * </p>
     * 
     * @throws IOException Jeśli brakuje domyślnego pliku w JAR lub wystąpi błąd I/O
     */
    private void loadMessages() throws IOException {
        File messageDir = new File(plugin.getDataFolder(), "messages");
        if (!messageDir.exists()) messageDir.mkdirs();

        String fileName = "messages_" + currentLanguage + ".yml";
        File messageFile = new File(messageDir, fileName);

        // Skopiuj domyślny plik jeśli nie istnieje
        try (InputStream baseResource = plugin.getResource("messages/messages_" + defaultLang + ".yml")) {
            if (baseResource == null)
                throw new IOException("Brakuje domyślnego pliku messages_" + defaultLang + ".yml w JAR!");
            if (!messageFile.exists()) Files.copy(baseResource, messageFile.toPath());
        }

        InputStream resourceForYaml = plugin.getResource("messages/messages_" + defaultLang + ".yml");
        if (resourceForYaml == null)
            throw new IOException("Brakuje domyślnego pliku messages_" + defaultLang + ".yml w JAR!");

        this.messages = YamlDocument.create(
                messageFile,
                resourceForYaml,
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("messages-version")).build()
        );

        plugin.getLogger().info("Załadowano plik językowy: " + fileName);
    }

    /**
     * Pobiera wiadomość z pliku językowego.
     * <p>
     * Przykład użycia:
     * <pre>{@code
     * String welcome = messageManager.getMessage("messages.welcome", "Witaj!");
     * }</pre>
     * </p>
     * 
     * @param key Klucz wiadomości w notacji kropkowej (np. "messages.welcome")
     * @param defaultMsg Domyślna wiadomość zwracana jeśli klucz nie istnieje
     * @return Wiadomość z pliku językowego lub defaultMsg
     */
    public String getMessage(String key, String defaultMsg) {
        return messages.getString(key, defaultMsg);
    }

    /**
     * Pobiera wiadomość z podmianą placeholderów.
     * <p>
     * Placeholdery w wiadomości muszą być w formacie {klucz}.
     * Przykład użycia:
     * <pre>{@code
     * Map<String, String> placeholders = new HashMap<>();
     * placeholders.put("player", "Steve");
     * placeholders.put("amount", "100");
     * 
     * // Jeśli w pliku: "Gracz {player} otrzymał {amount} monet"
     * String msg = messageManager.getMessage("messages.reward", "Error", placeholders);
     * // Wynik: "Gracz Steve otrzymał 100 monet"
     * }</pre>
     * </p>
     * 
     * @param key Klucz wiadomości w notacji kropkowej
     * @param defaultMsg Domyślna wiadomość jeśli klucz nie istnieje
     * @param placeholders Mapa placeholderów do podstawienia (klucz bez nawiasów klamrowych)
     * @return Wiadomość z podstawionymi placeholderami
     */
    public String getMessage(String key, String defaultMsg, Map<String, String> placeholders) {
        String msg = getMessage(key, defaultMsg);
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            msg = msg.replace("{" + e.getKey() + "}", e.getValue());
        }
        return msg;
    }

    /**
     * Pobiera listę wiadomości z pliku językowego.
     * <p>
     * Użyteczne dla wiadomości wieloliniowych (np. help, MOTD).
     * Przykład użycia:
     * <pre>{@code
     * List<String> helpLines = messageManager.getMessageList(
     *     "help.commands", 
     *     Arrays.asList("Brak pomocy")
     * );
     * }</pre>
     * </p>
     * 
     * @param key Klucz listy w notacji kropkowej
     * @param defaultList Domyślna lista zwracana jeśli klucz nie istnieje
     * @return Lista wiadomości z pliku językowego lub defaultList
     */
    public List<String> getMessageList(String key, List<String> defaultList) {
        return messages.contains(key) ? messages.getStringList(key) : defaultList;
    }

    /**
     * Pobiera listę wiadomości z podmianą placeholderów.
     * <p>
     * Każda linia w liście będzie miała podstawione placeholdery.
     * Przykład użycia:
     * <pre>{@code
     * Map<String, String> placeholders = new HashMap<>();
     * placeholders.put("version", "1.0.0");
     * placeholders.put("author", "Steve");
     * 
     * List<String> info = messageManager.getMessageList(
     *     "info.plugin", 
     *     Arrays.asList("Brak informacji"),
     *     placeholders
     * );
     * }</pre>
     * </p>
     * 
     * @param key Klucz listy w notacji kropkowej
     * @param defaultList Domyślna lista jeśli klucz nie istnieje
     * @param placeholders Mapa placeholderów do podstawienia w każdej linii
     * @return Lista wiadomości z podstawionymi placeholderami
     */
    public List<String> getMessageList(String key, List<String> defaultList, Map<String, String> placeholders) {
        return getMessageList(key, defaultList).stream()
                .map(line -> {
                    for (Map.Entry<String, String> e : placeholders.entrySet())
                        line = line.replace("{" + e.getKey() + "}", e.getValue());
                    return line;
                }).collect(Collectors.toList());
    }

    /**
     * Zapisuje aktualny stan pliku językowego na dysk.
     * <p>
     * Rzadko używane - pliki językowe zazwyczaj nie są modyfikowane w runtime.
     * </p>
     * 
     * @throws IOException Jeśli wystąpi błąd podczas zapisu do pliku
     */
    public void save() throws IOException {
        messages.save();
    }

    /**
     * Przeładowuje plik językowy, opcjonalnie zmieniając język.
     * <p>
     * Jeśli język w konfiguracji uległ zmianie, zostanie załadowany nowy plik językowy.
     * W przeciwnym razie tylko odświeża zawartość aktualnego pliku.
     * </p>
     * 
     * <p>
     * Przykład użycia:
     * <pre>{@code
     * configManager.set("settings.language", "en");
     * messageManager.reload(configManager); // Załaduje messages_en.yml
     * }</pre>
     * </p>
     * 
     * @param configManager Manager konfiguracji (do odczytu języka)
     * @throws IOException Jeśli wystąpi błąd podczas ładowania pliku
     */
    public void reload(ConfigManager configManager) throws IOException {
        String langFromConfig = configManager.getConfig().getString("settings.language", defaultLang);
        if (!langFromConfig.equals(currentLanguage)) {
            currentLanguage = langFromConfig;
            messages.save();
            loadMessages();
            plugin.getLogger().info("Przeładowano plik językowy po zmianie języka na: " + currentLanguage);
        } else {
            messages.reload();
            plugin.getLogger().info("Przeładowano plik językowy bez zmiany języka");
        }
    }
}