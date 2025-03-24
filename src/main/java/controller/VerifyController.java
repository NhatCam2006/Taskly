package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class VerifyController {
    @FXML private TextField codeField;
    @FXML private Label errorLabel;
    @FXML private Button verifyButton;

    private String correctCode;
    private String tempUsername;
    private String tempEmail;
    private String tempPassword;

    // 📌 Nhận dữ liệu từ RegisterController
    public void setVerificationData(String username, String email, String password, String code) {
        this.tempUsername = username;
        this.tempEmail = email;
        this.tempPassword = password;
        this.correctCode = code;
    }

    // 📌 Xử lý xác minh mã
    @FXML
    private void handleVerify() {
        String enteredCode = codeField.getText();
        if (enteredCode.equals(correctCode)) {
            boolean success = UserDAO.registerUser(tempUsername, tempEmail, tempPassword);
            if (success) {
                System.out.println("✅ Xác minh thành công! Đăng ký hoàn tất.");

                // Chuyển về màn hình đăng nhập
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) codeField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Đăng nhập");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                errorLabel.setText("Lỗi khi đăng ký tài khoản!");
            }
        } else {
            errorLabel.setText("Mã xác minh không đúng!");
        }
    }
}
