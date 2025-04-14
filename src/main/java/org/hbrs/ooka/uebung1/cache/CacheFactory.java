package org.hbrs.ooka.uebung1.cache;

/**
 * Factory für Cache-Instanzen mit eingebautem Fallback auf NullCache
 */
public class CacheFactory {
    private static final NullCache NULL_CACHE = new NullCache();
    private static Caching defaultCache = NULL_CACHE;
    
    /**
     * Setzt den Standard-Cache für die Anwendung
     */
    public static void setDefaultCache(Caching cache) {
        defaultCache = cache != null ? cache : NULL_CACHE;
    }
    
    /**
     * Liefert eine Cache-Instanz zurück.
     * Wenn keine spezifische Instanz übergeben wird, wird der Standard-Cache verwendet.
     * Wenn kein Standard-Cache konfiguriert ist, wird der NullCache verwendet.
     */
    public static Caching getCache(Caching specificCache) {
        return specificCache != null ? specificCache : defaultCache;
    }
    
    /**
     * Liefert den Standard-Cache zurück
     */
    public static Caching getDefaultCache() {
        return defaultCache;
    }
    
    /**
     * Setzt den Cache auf den NullCache zurück
     */
    public static void resetToNullCache() {
        defaultCache = NULL_CACHE;
    }
} 