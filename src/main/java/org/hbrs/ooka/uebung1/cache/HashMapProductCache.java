package org.hbrs.ooka.uebung1.cache;

import org.hbrs.ooka.uebung1.entities.Product;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HashMapProductCache implements Caching {
    private final Map<Integer, Product> productCache;
    private final Map<String, List<Product>> searchCache;
    
    public HashMapProductCache() {
        this.productCache = new HashMap<>();
        this.searchCache = new HashMap<>();
    }
    
    @Override
    public void cacheProduct(int id, Product product) {
        productCache.put(id, product);
    }
    
    @Override
    public void cacheProductList(String searchTerm, List<Product> products) {
        searchCache.put(searchTerm.toLowerCase(), products);
    }
    
    @Override
    public Optional<Product> getProduct(int id) {
        return Optional.ofNullable(productCache.get(id));
    }
    
    @Override
    public Optional<List<Product>> getProductList(String searchTerm) {
        return Optional.ofNullable(searchCache.get(searchTerm.toLowerCase()));
    }
    
    @Override
    public void invalidateProduct(int id) {
        productCache.remove(id);
        // Also remove this product from any cached search results
        searchCache.values().forEach(list -> 
            list.removeIf(product -> product.getId() == id)
        );
    }
    
    @Override
    public void invalidateSearchTerm(String searchTerm) {
        searchCache.remove(searchTerm.toLowerCase());
    }
    
    @Override
    public void clearCache() {
        productCache.clear();
        searchCache.clear();
    }
    
    @Override
    public boolean containsProduct(int id) {
        return productCache.containsKey(id);
    }
    
    @Override
    public boolean containsSearchTerm(String searchTerm) {
        return searchCache.containsKey(searchTerm.toLowerCase());
    }
} 