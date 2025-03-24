package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import service.EmailService;

import java.io.IOException;
import java.util.Random;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private String verificationCode;
    private String tempUsername;
    private String tempEmail;
    private String tempPassword;
    private java.awt.Label codeField;

    @FXML
    private void handleRegister() {
        tempUsername = usernameField.getText();
        tempEmail = emailField.getText();
        tempPassword = passwordField.getText();

        if (tempUsername.isEmpty() || tempEmail.isEmpty() || tempPassword.isEmpty()) {
            errorLabel.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (UserDAO.getUserByUsername(tempUsername) != null || UserDAO.getUserByEmail(tempEmail) != null) {
            errorLabel.setText("Tên đăng nhập hoặc email đã tồn tại!");
            return;
        }

        verificationCode = generateVerificationCode();
        EmailService.sendVerificationCode(tempEmail, verificationCode);
        System.out.println("📧 Email xác minh đã gửi đến: " + tempEmail);

        // Chuyển sang màn hình nhập mã xác minh (verify.fxml)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/verify.fxml"));
            Parent root = loader.load();

            // Lấy controller của verify.fxml để truyền mã xác minh
            VerifyController verifyController = loader.getController();
            verifyController.setVerificationData(tempUsername, tempEmail, tempPassword, verificationCode);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Xác minh tài khoản");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @FXML
    private void handleVerify() {
        // Lấy mã từ người dùng nhập vào
        String enteredCode = codeField.getText();

        if (enteredCode.equals(verificationCode)) {
            boolean success = UserDAO.registerUser(tempUsername, tempEmail, tempPassword);
            if (success) {
                System.out.println("✅ Đăng ký thành công!");
                // TODO: Chuyển về màn hình đăng nhập
            } else {
                errorLabel.setText("Lỗi khi đăng ký!");
            }
        } else {
            errorLabel.setText("Mã xác minh không đúng!");
        }
    }
}
