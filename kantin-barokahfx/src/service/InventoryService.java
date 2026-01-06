package service;

import model.Category;
import model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {

    public List<Item> getAllItems() {
        List<Item> list = new ArrayList<>();

        String sql = """
            SELECT i.kode, i.nama, i.stok, i.harga, i.image, c.nama AS kategori
            FROM items i
            LEFT JOIN category c ON i.category_id = c.id
            ORDER BY i.kode
        """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Item(
                        rs.getString("kode"),
                        rs.getString("nama"),
                        new Category(rs.getString("kategori")),
                        rs.getInt("stok"),
                        rs.getDouble("harga"),
                        rs.getString("image")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addItem(Item item, String kategori) throws SQLException {
        Connection c = Database.getConnection();
        c.setAutoCommit(false);

        int catId;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id FROM category WHERE nama=?")) {
            ps.setString(1, kategori);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) catId = rs.getInt(1);
            else {
                PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO category(nama) VALUES (?)",
                        Statement.RETURN_GENERATED_KEYS);
                ins.setString(1, kategori);
                ins.executeUpdate();
                ResultSet g = ins.getGeneratedKeys();
                g.next();
                catId = g.getInt(1);
            }
        }

        PreparedStatement ps = c.prepareStatement("""
            INSERT INTO items(kode,nama,category_id,stok,harga,image)
            VALUES (?,?,?,?,?,?)
        """);

        ps.setString(1, item.getKode());
        ps.setString(2, item.getNama());
        ps.setInt(3, catId);
        ps.setInt(4, item.getStok());
        ps.setDouble(5, item.getHarga());
        ps.setString(6, item.getImagePath());

        ps.executeUpdate();
        c.commit();
    }

    public void updateItem(Item item) throws SQLException {
        PreparedStatement ps = Database.getConnection().prepareStatement("""
            UPDATE items
            SET nama=?, stok=?, harga=?, image=COALESCE(?,image)
            WHERE kode=?
        """);
        ps.setString(1, item.getNama());
        ps.setInt(2, item.getStok());
        ps.setDouble(3, item.getHarga());
        ps.setString(4, item.getImagePath());
        ps.setString(5, item.getKode());
        ps.executeUpdate();
    }

    public void deleteItemByKode(String kode) throws SQLException {
        PreparedStatement ps = Database.getConnection()
                .prepareStatement("DELETE FROM items WHERE kode=?");
        ps.setString(1, kode);
        ps.executeUpdate();
    }
}
