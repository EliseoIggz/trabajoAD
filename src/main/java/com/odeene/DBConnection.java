package com.odeene;

import java.sql.*;
import java.util.ArrayList;

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

    public static boolean existenDatosParaFechaYCiudad(String dia, String ciudad) throws SQLException {
        String query = "SELECT COUNT(*) FROM prevision WHERE dia = ? AND ciudad = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, dia);
            stmt.setString(2, ciudad);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Si la cuenta es mayor que 0, ya existen datos
            }
        }
        return false;
    }

    public static ArrayList<String> consultaDePrevisionPorCiudadYFranja(String ciudad, String dato) throws SQLException {
        String query = "SELECT franja_horaria, "+dato+" FROM prevision WHERE ciudad = ?";
        ArrayList<String> resultado = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, ciudad);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnas = metaData.getColumnCount();

                while (rs.next()) {
                    StringBuilder fila = new StringBuilder();
                    for (int i = 1; i <= columnas; i++) {
                        fila.append(rs.getString(i)).append(" | ");
                    }
                    resultado.add(fila.toString());
                }
            }
        }
        return resultado;
    }

}
