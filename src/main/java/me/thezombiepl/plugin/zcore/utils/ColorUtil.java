package me.thezombiepl.plugin.zcore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Narzędzie do kolorowania tekstu z wsparciem dla wielu wersji Minecraft.
 * Automatycznie wykrywa czy serwer wspiera kolory HEX (1.16+) i dostosowuje się.
 */
public class ColorUtil {
    
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer serializer;

    static {
        // Sprawdzamy, czy serwer wspiera HEX (wersja 1.16+)
        boolean supportsHex = false;
        try {
            // Metoda 'of' w ChatColor została dodana w 1.16 (Bungee API w Spigocie)
            Class.forName("net.md_5.bungee.api.ChatColor").getMethod("of", String.class);
            supportsHex = true;
        } catch (Exception e) {
            supportsHex = false;
        }

        if (supportsHex) {
            // DLA 1.16+: Zachowujemy kolory RGB
            // Używamy formatu '§x§r§r§g§g§b§b', który Bukkit 1.16+ rozumie natywnie
            serializer = LegacyComponentSerializer.builder()
                    .character('§')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();
        } else {
            // DLA 1.8 - 1.15: Konwertujemy RGB na najbliższy zwykły kolor
            serializer = LegacyComponentSerializer.legacySection();
        }
    }

    /**
     * Koloruje tekst używając MiniMessage i legacy codes.
     * Wspiera zarówno stare kody kolorów (&amp;c, §c) jak i MiniMessage (&lt;red&gt;).
     * 
     * @param message Tekst do pokolorowania (może być null)
     * @return Kolorowany Component lub null jeśli message był null
     */
    public static Component colorize(String message) {
        if (message == null) return null;
        // Najpierw zamieniamy stare & na §, żeby legacySerializer to zrozumiał
        String fixedMessage = message.replace('§', '&');
        
        // Deserialize pozwala zamienić &c na Component
        Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(fixedMessage);
        
        // Serializujemy do MiniMessage, czyścimy escape'y i deserializujemy ponownie
        // To pozwala łączyć &c z <red> w jednej wiadomości
        String minimessage = miniMessage.serialize(legacy).replace("\\", "");
        return miniMessage.deserialize(minimessage);
    }

    /**
     * Inteligentnie zamienia Component na String.
     * <p>
     * Na Minecraft 1.16+ zwraca tekst z kodami HEX (format §x§r§r§g§g§b§b).
     * <br>
     * Na Minecraft 1.8-1.15 zwraca tekst ze zwykłymi kolorami (§c, §a, itp).
     * </p>
     * 
     * @param component Component do serializacji (może być null)
     * @return Zserializowany tekst lub pusty string jeśli component był null
     */
    public static String serialize(Component component) {
        if (component == null) return "";
        return serializer.serialize(component);
    }
}