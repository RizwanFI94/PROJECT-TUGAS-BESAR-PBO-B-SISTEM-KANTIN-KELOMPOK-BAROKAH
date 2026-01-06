package controller;

import app.KantinApp;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Category;
import model.Item;
import service.InventoryService;
import service.TransactionService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class AdminController {

    @FXML private FlowPane flowBarang;
    @FXML private TextArea txtInfo;

    private final InventoryService inventoryService = new InventoryService();
    private final TransactionService transactionService = new TransactionService();

    private final Path UPLOAD_DIR = Path.of("uploads");

    @FXML
    private void initialize() {
        try {
            if (!Files.exists(UPLOAD_DIR)) {
                Files.createDirectories(UPLOAD_DIR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadBarang();
    }

    private void loadBarang() {
        flowBarang.getChildren().clear();

        List<Item> items = inventoryService.getAllItems();
        txtInfo.setText("Total barang: " + items.size());

        for (Item item : items) {
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
        iv.setFitWidth(150);
        iv.setFitHeight(90);
        iv.setPreserveRatio(true);

        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(110);
        imgBox.setMaxWidth(Double.MAX_VALUE);
        imgBox.setAlignment(Pos.CENTER);

        imgBox.getChildren().add(iv);

        
        boolean loaded = false;
        if (item.getImagePath() != null && !item.getImagePath().isBlank()) {
            File f = new File(item.getImagePath());
            if (f.exists()) {
                iv.setImage(new Image(f.toURI().toString(), 150, 0, true, true));
                loaded = true;
            }
        }

        if (!loaded) {
            try {
                iv.setImage(new Image(
                        getClass().getResource("/placeholder.png").toExternalForm(),
                        150, 0, true, true
                ));
            } catch (Exception ignored) {}
        }

        Label nama  = new Label(item.getNama());
        nama.setWrapText(true);
        nama.setStyle("-fx-font-weight:bold;");

        Label harga = new Label("Rp " + (int) item.getHarga());
        Label stok  = new Label("Stok: " + item.getStok());

        Button btnEdit = new Button("Edit");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        btnEdit.setStyle("-fx-background-color:#003f87; -fx-text-fill:white; -fx-font-weight:bold;");
        btnEdit.setOnAction(e -> showEditDialog(item));

        Button btnHapus = new Button("Hapus");
        btnHapus.setMaxWidth(Double.MAX_VALUE);
        btnHapus.setStyle("-fx-background-color:#ff8000; -fx-text-fill:white; -fx-font-weight:bold;");
        btnHapus.setOnAction(e -> handleHapus(item));

        card.getChildren().addAll(imgBox, nama, harga, stok, btnEdit, btnHapus);
        return card;
    }

    @FXML
    private void handleTambahBarang() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Tambah Barang");

        GridPane gp = new GridPane();
        gp.setHgap(5);
        gp.setVgap(5);
        gp.setPadding(new Insets(10));

        TextField tfKode = new TextField();
        TextField tfNama = new TextField();
        TextField tfKat  = new TextField();
        TextField tfStok = new TextField();
        TextField tfHarga= new TextField();

        ImageView iv = new ImageView();
        iv.setFitWidth(120);
        iv.setFitHeight(80);
        iv.setPreserveRatio(true);

        Button btnImg = new Button("Pilih Foto");
        final File[] selected = new File[1];

        btnImg.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg")
            );
            File f = fc.showOpenDialog(btnImg.getScene().getWindow());
            if (f != null) {
                selected[0] = f;
                iv.setImage(new Image(f.toURI().toString(), 120, 0, true, true));
            }
        });

        gp.addRow(0, new Label("Kode"), tfKode);
        gp.addRow(1, new Label("Nama"), tfNama);
        gp.addRow(2, new Label("Kategori"), tfKat);
        gp.addRow(3, new Label("Stok"), tfStok);
        gp.addRow(4, new Label("Harga"), tfHarga);
        gp.addRow(5, new Label("Foto"), new HBox(10, btnImg, iv));

        d.getDialogPane().setContent(gp);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        d.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    Item item = new Item(
                            tfKode.getText().trim(),
                            tfNama.getText().trim(),
                            new Category(tfKat.getText().trim()),
                            Integer.parseInt(tfStok.getText().trim()),
                            Double.parseDouble(tfHarga.getText().trim())
                    );

                    if (selected[0] != null) {
                        item.setImagePath(saveImage(selected[0]));
                    }

                    inventoryService.addItem(item, tfKat.getText().trim());
                    loadBarang();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Gagal menambah barang");
                }
            }
        });
    }

    private void showEditDialog(Item item) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Edit Barang");
        d.setHeaderText("Edit data barang");

        GridPane gp = new GridPane();
        gp.setHgap(8);
        gp.setVgap(8);
        gp.setPadding(new Insets(10));

        TextField tfNama  = new TextField(item.getNama());
        TextField tfStok  = new TextField(String.valueOf(item.getStok()));
        TextField tfHarga = new TextField(String.valueOf((int) item.getHarga()));

        ImageView iv = new ImageView();
        iv.setFitWidth(120);
        iv.setFitHeight(80);
        iv.setPreserveRatio(true);

    if (item.getImagePath() != null) {
        File f = new File(item.getImagePath());
        if (f.exists()) {
            iv.setImage(new Image(f.toURI().toString(), 120, 0, true, true));
        }
    }

    Button btnImg = new Button("Ganti Foto");
    final File[] selected = new File[1];

    btnImg.setOnAction(e -> {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg")
        );
        File f = fc.showOpenDialog(btnImg.getScene().getWindow());
        if (f != null) {
            selected[0] = f;
            iv.setImage(new Image(f.toURI().toString(), 120, 0, true, true));
        }
    });

    gp.addRow(0, new Label("Nama"), tfNama);
    gp.addRow(1, new Label("Stok"), tfStok);
    gp.addRow(2, new Label("Harga"), tfHarga);
    gp.addRow(3, new Label("Foto"), new HBox(10, btnImg, iv));

    d.getDialogPane().setContent(gp);
    d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    d.showAndWait().ifPresent(bt -> {
        if (bt == ButtonType.OK) {
            try {
                item.setNama(tfNama.getText().trim());
                item.setStok(Integer.parseInt(tfStok.getText().trim()));
                item.setHarga(Double.parseDouble(tfHarga.getText().trim()));

                if (selected[0] != null) {
                    item.setImagePath(saveImage(selected[0]));
                }

                inventoryService.updateItem(item);
                loadBarang();

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Gagal mengedit barang");
            }
        }
    });
}

    private void handleHapus(Item item) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setContentText("Hapus " + item.getNama() + "?");
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    inventoryService.deleteItemByKode(item.getKode());
                    loadBarang();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Gagal menghapus barang");
                }
            }
        });
    }

    @FXML
    private void handleLihatTransaksi() {
        Stage s = new Stage();
        s.setTitle("Data Transaksi");

        TableView<TransactionService.PurchaseSummary> table = new TableView<>();

        TableColumn<TransactionService.PurchaseSummary, Integer> colId =
                new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<TransactionService.PurchaseSummary, String> colNama =
                new TableColumn<>("Pembeli");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaPembeli"));

        TableColumn<TransactionService.PurchaseSummary, String> colTgl =
                new TableColumn<>("Tanggal");
        colTgl.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        TableColumn<TransactionService.PurchaseSummary, Double> colTotal =
                new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        table.getColumns().addAll(List.of(colId, colNama, colTgl, colTotal));
        table.getItems().addAll(transactionService.getAllPurchases());

        VBox root = new VBox(table);
        root.setPadding(new Insets(10));
        s.setScene(new Scene(root, 600, 400));
        s.show();
    }

    @FXML
    private void handleRefresh() {
        loadBarang();
    }

    @FXML
    private void handleKeluar() {
        KantinApp.setRoot("view/login-view.fxml", "Kantin Barokah - Login");
    }

    private String saveImage(File f) throws Exception {
        String name = System.currentTimeMillis() + "_" + f.getName();
        Path dest = UPLOAD_DIR.resolve(name);
        Files.copy(f.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString().replace("\\", "/");
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }
}
