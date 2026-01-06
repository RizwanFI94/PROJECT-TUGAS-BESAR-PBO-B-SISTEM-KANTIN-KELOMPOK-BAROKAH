package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class Database {

    private static final String URL = "jdbc:sqlite:kantin.db";

    static {
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /** Jalankan script.sql */
    public static void initSchemaIfNeeded() {
        try (Connection c = getConnection()) {
            try (ResultSet rs = c.getMetaData()
                    .getTables(null, null, "users", null)) {
                if (rs.next()) {
                    return;
                }
            }

            // baca file script.sql
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader("script.sql"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                System.err.println("Gagal membaca script.sql: " + e.getMessage());
                return;
            }

            String[] statements = sb.toString().split(";");
            try (Statement st = c.createStatement()) {
                for (String s : statements) {
                    String sql = s.trim();
                    if (!sql.isEmpty()) {
                        st.execute(sql);
                    }
                }
            }

            System.out.println("Schema database berhasil dibuat dari script.sql");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
