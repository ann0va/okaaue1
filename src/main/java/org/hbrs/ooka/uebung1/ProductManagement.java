package org.hbrs.ooka.uebung1;

import org.hbrs.ooka.uebung1.entities.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductManagement implements ProductManagementInt {
    private Connection connection;
    private boolean isSessionOpen = false;
    private final Caching cache;

    public ProductManagement(Caching cache) {
        this.cache = cache;
    }

    @Override
    public void openSession() {
        if (isSessionOpen) {
            throw new IllegalStateException("Session is already open!");
        }
        try {
            DatabaseConnection databaseConnection = new DatabaseConnection();
            connection = databaseConnection.getConnection();
            createTableIfNotExists();
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

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS products ("
                + "id INT PRIMARY KEY AUTO_INCREMENT, "
                + "name VARCHAR(255) NOT NULL, "
                + "price DOUBLE NOT NULL)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void checkSession() {
        if (!isSessionOpen) {
            throw new IllegalStateException("Session must be opened before executing queries!");
        }
    }

    @Override
    public List<Product> getProductByName(String name) {
        checkSession();
        
        // Check cache first
        Optional<List<Product>> cachedProducts = cache.getProductList(name);
        if (cachedProducts.isPresent()) {
            return cachedProducts.get();
        }

        // If not in cache, query database
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                ));
            }
            // Cache the results
            cache.cacheProductList(name, products);
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
    }

    public void createProduct(Product product) {
        checkSession();
        String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.executeUpdate();
            
            // Get the generated ID and set it in the product
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating product", e);
        }
    }

    @Override
    public Product getProductById(int id) {
        checkSession();
        
        // First check cache
        Optional<Product> cachedProduct = cache.getProduct(id);
        if (cachedProduct.isPresent()) {
            return cachedProduct.get();
        }

        // If not in cache, get from database
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
                // Cache the result
                cache.cacheProduct(id, product);
                return product;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting product by ID", e);
        }
    }

    public List<Product> getAllProducts() {
        checkSession();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all products", e);
        }
        return products;
    }

    @Override
    public void updateProduct(Product product) {
        checkSession();
        String sql = "UPDATE products SET name = ?, price = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getId());
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Product with ID " + product.getId() + " not found");
            }
            
            // Invalidate cache entries
            cache.invalidateProduct(product.getId());
            cache.clearCache(); // Clear search results as they might be affected
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
    }

    @Override
    public void deleteProduct(int id) {
        checkSession();
        String sql = "DELETE FROM products WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new RuntimeException("Product with ID " + id + " not found");
            }
            
            // Invalidate cache entries
            cache.invalidateProduct(id);
            cache.clearCache(); // Clear search results as they might be affected
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }
} 