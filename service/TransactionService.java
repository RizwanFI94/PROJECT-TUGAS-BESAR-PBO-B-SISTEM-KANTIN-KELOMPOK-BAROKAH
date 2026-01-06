package service;

import model.Item;
import model.Purchase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void savePurchase(Purchase p) {
        String insPurchase = "INSERT INTO purchases(user_id,tanggal,total) VALUES (?,?,?)";
        String insDetail = """
                INSERT INTO purchase_details(purchase_id,item_kode,nama,qty,harga,subtotal)
                VALUES (?,?,?,?,?,?)
                """;

        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);

            int userId = getUserIdByUsername(c, p.getPembeli().getUsername());
            if (userId == -1) throw new SQLException("User tidak ditemukan");

            int purchaseId;
            try (PreparedStatement ps = c.prepareStatement(insPurchase,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, LocalDateTime.now().format(FMT));
                ps.setDouble(3, p.getTotal());
                ps.executeUpdate();
                ResultSet gen = ps.getGeneratedKeys();
                gen.next();
                purchaseId = gen.getInt(1);
            }

            for (Purchase.Detail d : p.getDetails()) {
                Item item = d.getItem();
                try (PreparedStatement ps = c.prepareStatement(insDetail)) {
                    ps.setInt(1, purchaseId);
                    ps.setString(2, item.getKode());
                    ps.setString(3, item.getNama());
                    ps.setInt(4, d.getQty());
                    ps.setDouble(5, d.getHargaSatuan());
                    ps.setDouble(6, d.getSubtotal());
                    ps.executeUpdate();
                }
            }

            c.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getUserIdByUsername(Connection c, String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public static class PurchaseSummary {
        private int id;
        private String namaPembeli;
        private String tanggal;
        private double total;

        public PurchaseSummary(int id, String namaPembeli, String tanggal, double total) {
            this.id = id;
            this.namaPembeli = namaPembeli;
            this.tanggal = tanggal;
            this.total = total;
        }

        public int getId() { return id; }
        public String getNamaPembeli() { return namaPembeli; }
        public String getTanggal() { return tanggal; }
        public double getTotal() { return total; }
    }

    public List<PurchaseSummary> getAllPurchases() {
        List<PurchaseSummary> list = new ArrayList<>();

        String sql = """
                SELECT p.id, u.nama AS pembeli, p.tanggal, p.total
                FROM purchases p
                JOIN users u ON p.user_id = u.id
                ORDER BY p.id DESC
                """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new PurchaseSummary(
                        rs.getInt("id"),
                        rs.getString("pembeli"),
                        rs.getString("tanggal"),
                        rs.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}