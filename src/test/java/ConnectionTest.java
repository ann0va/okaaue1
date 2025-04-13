import org.hbrs.ooka.uebung1.DatabaseConnection;
import org.hbrs.ooka.uebung1.ProductManagement;
import org.hbrs.ooka.uebung1.entities.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ConnectionTest {
    private ProductManagement productManagement;
    private boolean sessionOpened = false;

    @BeforeEach
    public void setup() {
        productManagement = new ProductManagement();
        productManagement.openSession();
        sessionOpened = true;
    }

    @AfterEach
    public void cleanup() {
        // Only try to close if a session was successfully opened
        if (productManagement != null && sessionOpened) {
            try {
                productManagement.closeSession();
            } catch (IllegalStateException e) {
                // Log the error but don't fail the test
                System.err.println("Warning: " + e.getMessage());
            }
        }
    }

    @Test
    public void testCompleteRoundTrip() {
        // 1. Create a new product
        Product newProduct = new Product(0, "Test Product", 99.99);
        productManagement.createProduct(newProduct);
        assertTrue("Product ID should be set after creation", newProduct.getId() > 0);

        // 2. Read the product by ID
        Product retrievedById = productManagement.getProductById(newProduct.getId());
        assertNotNull("Product should be retrieved by ID", retrievedById);
        assertEquals("Product name should match", newProduct.getName(), retrievedById.getName());
        assertEquals("Product price should match", newProduct.getPrice(), retrievedById.getPrice(), 0.001);

        // 3. Read the product by name
        List<Product> retrievedByName = productManagement.getProductByName("Test Product");
        assertFalse("Product list should not be empty", retrievedByName.isEmpty());
        assertEquals("Should find one product", 1, retrievedByName.size());
        assertEquals("Product should match", newProduct.getName(), retrievedByName.get(0).getName());

        // 4. Update the product
        retrievedById.setPrice(149.99);
        retrievedById.setName("Updated Test Product");
        productManagement.updateProduct(retrievedById);

        // 5. Verify the update
        Product updatedProduct = productManagement.getProductById(retrievedById.getId());
        assertEquals("Product name should be updated", "Updated Test Product", updatedProduct.getName());
        assertEquals("Product price should be updated", 149.99, updatedProduct.getPrice(), 0.001);

        // 6. Get all products and verify the product is in the list
        List<Product> allProducts = productManagement.getAllProducts();
        assertFalse("Product list should not be empty", allProducts.isEmpty());
        assertTrue("Updated product should be in the list", 
                  allProducts.stream().anyMatch(p -> p.getId() == newProduct.getId()));

        // 7. Delete the product
        productManagement.deleteProduct(newProduct.getId());

        // 8. Verify deletion
        Product deletedProduct = productManagement.getProductById(newProduct.getId());
        assertNull("Product should be null after deletion", deletedProduct);
    }

    @Test
    public void testSessionManagement() {
        // Test double open session
        assertThrows(IllegalStateException.class, () -> productManagement.openSession());

        // Test operations without session (after closing)
        productManagement.closeSession();
        sessionOpened = false; // Mark session as closed
        assertThrows(IllegalStateException.class, () -> productManagement.getAllProducts());

        // Don't test double close here as session is already closed
    }

    @Test
    public void testProductOperations() {
        // Test creating multiple products and retrieving them
        Product product1 = new Product(0, "Product 1", 10.0);
        Product product2 = new Product(0, "Product 2", 20.0);
        
        productManagement.createProduct(product1);
        productManagement.createProduct(product2);
        
        List<Product> allProducts = productManagement.getAllProducts();
        assertEquals("Should have created 2 products", 2, allProducts.size());
        
        // Test searching by partial name
        List<Product> searchResults = productManagement.getProductByName("Product");
        assertEquals("Should find both products", 2, searchResults.size());
        
        // Clean up
        productManagement.deleteProduct(product1.getId());
        productManagement.deleteProduct(product2.getId());
    }
}