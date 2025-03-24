package controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class HelloController {
    @FXML
    private Button addBtn;

    @FXML
    private Button chartBtn;

    @FXML
    private Button closeButton;

    @FXML
    private Pane contentPane;

    @FXML
    private Button editBtn;

    @FXML
    private Button listBtn;

    @FXML
    private Button maximizeButton;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button settingBtn;

    @FXML
    private Button getStartBtn;

    @FXML
    private VBox sideBar;

    @FXML
    private Button chatbotBtn;


    @FXML
    private void showChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/chatbot.fxml"));
            Parent chatbotView = loader.load();
            contentPane.getChildren().setAll(chatbotView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChatbot() {
        showChatbot(); // Gọi showChatbot() khi nhấn nút chatbotBtn
    }

    @FXML
    private void handleAddTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add-task.fxml"));
            Parent addTaskView = loader.load();
            contentPane.getChildren().setAll(addTaskView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showEditTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit-task.fxml"));
            Parent addTaskView = loader.load();
            contentPane.getChildren().setAll(addTaskView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void switchToMainView() {
        try {
            Stage stage = (Stage) getStartBtn.getScene().getWindow(); // Lấy Stage hiện tại
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/view/main-layout.fxml"));
            Parent mainRoot = mainLoader.load();
            Scene mainScene = new Scene(mainRoot);
            stage.setScene(mainScene);
            stage.centerOnScreen();

            // Lấy controller của main-layout để hiển thị list-task.fxml mặc định
            HelloController mainController = mainLoader.getController();
            mainController.showListTask(); // Hiển thị list-task.fxml trong contentPane

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showListTask() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/list-task.fxml"));
            Parent listTaskView = loader.load();
            contentPane.getChildren().setAll(listTaskView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleListTask() {
        showListTask(); // Khi ấn nút listBtn thì gọi lại showListTask()
    }

    @FXML
    private void showChartView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/chart.fxml"));
            Parent chartView = loader.load();
            contentPane.getChildren().setAll(chartView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChartView() {
        showChartView(); // Khi nhấn nút chartBtn thì gọi showChartView()
    }


    @FXML
    private void closeApp() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // Thu nhỏ ứng dụng
    @FXML
    private void minimizeApp() {
        Stage stage = (Stage) minimizeButton.getScene().getWindow();
        stage.setIconified(true);
    }

    // Phóng to / thu nhỏ cửa sổ
    @FXML
    private void maximizeApp() {
        Stage stage = (Stage) maximizeButton.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false); // Nếu đang phóng to thì thu nhỏ lại
        } else {
            stage.setMaximized(true); // Nếu đang nhỏ thì phóng to
        }
    }

}