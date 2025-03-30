package org.hbrs.ooka.uebung1;

import org.hbrs.ooka.uebung1.entities.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    // Weitere CRUD-Methoden können hinzugefügt werden
    public void save(Product product) {
        Connection connection;
        try {
            connection = DatabaseConnection.getConnection();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Siehe auch Beispiel in ConnectionTest.java
    }


}
