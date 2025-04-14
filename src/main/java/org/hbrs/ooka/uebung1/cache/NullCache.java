package org.hbrs.ooka.uebung1.cache;

import org.hbrs.ooka.uebung1.entities.Product;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

/**
 * Implementierung des Null Object Pattern für den Cache.
 * Stellt sicher, dass keine NullPointerExceptions auftreten, auch wenn kein Cache konfiguriert ist.
 */
public class NullCache implements Caching {
    
    @Override
    public void cacheProduct(int id, Product product) {
        // Keine Operation - silent fail
    }
    
    @Override
    public void cacheProductList(String searchTerm, List<Product> products) {
        // Keine Operation - silent fail
    }
    
    @Override
    public Optional<Product> getProduct(int id) {
        return Optional.empty();
    }
    
    @Override
    public Optional<List<Product>> getProductList(String searchTerm) {
        return Optional.of(Collections.emptyList());
    }
    
    @Override
    public void invalidateProduct(int id) {
        // Keine Operation - silent fail
    }
    
    @Override
    public void invalidateSearchTerm(String searchTerm) {
        // Keine Operation - silent fail
    }
    
    @Override
    public void clearCache() {
        // Keine Operation - silent fail
    }
    
    @Override
    public boolean containsProduct(int id) {
        return false;
    }
    
    @Override
    public boolean containsSearchTerm(String searchTerm) {
        return false;
    }
} 