package com.odeene;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/meteobd";
    private static final String USER = "root";
    private static final String PASSWORD = "abc123.";

    static private Connection connection;

    static public void conectarBD() throws SQLException {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexion establecida correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            throw e;
        }
    }

     public static void insertarDatos(String ciudad,
                             String dia,
                             String franja,
                             String cielo,
                             String temp,
                             String lluvias,
                             String viento,
                             String humedad,
                             String nubosidad) throws SQLException {
        String sql = "INSERT INTO prevision (ciudad, dia, franja_horaria, estado_del_cielo, temperatura, probabilidad_de_lluvias, viento, humedad, cobertura_nubosa) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ciudad);
            ps.setString(2, dia);
            ps.setString(3, franja);
            ps.setString(4, cielo);
            ps.setString(5, temp);
            ps.setString(6, lluvias);
            ps.setString(7, viento);
            ps.setString(8, humedad);
            ps.setString(9, nubosidad);
            ps.executeUpdate();
            System.out.println("Prevision insertado correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al insertar el prevision: " + e.getMessage());
            throw e;
        }
    }
}
