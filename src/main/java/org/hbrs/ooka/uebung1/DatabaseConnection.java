package org.hbrs.ooka.uebung1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
        private static final String URL = "jdbc:h2:~/test";
        private static final String USER = "sa";
        private static final String PASSWORD = "";

        public static Connection getConnection() throws SQLException {
                DriverManager.registerDriver(new org.h2.Driver());
                return DriverManager.getConnection(URL, USER, PASSWORD);
        }
}


