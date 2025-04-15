import org.hbrs.ooka.uebung1.ProductManagementInt;
import org.hbrs.ooka.uebung1.entities.Product;
import org.hbrs.ooka.uebung1.logger.LoggingProductManagementFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.Assert.*;

public class LoggingTest {
    private ProductManagementInt productManagement;
    
    @BeforeEach
    public void setup() {
        productManagement = LoggingProductManagementFactory.createLoggingProductManagement();
        productManagement.openSession();
    }
    
    @AfterEach
    public void cleanup() {
        productManagement.closeSession();
    }
    
    @Test
    public void testLogging() throws Exception {
        // Testprodukt erstellen und Operationen ausführen
        Product product = new Product(0, "Test Motor", 999.99);
        productManagement.createProduct(product);
        productManagement.getProductByName("Motor");
        
        // Log-Datei überprüfen
        Path logFile = Paths.get("product_management.log");
        assertTrue("Log file should exist", Files.exists(logFile));
        
        List<String> logLines = Files.readAllLines(logFile);
        assertTrue("Log should contain create operation", 
            logLines.stream().anyMatch(line -> 
                line.contains("createProduct") && line.contains("Test Motor")));
        
        assertTrue("Log should contain search operation", 
            logLines.stream().anyMatch(line -> 
                line.contains("getProductByName") && line.contains("Motor")));
    }
} 