# Komponenten-Ports in der Softwarearchitektur

## Überblick
Dieses Repository demonstriert das Konzept und die Implementierung von Ports in Komponentendiagrammen mit Java, am Beispiel eines ProductManagement-Systems.

## Komponentendiagramm
![ProductManagement Component Diagram](img/Picture1.png)
![Port-Konfigurationsdiagramm](img/Picture2.png)

## Port-Architektur :: FA0
Das System implementiert drei Hauptports:

1. **PM_Port (ProductManagementInt)**
    - Externes Interface für Client-Zugriffe
    - Definiert Produktverwaltungsmethoden
    - Standardisierte Schnittstelle

2. **Cache_Port (Caching)**
    - Interface für Cache-Operationen
    - Ermöglicht austauschbare Implementierungen

3. **DB_Port (Database)**
    - JDBC-basierte Datenbankanbindung
    - Abstraktion der Datenbankzugriffe

## Lifecycle Management :: FA1

### Methodenreihenfolge
1. `openSession()` - Initialisierung
2. CRUD-Operationen
3. `closeSession()` - Cleanup

### Interface-Definition
```java
public interface ProductManagementInt {
    void openSession();
    void closeSession();
    
    // CRUD Operations
    void createProduct(Product product);
    Product getProductById(int id);
    List<Product> getProductByName(String name);
    List<Product> getAllProducts();
    void updateProduct(Product product);
    void deleteProduct(int id);
}
```

## Cache-Implementierung :: FA2
```java
public interface Caching {
    void cacheProduct(Product product);
    Product getCachedProduct(int id);
    void cacheSearchResult(String searchTerm, List<Product> products);
    List<Product> getCachedSearchResult(String searchTerm);
    void clearCache();
}
```

## Fehlerbehandlung und Null-Safety :: FA3

### Null-Object Pattern für Cache
```java
public class NullSafeCache implements Caching {
    private final Caching actualCache;
    
    public NullSafeCache(Caching actualCache) {
        this.actualCache = actualCache != null ? actualCache : new EmptyCache();
    }
    
    @Override
    public Product getCachedProduct(int id) {
        try {
            return actualCache.getCachedProduct(id);
        } catch (Exception e) {
            return null; // Fail-safe Verhalten
        }
    }
    
    @Override
    public List<Product> getCachedSearchResult(String searchTerm) {
        try {
            return actualCache.getCachedSearchResult(searchTerm);
        } catch (Exception e) {
            return Collections.emptyList(); // Fail-safe Verhalten
        }
    }
}

// Leere Cache-Implementierung als Fallback
private static class EmptyCache implements Caching {
    @Override
    public Product getCachedProduct(int id) {
        return null;
    }
    
    @Override
    public List<Product> getCachedSearchResult(String searchTerm) {
        return Collections.emptyList();
    }
    
    @Override
    public void cacheProduct(Product product) {
        // Keine Operation
    }
    
    @Override
    public void cacheSearchResult(String searchTerm, List<Product> products) {
        // Keine Operation
    }
}
```

### Fehlerbehandlung außerhalb des ProductController
```java
public class ProductManagementFactory {
    public ProductManagementInt createProductManagement(Caching cache) {
        // Basis-Implementierung mit Null-Safety
        Caching safeCaching = new NullSafeCache(cache);
        ProductController controller = new ProductController(safeCaching);
        
        // Fehlerbehandlung durch Decorator
        return new ErrorHandlingDecorator(controller);
    }
}

public class ErrorHandlingDecorator implements ProductManagementInt {
    private final ProductManagementInt wrapped;
    
    @Override
    public Product getProductById(int id) {
        try {
            return wrapped.getProductById(id);
        } catch (Exception e) {
            // Zentrale Fehlerbehandlung
            logError("Error retrieving product", e);
            throw new ProductManagementException("Could not retrieve product: " + id, e);
        }
    }
    
    // Weitere Methoden mit ähnlicher Fehlerbehandlung
}

### Hauptmerkmale
- **Null-Safety**: Kein NullPointerException-Risiko durch NullSafeCache
- **Zentrale Fehlerbehandlung**: Alle Fehler werden im Decorator behandelt
- **Fail-Safe Verhalten**: Leere Ergebnisse statt Exceptions
- **Separation of Concerns**: Fehlerbehandlung getrennt von Geschäftslogik
- **Transparente Fehlerbehandlung**: ProductController bleibt schlank und fokussiert

## Logging-Funktionalität :: FA4
```java
public class LoggingDecorator implements ProductManagementInt {
    private final ProductManagementInt wrapped;
    private final Logger logger;

    @Override
    public List<Product> getProductByName(String name) {
        logger.log(String.format("%s: Zugriff auf ProductManagement über Methode getProductByName. Suchwort: %s",
            getCurrentTimestamp(), name));
        return wrapped.getProductByName(name);
    }
}
```

## Externer Client :: FA5

### Client-Implementierung
```java
public class ExternalClient {
    private final ProductManagementInt productManagement;
    private final CustomCacheImpl cache;

    public void demonstrateProductManagement() {
        try {
            productManagement.openSession();
            
            // Beispieloperationen
            Product product = new Product(1, "Motor A123", 1500.0);
            productManagement.addProduct(product);
            
            List<Product> results = productManagement.getProductsByName("Motor");
            results.forEach(System.out::println);

            productManagement.closeSession();
        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}
```

### Custom Cache
```java
public class CustomCacheImpl implements Caching {
    private final Map<Integer, Product> productCache = new HashMap<>();
    private final Map<String, List<Product>> searchCache = new HashMap<>();
    
    // Cache-Implementierung
}
```

## Technische Details
- Java 8+
- H2 Datenbank
- JDBC für Datenbankzugriffe
- Keine UI-Abhängigkeit erforderlich



```

