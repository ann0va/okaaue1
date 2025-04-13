# Komponenten-Ports in der Softwarearchitektur

## Überblick
Dieses Repository demonstriert das Konzept und die Implementierung von Ports in Komponentendiagrammen mit Java, am Beispiel eines ProductManagement-Systems. Das System zeigt, wie Ports die strukturierte Kommunikation zwischen Komponenten, externen Systemen und Datenbanken ermöglichen.

## Komponentendiagramm
Das System besteht aus zwei Hauptdiagrammen:

### 1. Strukturdiagramm

![ProductManagement Component Diagram](img/image1.png)

### 2. Port-Konfigurationsdiagramm
![Port-Konfigurationsdiagramm](img/image2.png)


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
