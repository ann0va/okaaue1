package org.hbrs.ooka.uebung1;


import org.hbrs.ooka.uebung1.entities.Product;
import java.util.List;
import java.util.Optional;

public class ProductController {
    private final ProductManagementInt productManagement;

    public ProductController(ProductManagementInt productManagement) {
        this.productManagement = productManagement;
    }

    public Product getProduct(int id) {
        return productManagement.getProductById(id);
    }

    public List<Product> searchProducts(String name) {
        return productManagement.getProductByName(name);
    }
    
    public void updateProduct(Product product) {
        productManagement.updateProduct(product);
    }
    
    public void deleteProduct(int id) {
        productManagement.deleteProduct(id);
    }
} 