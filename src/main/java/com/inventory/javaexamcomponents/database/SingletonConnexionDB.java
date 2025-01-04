package com.inventory.javaexamcomponents.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SingletonConnexionDB {
    private static Connection connection;
    private static String url = "jdbc:mysql://localhost:3306/medical_office";
    private static String user = "root";
    private static String password = "root";

    private SingletonConnexionDB() {
    }

    public static Connection getConnexion() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return connection;
    }
}
