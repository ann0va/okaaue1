# Komponenten-Ports in der Softwarearchitektur

## Überblick
Dieses Repository demonstriert das Konzept und die Implementierung von Ports in Komponentendiagrammen mit Java, am Beispiel eines ProductManagement-Systems. Das System zeigt, wie Ports die strukturierte Kommunikation zwischen Komponenten, externen Systemen und Datenbanken ermöglichen.

## Komponentendiagramm
Das System besteht aus zwei Hauptdiagrammen:

### 1. Strukturdiagramm

![ProductManagement Component Diagram](img/Picture1.png)

### 2. Port-Konfigurationsdiagramm
![Port-Konfigurationsdiagramm](img/Picture2.png)


## Was sind Ports?
Ports in Komponentendiagrammen dienen als klar definierte Interaktionsschnittstellen zwischen verschiedenen Systemteilen. In unserem ProductManagement-System haben wir drei Hauptports:

1. **PM_Port (ProductManagementInt)**
    - Angebotenes Interface für externe Clients
    - Definiert Methoden wie `getProductByName` und `openSession`
    - Ermöglicht standardisierte Produktverwaltung

2. **Cache_Port (Caching)**
    - Benötigtes Interface für Caching-Operationen
    - Standardisiert Cache-Zugriffe
    - Ermöglicht austauschbare Cache-Implementierungen

3. **DB_Port (Database)**
    - Verbindung zur Datenbank über JDBC
    - Abstrahiert Datenbankzugriffe
    - Ermöglicht Unabhängigkeit von konkreter Datenbankimplementierung

## Implementierungsbeispiele

### 1. Provided Interface (ProductManagementInt)
```java
public interface ProductManagementInt {
    // Hauptmethoden für Produktverwaltung
    Product getProductByName(String name);
    void openSession();
}

// Implementierung
public class ProductManagement implements ProductManagementInt {
    private final ProductRepository repository;
    private final Caching cacheService;

    @Override
    public Product getProductByName(String name) {
        // Erst Cache prüfen
        Product cached = cacheService.get(name);
        if (cached != null) return cached;

        // Aus Repository laden
        Product product = repository.findByName(name);
        cacheService.cacheResult(name, product);
        return product;
    }

    @Override
    public void openSession() {
        // Session-Verwaltung
    }
}
```

### 2. Required Interface (Caching)

Das Delegate-Pattern (oder Proxy-Pattern) ist ideal, um die Delegation zwischen internem und externem Verhalten zu realisieren:

Ein Port delegiert Aufrufe an die eigentliche Implementierung.

Er kann zusätzliche Funktionalität (Logging, Caching) einfügen.


```java
public interface Caching {
    void cacheResult(String key, List<Object> value);
    Object get(String key);
}

// Beispiel-Implementierung mit Logging
public class LoggingCacheDecorator implements Caching {
    private final Caching wrapped;
    
    public LoggingCacheDecorator(Caching wrapped) {
        this.wrapped = wrapped;
    }
    
    @Override
    public void cacheResult(String key, List<Object> value) {
        System.out.println("Caching: " + key);
        wrapped.cacheResult(key, value);
    }

    @Override
    public Object get(String key) {
        System.out.println("Cache lookup: " + key);
        return wrapped.get(key);
    }
}
```

### 3. Dependency Injection Beispiele

#### Controller mit Cache-Abhängigkeit
```java
@Component
public class ProductController {
    private final Caching cache;
    private final ProductManagementInt productService;

    @Autowired
    public ProductController(Caching cache, ProductManagementInt productService) {
        this.cache = cache;
        this.productService = productService;
    }

    public Product getProduct(String name) {
        return productService.getProductByName(name);
    }
}
```

## Best Practices für Port-Implementierung

1. **Klare Schnittstellendefinition**
    - Interfaces sollten spezifisch und zweckgebunden sein
    - Methoden klar dokumentieren
    - Versionierung berücksichtigen

2. **Dependency Injection**
    - Constructor Injection bevorzugen
    - Abhängigkeiten explizit machen
    - Standardimplementierungen für Tests bereitstellen

3. **Fehlerbehandlung**
   ```java
   public interface ProductManagementInt {
       Product getProductByName(String name) throws ProductNotFoundException;
       void openSession() throws SessionException;
   }
   ```

4. **Testbarkeit**
   ```java
   // Mock-Implementierung für Tests
   public class MockCache implements Caching {
       private Map<String, Object> cache = new HashMap<>();
       
       @Override
       public void cacheResult(String key, List<Object> value) {
           cache.put(key, value);
       }

       @Override
       public Object get(String key) {
           return cache.get(key);
       }
   }
   ```

## Architekturvorteile

1. **Modularität**
    - Komponenten sind unabhängig austauschbar
    - Einfache Integration neuer Implementierungen
    - Verbesserte Wartbarkeit

2. **Testbarkeit**
    - Mocking von Abhängigkeiten
    - Isolierte Komponententests
    - Vereinfachte Integrationstests

3. **Skalierbarkeit**
    - Einfache Erweiterung um neue Funktionalitäten
    - Austauschbare Implementierungen (z.B. verschiedene Cache-Systeme)
    - Flexible Anpassung an Lastanforderungen

## Technische Abhängigkeiten
- Java 8 oder höher
- Maven (für Dependency Injection)
- JDBC für Datenbankzugriffe
- JUnit für Tests (empfohlen)

## Lifecycle Management und CRUD-Operationen :: FA1

### 1. Methodenreihenfolge
Das Interface `ProductManagementInt` definiert eine strikte Reihenfolge für Methodenaufrufe:

1. **Initialisierung**: `openSession()` MUSS zuerst aufgerufen werden
2. **Operationen**: CRUD-Methoden können beliebig ausgeführt werden
3. **Beendigung**: `closeSession()` MUSS am Ende aufgerufen werden

### 2. Erweiterte CRUD-Operationen
Das Interface wurde um folgende Methoden erweitert:

```java
public interface ProductManagementInt {
    // Bestehende Methoden
    List<Product> getProductByName(String name);
    void openSession();
    void closeSession();

    // Neue CRUD-Methoden
    void createProduct(Product product);
    Product getProductById(int id);
    List<Product> getAllProducts();
    void updateProduct(Product product);
    void deleteProduct(int id);
}
```

### 3. Lifecycle-Implementierung :: FA1
Die Implementierung verwendet ein einfaches Zustandsmodell:

```java
public class ProductManagement implements ProductManagementInt {
    private Connection connection;
    private boolean isSessionOpen = false;

    @Override
    public void openSession() {
        if (isSessionOpen) {
            throw new IllegalStateException("Session is already open!");
        }
        try {
            connection = DatabaseConnection.getConnection();
            isSessionOpen = true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database connection", e);
        }
    }

    @Override
    public void closeSession() {
        if (!isSessionOpen) {
            throw new IllegalStateException("No session is currently open!");
        }
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            isSessionOpen = false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection", e);
        }
    }
}
```

### 4. Sicherheitsaspekte der Implementierung

1. **Zustandsüberwachung**:
   - Tracking des Session-Status über `isSessionOpen`
   - Validierung des Status vor jeder Operation
   - Verhinderung von mehrfachen Session-Öffnungen

2. **Fehlerbehandlung**:
   - Aussagekräftige Exception-Messages
   - Proper Resource Management
   - SQL-Exception Handling

3. **Best Practices**:
   - Fail-Fast Prinzip bei ungültigem Zustand
   - Sauberes Connection-Management
   - Thread-Safety Considerations

### 5. Beispiel einer CRUD-Operation

```java
@Override
public List<Product> getProductByName(String name) {
    if (!isSessionOpen) {
        throw new IllegalStateException("Session must be opened before executing queries!");
    }
    List<Product> products = new ArrayList<>();
    try {
        String sql = "SELECT * FROM products WHERE name LIKE ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, "%" + name + "%");
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            products.add(new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price")
            ));
        }
    } catch (SQLException e) {
        throw new RuntimeException("Error executing query", e);
    }
    return products;
}
```
