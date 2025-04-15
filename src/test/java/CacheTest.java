import org.hbrs.ooka.uebung1.cache.Caching;
import org.hbrs.ooka.uebung1.cache.HashMapProductCache;
import org.hbrs.ooka.uebung1.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CacheTest {
    private Caching cache;

    @BeforeEach
    public void setup() {
        cache = new HashMapProductCache();
    }

    @Test
    public void testProductCaching() {
        Product product = new Product(1, "Test Product", 99.99);
        
        // Cache product
        cache.cacheProduct(1, product);
        
        // Verify cache hit
        Optional<Product> cached = cache.getProduct(1);
        assertTrue(cached.isPresent());
        assertEquals(product.getName(), cached.get().getName());
        
        // Test invalidation
        cache.invalidateProduct(1);
        cached = cache.getProduct(1);
        assertFalse(cached.isPresent());
    }

    @Test
    public void testSearchResultCaching() {
        List<Product> products = Arrays.asList(
            new Product(1, "Product 1", 99.99),
            new Product(2, "Product 2", 149.99)
        );
        
        // Cache search results
        cache.cacheProductList("test", products);
        
        // Verify cache hit
        Optional<List<Product>> cached = cache.getProductList("test");
        assertTrue(cached.isPresent());
        assertEquals(2, cached.get().size());
        
        // Test case insensitivity
        cached = cache.getProductList("TEST");
        assertTrue(cached.isPresent());
        
        // Test invalidation
        cache.invalidateSearchTerm("test");
        cached = cache.getProductList("test");
        assertFalse(cached.isPresent());
    }
} 