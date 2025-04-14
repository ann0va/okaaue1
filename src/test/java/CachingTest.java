import org.hbrs.ooka.uebung1.ProductManagement;
import org.hbrs.ooka.uebung1.cache.HashMapProductCache;
import org.hbrs.ooka.uebung1.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

public class CachingTest {
    private ProductManagement productManagement;
    private HashMapProductCache cache;

    @BeforeEach
    public void setup() {
        cache = new HashMapProductCache();
        productManagement = new ProductManagement(cache);
        productManagement.openSession();
    }

    @Test
    public void testCaching() {
        // Create a test product
        Product product = new Product(0, "Test Product", 99.99);
        productManagement.createProduct(product);

        // First call should hit the database
        Product firstCall = productManagement.getProductById(product.getId());
        assertNotNull(firstCall);
        assertTrue(cache.containsProduct(product.getId()));

        // Second call should hit the cache
        Product secondCall = productManagement.getProductById(product.getId());
        assertNotNull(secondCall);
        assertEquals(firstCall, secondCall);

        // Update should invalidate cache
        product.setPrice(149.99);
        productManagement.updateProduct(product);
        assertFalse(cache.containsProduct(product.getId()));

        // Delete should invalidate cache
        productManagement.getProductById(product.getId()); // Cache it again
        assertTrue(cache.containsProduct(product.getId()));
        productManagement.deleteProduct(product.getId());
        assertFalse(cache.containsProduct(product.getId()));
    }

    @Test
    public void testSearchCaching() {
        // Create test products
        Product product1 = new Product(0, "Test Product 1", 99.99);
        Product product2 = new Product(0, "Test Product 2", 149.99);
        productManagement.createProduct(product1);
        productManagement.createProduct(product2);

        // First search should hit database
        String searchTerm = "Test Product";
        List<Product> firstSearch = productManagement.getProductByName(searchTerm);
        assertEquals(2, firstSearch.size());
        assertTrue(cache.containsSearchTerm(searchTerm));

        // Second search should hit cache
        List<Product> secondSearch = productManagement.getProductByName(searchTerm);
        assertEquals(2, secondSearch.size());
        assertEquals(firstSearch, secondSearch);

        // Update should invalidate cache
        product1.setPrice(199.99);
        productManagement.updateProduct(product1);
        assertFalse(cache.containsSearchTerm(searchTerm));
    }
} 