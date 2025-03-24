package controller;

import dao.UserDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.User;
import utils.SessionManager; // 🆕 Thêm import để quản lý session

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private AnchorPane rootPane;

    public void initialize() {
        // Sử dụng Platform.runLater để đảm bảo giao diện đã load xong
        Platform.runLater(() -> rootPane.requestFocus());
    }
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = UserDAO.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) { // TODO: Hash password sau này
            System.out.println("✅ Đăng nhập thành công! User ID: " + user.getId());

            // Lưu user vào session để các màn hình khác có thể dùng
            SessionManager.setCurrentUser(user);

            // Chuyển sang màn hình chính (hello-view.fxml)
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/hello-view.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene newScene = new Scene(root);
                stage.setScene(newScene);
                stage.setTitle("Task Manager");

                // Căn giữa cửa sổ trên màn hình
                stage.centerOnScreen();

                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Tên đăng nhập hoặc mật khẩu sai!");
        }
    }


    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng ký - Task Manager");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
