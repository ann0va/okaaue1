package org.hbrs.ooka.uebung1;

import org.hbrs.ooka.uebung1.entities.Product;
import java.sql.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProductManagement implements ProductManagementInt {
    private Connection connection;
    private boolean isSessionOpen = false;
    private final Map<Integer, Product> productCache;
    private final Map<String, List<Product>> searchCache;
    
    // Logging setup
    private static final String LOG_FILE = "product_management.log";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final PrintWriter logWriter;

    public ProductManagement() {
        this.productCache = new HashMap<>();
        this.searchCache = new HashMap<>();
        // Initialize logger
        try {
            this.logWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logging", e);
        }
        log("ProductManagement initialized");
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(dateFormatter);
        String logMessage = String.format("[%s] %s", timestamp, message);
        logWriter.println(logMessage);
        System.out.println(logMessage); // Optional: also print to console
    }

    @Override
    public void openSession() {
        if (isSessionOpen) {
            log("Error: Attempted to open already open session");
            throw new IllegalStateException("Session is already open!");
        }
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            connection = databaseConnection.getConnection();
            createTableIfNotExists();
            isSessionOpen = true;
            log("Session opened successfully");
        } catch (SQLException e) {
            log("Error opening session: " + e.getMessage());
            throw new RuntimeException("Failed to open database connection", e);
        }
    }

    @Override
    public void closeSession() {
        if (!isSessionOpen) {
            log("Error: Attempted to close non-existent session");
            throw new IllegalStateException("No session is currently open!");
        }
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            isSessionOpen = false;
            clearCaches();
            log("Session closed successfully");
        } catch (SQLException e) {
            log("Error closing session: " + e.getMessage());
            throw new RuntimeException("Failed to close database connection", e);
        }
    }

    private void clearCaches() {
        productCache.clear();
        searchCache.clear();
        log("Caches cleared");
    }

    @Override
    public Product getProductById(int id) {
        checkSession();
        log("Searching for product with ID: " + id);

        // Check cache first
        Product cachedProduct = productCache.get(id);
        if (cachedProduct != null) {
            log("Cache hit for product ID: " + id);
            return cachedProduct;
        }
        log("Cache miss for product ID: " + id);

        // Database query
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                );
                productCache.put(id, product);
                log("Product found and cached: " + product);
                return product;
            }
            log("No product found with ID: " + id);
            return null;
        } catch (SQLException e) {
            log("Error querying product by ID: " + e.getMessage());
            throw new RuntimeException("Error getting product by ID", e);
        }
    }

    @Override
    public List<Product> getProductByName(String name) {
        checkSession();
        log("Searching for products with name: " + name);

        // Check cache
        List<Product> cachedProducts = searchCache.get(name.toLowerCase());
        if (cachedProducts != null) {
            log("Cache hit for search term: " + name);
            return new ArrayList<>(cachedProducts);
        }
        log("Cache miss for search term: " + name);

        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                );
                products.add(product);
                productCache.put(product.getId(), product);
            }
            searchCache.put(name.toLowerCase(), new ArrayList<>(products));
            log("Found " + products.size() + " products matching: " + name);
            return products;
        } catch (SQLException e) {
            log("Error searching products by name: " + e.getMessage());
            throw new RuntimeException("Error executing query", e);
        }
    }

    @Override
    public void createProduct(Product product) {
        checkSession();
        log("Creating new product: " + product);

        String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    product.setId(id);
                    productCache.put(id, product);
                    log("Product created successfully with ID: " + id);
                }
            }
            searchCache.clear();
        } catch (SQLException e) {
            log("Error creating product: " + e.getMessage());
            throw new RuntimeException("Error creating product", e);
        }
    }

    @Override
    public void updateProduct(Product product) {
        checkSession();
        log("Updating product: " + product);

        String sql = "UPDATE products SET name = ?, price = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getId());
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected == 0) {
                log("No product found to update with ID: " + product.getId());
                throw new RuntimeException("Product not found: " + product.getId());
            }
            
            productCache.put(product.getId(), product);
            searchCache.clear();
            log("Product updated successfully");
        } catch (SQLException e) {
            log("Error updating product: " + e.getMessage());
            throw new RuntimeException("Error updating product", e);
        }
    }

    @Override
    public void deleteProduct(int id) {
        checkSession();
        log("Deleting product with ID: " + id);

        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected == 0) {
                log("No product found to delete with ID: " + id);
                throw new RuntimeException("Product not found: " + id);
            }
            
            productCache.remove(id);
            searchCache.clear();
            log("Product deleted successfully");
        } catch (SQLException e) {
            log("Error deleting product: " + e.getMessage());
            throw new RuntimeException("Error deleting product", e);
        }
    }

    @Override
    public List<Product> getAllProducts() {
        checkSession();
        log("Retrieving all products");

        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                );
                products.add(product);
                productCache.put(product.getId(), product);
            }
            log("Retrieved " + products.size() + " products");
            return products;
        } catch (SQLException e) {
            log("Error retrieving all products: " + e.getMessage());
            throw new RuntimeException("Error getting all products", e);
        }
    }

    private void checkSession() {
        if (!isSessionOpen) {
            log("Error: Attempted to perform operation without open session");
            throw new IllegalStateException("Session must be opened before executing queries!");
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS products ("
                + "id INT PRIMARY KEY AUTO_INCREMENT, "
                + "name VARCHAR(255) NOT NULL, "
                + "price DOUBLE NOT NULL)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log("Database table checked/created successfully");
        }
    }

    // Ensure resources are properly closed
    @Override
    protected void finalize() throws Throwable {
        try {
            if (logWriter != null) {
                logWriter.close();
            }
        } finally {
            super.finalize();
        }
    }
} 