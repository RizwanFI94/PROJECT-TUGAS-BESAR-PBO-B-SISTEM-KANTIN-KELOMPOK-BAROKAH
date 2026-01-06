package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Admin;
import model.Pembeli;
import model.User;
import service.UserService;
import app.KantinApp;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        cmbRole.getItems().addAll("ADMIN", "PEMBELI");
        cmbRole.setValue("PEMBELI");
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String role = cmbRole.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "ID dan Password wajib diisi");
            return;
        }

        User user = userService.login(username, password, role);
        if (user == null) {
            showAlert(Alert.AlertType.ERROR,
                    "Login gagal. Periksa ID, Password, dan Role.");
            return;
        }

        if (user instanceof Admin) {
            Session.setCurrentAdmin((Admin) user);
            KantinApp.setRoot("view/admin-view.fxml",
                    "Kantin Barokah - Admin");
        } else if (user instanceof Pembeli) {
            Session.setCurrentPembeli((Pembeli) user);
            KantinApp.setRoot("view/pembeli-view.fxml",
                    "Kantin Barokah - User");
        }
    }

    @FXML
    private void handleRegister() {

        TextInputDialog dId = new TextInputDialog();
        dId.setTitle("Registrasi");
        dId.setHeaderText("Masukkan ID");
        dId.setContentText("ID:");

        dId.showAndWait().ifPresent(id -> {

            if (id.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "ID tidak boleh kosong");
                return;
            }

            TextInputDialog dPass = new TextInputDialog();
            dPass.setHeaderText("Masukkan Password");
            dPass.setContentText("Password:");

            dPass.showAndWait().ifPresent(pass -> {

                if (pass.trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Password tidak boleh kosong");
                    return;
                }

                TextInputDialog dNama = new TextInputDialog();
                dNama.setHeaderText("Masukkan Nama Lengkap");
                dNama.setContentText("Nama:");

                dNama.showAndWait().ifPresent(nama -> {

                    if (nama.trim().isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Nama tidak boleh kosong");
                        return;
                    }

                    boolean ok = userService.registerPembeli(
                            id.trim(),
                            pass.trim(),
                            nama.trim()
                    );

                    showAlert(
                            ok ? Alert.AlertType.INFORMATION
                               : Alert.AlertType.ERROR,
                            ok ? "Registrasi berhasil.\nSilakan login sebagai PEMBELI."
                               : "Registrasi gagal. ID sudah digunakan."
                    );
                });
            });
        });
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
