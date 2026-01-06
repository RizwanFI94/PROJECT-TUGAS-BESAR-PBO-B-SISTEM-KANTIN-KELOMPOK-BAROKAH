package controller;

import app.KantinApp;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Item;
import model.Pembeli;
import model.Purchase;
import service.InventoryService;
import service.PdfService;
import service.TransactionService;

import java.io.File;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PembeliController {

    @FXML private FlowPane flowBarang;

    @FXML private ListView<String> listCart;
    @FXML private TextField tfTotal;
    @FXML private TextField tfBayar;
    @FXML private TextField tfKembali;

    private final InventoryService inventoryService = new InventoryService();
    private final TransactionService transactionService = new TransactionService();

    private final Map<Item, Integer> cart = new LinkedHashMap<>();

    @FXML
    private void initialize() {
        loadItems();
    }

    private void loadItems() {
        flowBarang.getChildren().clear();

        for (Item item : inventoryService.getAllItems()) {
            flowBarang.getChildren().add(createItemCard(item));
        }
    }

    private VBox createItemCard(Item item) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(8));
        card.setPrefWidth(180);
        card.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:12;" +
                "-fx-border-color:#d0d0d0;" +
                "-fx-border-radius:12;"
        );

        ImageView iv = new ImageView();
        iv.setFitWidth(140);
        iv.setFitHeight(90);
        iv.setPreserveRatio(true);

        StackPane imgBox = new StackPane(iv);
        imgBox.setPrefHeight(110);
        imgBox.setAlignment(Pos.CENTER);

        boolean loaded = false;
        if (item.getImagePath() != null && !item.getImagePath().isBlank()) {
            File f = new File(item.getImagePath());
            if (f.exists()) {
                iv.setImage(new Image(f.toURI().toString(), 140, 0, true, true));
                loaded = true;
            }
        }

        if (!loaded) {
            try {
                iv.setImage(new Image(
                        getClass().getResource("/placeholder.png").toExternalForm(),
                        140, 0, true, true
                ));
            } catch (Exception ignored) {}
        }

        Label nama  = new Label(item.getNama());
        nama.setStyle("-fx-font-weight:bold;");
        nama.setWrapText(true);

        Label harga = new Label("Rp " + (int) item.getHarga());
        Label stok  = new Label("Stok: " + item.getStok());

        Button btnTambah = new Button("Tambah");
        btnTambah.setMaxWidth(Double.MAX_VALUE);
        btnTambah.setStyle(
                "-fx-background-color:#003F87;" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;"
        );

    btnTambah.setOnAction(e -> {
        int currentQty = cart.getOrDefault(item, 0);

        if (currentQty >= item.getStok()) {
            showAlert(Alert.AlertType.WARNING,
                    "Stok " + item.getNama() + " hanya " + item.getStok() + " buah.\n" +
                    "Tidak bisa menambah lebih banyak.");
            return;
        }

        cart.put(item, currentQty + 1);
        refreshCart();
    });


        card.getChildren().addAll(imgBox, nama, harga, stok, btnTambah);
        return card;
    }

    private void refreshCart() {
        listCart.getItems().clear();
        double total = 0;

        for (var entry : cart.entrySet()) {
            Item item = entry.getKey();
            int qty = entry.getValue();
            double sub = item.getHarga() * qty;

            listCart.getItems().add(
                    item.getNama() + " x" + qty + " = Rp " + (int) sub
            );
            total += sub;
        }

        tfTotal.setText(String.valueOf((int) total));
    }

    @FXML
private void handleBeli() {
    if (cart.isEmpty()) {
        showAlert(Alert.AlertType.WARNING, "Keranjang masih kosong");
        return;
    }

    try {
        double total = Double.parseDouble(tfTotal.getText());
        double bayar = Double.parseDouble(tfBayar.getText());

        if (bayar < total) {
            showAlert(Alert.AlertType.ERROR, "Uang bayar kurang");
            return;
        }

        double kembali = bayar - total;
        tfKembali.setText(String.valueOf((int) kembali));

        Pembeli pembeli = Session.getCurrentPembeli();
        Purchase purchase = new Purchase(pembeli);

        for (var entry : cart.entrySet()) {
            Item item = entry.getKey();
            int qty = entry.getValue();

            item.kurangiStok(qty);

            try {
                inventoryService.updateItem(item);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Gagal update stok: " + e.getMessage());
                return;
            }

            purchase.addDetail(item, qty);
        }

        transactionService.savePurchase(purchase);

        StringBuilder sb = new StringBuilder();
        sb.append("STRUK PEMBELIAN\n");
        sb.append("--------------------------------\n");

        for (var entry : cart.entrySet()) {
            Item item = entry.getKey();
            int qty = entry.getValue();
            sb.append(item.getNama())
                    .append(" x").append(qty)
                    .append(" = Rp ")
                    .append((int) (item.getHarga() * qty))
                    .append("\n");
        }

        sb.append("--------------------------------\n");
        sb.append("Total     : Rp ").append((int) total).append("\n");
        sb.append("Bayar     : Rp ").append((int) bayar).append("\n");
        sb.append("Kembalian : Rp ").append((int) kembali).append("\n");

        TextArea struk = new TextArea(sb.toString());
        struk.setEditable(false);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Struk Pembelian");
        alert.getDialogPane().setContent(struk);
        alert.showAndWait();

        // pdf
        String pdfPath = PdfService.generateStruk(
                pembeli.getNama(),
                sb.toString()
        );

        if (pdfPath != null) {
            try {
                java.awt.Desktop.getDesktop().open(new File(pdfPath));
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Gagal membuka PDF: " + ex.getMessage());
            }
        }

        cart.clear();
        listCart.getItems().clear();
        tfTotal.clear();
        tfBayar.clear();
        tfKembali.clear();
        loadItems();

    } catch (NumberFormatException e) {
        showAlert(Alert.AlertType.ERROR, "Input pembayaran tidak valid");
    }
}

    @FXML
    private void handleKeluar() {
        KantinApp.setRoot("view/login-view.fxml", "Kantin Barokah - Login");
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleKurangiSatu() {
        String selected = listCart.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih item di keranjang dulu");
            return;
        }

        Item target = null;
        for (Item it : cart.keySet()) {
            if (selected.startsWith(it.getNama())) {
                target = it;
                break;
            }
        }

        if (target == null) return;

        int qty = cart.get(target);

        if (qty > 1) {
            cart.put(target, qty - 1);
        } else {
            cart.remove(target); 
        }

        refreshCart();
    }
}
