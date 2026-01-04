package me.thezombiepl.plugin.zcore.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Manager do zarządzania plikami konfiguracyjnymi używając BoostedYAML.
 * <p>
 * Automatycznie tworzy plik konfiguracyjny jeśli nie istnieje oraz obsługuje
 * wersjonowanie i auto-update konfiguracji.
 * </p>
 * 
 * @author TheZombiePL
 * @version 1.0.0
 */
public class ConfigManager {

    private final YamlDocument config;

    /**
     * Tworzy nowy ConfigManager i ładuje plik konfiguracyjny.
     * <p>
     * Jeśli plik nie istnieje, zostanie automatycznie skopiowany z resources pluginu.
     * Wspiera auto-update konfiguracji na podstawie klucza "config-version".
     * </p>
     * 
     * @param plugin Instancja pluginu Bukkit/Spigot
     * @param fileName Nazwa pliku konfiguracyjnego (np. "config.yml")
     * @throws IOException Jeśli wystąpi błąd podczas ładowania lub tworzenia pliku
     */
    public ConfigManager(JavaPlugin plugin, String fileName) throws IOException {
        File configFile = new File(plugin.getDataFolder(), fileName);

        this.config = YamlDocument.create(
                configFile,
                plugin.getResource(fileName),
                GeneralSettings.DEFAULT,
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build()
        );
    }

    /**
     * Pobiera obiekt YamlDocument do bezpośredniego dostępu.
     * <p>
     * Użyj tej metody jeśli potrzebujesz zaawansowanych operacji na konfiguracji,
     * które nie są dostępne przez proste gettery/settery.
     * </p>
     * 
     * @return Obiekt YamlDocument reprezentujący konfigurację
     */
    public YamlDocument getConfig() {
        return config;
    }

    /**
     * Przeładowuje konfigurację z pliku.
     * <p>
     * Wszystkie zmiany dokonane w pamięci które nie zostały zapisane zostaną utracone.
     * </p>
     * 
     * @throws IOException Jeśli wystąpi błąd podczas odczytu pliku
     */
    public void reload() throws IOException {
        config.reload();
    }

    /**
     * Zapisuje aktualny stan konfiguracji do pliku.
     * <p>
     * Wszystkie zmiany dokonane metodą {@link #set(String, Object)} będą zapisane na dysku.
     * </p>
     * 
     * @throws IOException Jeśli wystąpi błąd podczas zapisu do pliku
     */
    public void save() throws IOException {
        config.save();
    }

    /**
     * Pobiera wartość z konfiguracji pod podaną ścieżką.
     * <p>
     * Przykład użycia:
     * <pre>{@code
     * String name = (String) configManager.get("settings.server-name");
     * int maxPlayers = (Integer) configManager.get("settings.max-players");
     * }</pre>
     * </p>
     * 
     * @param path Ścieżka do wartości w notacji kropkowej (np. "settings.language")
     * @return Wartość pod podaną ścieżką lub null jeśli nie istnieje
     */
    public Object get(String path) {
        return config.get(path);
    }

    /**
     * Ustawia wartość w konfiguracji i automatycznie zapisuje do pliku.
     * <p>
     * Przykład użycia:
     * <pre>{@code
     * configManager.set("settings.language", "pl");
     * configManager.set("settings.enabled", true);
     * }</pre>
     * </p>
     * 
     * @param path Ścieżka do wartości w notacji kropkowej
     * @param value Nowa wartość do ustawienia
     * @throws IOException Jeśli wystąpi błąd podczas zapisu do pliku
     */
    public void set(String path, Object value) throws IOException {
        config.set(path, value);
        config.save();
    }
}