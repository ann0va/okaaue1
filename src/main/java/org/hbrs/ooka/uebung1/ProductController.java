package org.hbrs.ooka.uebung1;

import org.hbrs.ooka.uebung1.cache.CacheFactory;
import org.hbrs.ooka.uebung1.cache.Caching;
import org.hbrs.ooka.uebung1.entities.Product;
import java.util.List;
import java.util.Optional;

public class ProductController {
    private final ProductManagementInt productService;
    private final Caching cache;
    
    public ProductController(ProductManagementInt productService) {
        this(productService, null);
    }
    
    public ProductController(ProductManagementInt productService, Caching specificCache) {
        this.productService = productService;
        this.cache = CacheFactory.getCache(specificCache);
    }
    
    public Product getProduct(int id) {
        // Cache-Zugriff ist immer sicher
        Optional<Product> cachedProduct = cache.getProduct(id);
        if (cachedProduct.isPresent()) {
            return cachedProduct.get();
        }
        
        // Wenn nicht im Cache, hole es aus dem Service
        Product product = productService.getProductById(id);
        if (product != null) {
            cache.cacheProduct(id, product);
        }
        return product;
    }
    
    public List<Product> searchProducts(String name) {
        // Cache-Zugriff ist immer sicher
        Optional<List<Product>> cachedProducts = cache.getProductList(name);
        if (cachedProducts.isPresent()) {
            return cachedProducts.get();
        }
        
        // Wenn nicht im Cache, hole es aus dem Service
        List<Product> products = productService.getProductByName(name);
        cache.cacheProductList(name, products);
        return products;
    }
    
    public void updateProduct(Product product) {
        productService.updateProduct(product);
        cache.invalidateProduct(product.getId());
        cache.invalidateSearchTerm(product.getName());
    }
    
    public void deleteProduct(int id) {
        Product product = getProduct(id);
        if (product != null) {
            productService.deleteProduct(id);
            cache.invalidateProduct(id);
            cache.invalidateSearchTerm(product.getName());
        }
    }
} 