package service;

import model.Admin;
import model.Pembeli;
import model.User;

import java.sql.*;

public class UserService {

    public boolean registerAdmin(String username, String password, String nama) {
        return registerUser(username, password, nama, "ADMIN");
    }

    public boolean registerPembeli(String username, String password, String nama) {
        return registerUser(username, password, nama, "PEMBELI");
    }

    private boolean registerUser(String username, String password,
                                 String nama, String role) {
        String cek = "SELECT id FROM users WHERE username = ?";
        String ins = "INSERT INTO users (username,password,nama,role) VALUES (?,?,?,?)";

        try (Connection c = Database.getConnection();
             PreparedStatement psCek = c.prepareStatement(cek);
             PreparedStatement psIns = c.prepareStatement(ins)) {

            psCek.setString(1, username);
            if (psCek.executeQuery().next()) {
                return false;
            }

            psIns.setString(1, username);
            psIns.setString(2, password);
            psIns.setString(3, nama);
            psIns.setString(4, role);
            psIns.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User login(String username, String password, String role) {
        String sql = "SELECT * FROM users WHERE username=? AND password=? AND role=?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nama = rs.getString("nama");
                if ("ADMIN".equals(role)) {
                    return new Admin(username, password, nama);
                } else {
                    return new Pembeli(username, password, nama);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}