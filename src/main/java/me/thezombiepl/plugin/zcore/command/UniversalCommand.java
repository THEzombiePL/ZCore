package me.thezombiepl.plugin.zcore.command;

/**
 * Uniwersalny interfejs dla komend wspierający Paper i Velocity
 */
public interface UniversalCommand {
    
    /**
     * Nazwa głównej komendy
     */
    String getName();
    
    /**
     * Aliasy komendy (opcjonalne)
     */
    default String[] getAliases() {
        return new String[0];
    }
    
    /**
     * Wymagane uprawnienie do użycia komendy (opcjonalne)
     */
    default String getPermission() {
        return null;
    }
    
    /**
     * Opis komendy
     */
    default String getDescription() {
        return "";
    }
    
    /**
     * Wykonuje komendę
     * 
     * @param context Kontekst wykonania komendy
     * @return true jeśli komenda wykonana pomyślnie
     */
    boolean execute(CommandContext context);
}