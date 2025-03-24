package controller;

import dao.TaskDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.Task;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.HBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;


import javafx.geometry.Rectangle2D;
import java.sql.Timestamp;
import java.util.Arrays;

import javafx.scene.input.MouseEvent;

public class ListTaskCellController {
    @FXML private Pane priorityIndicator;
    @FXML private Label taskName;
    @FXML private Label taskDescription;
    @FXML private Label dueDate;
    @FXML private Label statusLabel; // ✅ Thêm label hiển thị trạng thái
    @FXML private Button startTaskBtn;
    @FXML private ProgressIndicator progressBar;
    @FXML private Label progressLabel;
    private Timeline countdownTimeline;
    private Task task;




    public void setTaskData(Task task) {
        if (task == null) return;
        this.task = task;

        taskName.setText(task.getContent());
        taskDescription.setText(task.getDescription());
        dueDate.setText("Due in " + task.getTimeToComplete() + " min");
        statusLabel.setText(task.getStatus());
        progressBar.setProgress(task.getProgress() / 100.0);
        progressLabel.setText(task.getProgress() + "%");

        switch (task.getCategoryLevel()) {
            case 1 -> priorityIndicator.setStyle("-fx-background-color: #84fab0; -fx-background-radius: 20px");
            case 2 -> priorityIndicator.setStyle("-fx-background-color: #74c0fc; -fx-background-radius: 20px");
            case 3 -> priorityIndicator.setStyle("-fx-background-color: #ff7eb3; -fx-background-radius: 20px");
            default -> priorityIndicator.setStyle("-fx-background-color: gray; -fx-background-radius: 20px");
        }

        // ✅ Kiểm tra trạng thái để đặt lại nút cho đúng
        if (task.getStatus().equals("Chưa bắt đầu")) {
            startTaskBtn.setText("Bắt đầu");
            startTaskBtn.setStyle(""); // Reset về mặc định
            startTaskBtn.setDisable(false);
        } else if (task.getStatus().equals("Đang thực hiện")) {
            startTaskBtn.setText("Đang thực hiện...");
            startTaskBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-border-color: transparent; -fx-effect: null; -fx-max-width: 500px");
            startTaskBtn.setDisable(true);
        } else if (task.getStatus().equals("✅ Hoàn thành")) {
            startTaskBtn.setText("Đã hoàn thành");
            startTaskBtn.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white; -fx-border-color: transparent; -fx-effect: null;");
            startTaskBtn.setDisable(true);
        }

        startTaskBtn.setOnAction(event -> startTask());
    }

    private void startTask() {
        if (!task.getStatus().equals("Chưa bắt đầu")) return;

        task.setStatus("Đang thực hiện");
        task.setStartTime(new Timestamp(System.currentTimeMillis()));
        task.setRemainingTime(task.getTimeToComplete() * 60); // Đổi phút thành giây
        task.setProgress(0);

        // Cập nhật database
        TaskDAO.updateTaskStatus(task.getId(), "Đang thực hiện", task.getStartTime());

        // **Cập nhật giao diện**
        statusLabel.setText("Đang thực hiện");
        progressBar.setProgress(0.0);
        progressLabel.setText("0%");

        // ✨ **Thay đổi giao diện nút**
        startTaskBtn.setText("Đang thực hiện...");
        startTaskBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-border-color: transparent;");
        startTaskBtn.setDisable(true); // Ngăn bấm lại

        // Bắt đầu đếm ngược
        startCountdown();
    }


    private void startCountdown() {
        if (countdownTimeline != null) countdownTimeline.stop();

        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    task.setRemainingTime(task.getRemainingTime() - 1);
                    double progress = 1.0 - (double) task.getRemainingTime() / (task.getTimeToComplete() * 60);
                    task.setProgress((int) (progress * 100));

                    // Cập nhật database
                    TaskDAO.updateTaskProgress(task.getId(), task.getRemainingTime(), task.getProgress());

                    // Cập nhật giao diện UI
                    Platform.runLater(() -> {
                        progressBar.setProgress(progress);
                        progressLabel.setText(task.getProgress() + "%");
                    });

                    // 🎯 **Nếu task hoàn thành**
                    if (task.getRemainingTime() <= 0) {
                        task.setStatus("✅ Hoàn thành");
                        TaskDAO.updateTaskStatus(task.getId(), "✅ Hoàn thành", null);
                        statusLabel.setText("✅ Hoàn thành");

                        countdownTimeline.stop();

                        // ✨ **Cập nhật giao diện nút khi hoàn thành**
                        Platform.runLater(() -> {
                            startTaskBtn.setText("Đã hoàn thành");
                            showNotification("Thông báo", "Công việc đã hoàn thành!");
                            startTaskBtn.setStyle("-fx-background-color: #32CD32; -fx-text-fill: white; -fx-border-color: transparent;");
                        });
                    }
                })
        );
        countdownTimeline.setCycleCount(task.getRemainingTime());
        countdownTimeline.play();
    }
    private void showNotification(String title, String message) {
        Platform.runLater(() -> {
            // Tạo stage thông báo
            Stage notificationStage = new Stage();
            notificationStage.initStyle(StageStyle.UNDECORATED);
            notificationStage.setAlwaysOnTop(true);

            // Tạo Label với style đẹp mắt
            Label label = new Label(title + "\n" + message);
            label.setStyle("-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 14px; -fx-background-radius: 10;");
            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            notificationStage.setScene(scene);

            // Định vị ở góc dưới bên phải
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            notificationStage.setX(screenBounds.getMaxX() - 300);
            notificationStage.setY(screenBounds.getMaxY() - 100);

            notificationStage.show();

            // Phát âm thanh "ting"
            // Đảm bảo rằng file âm thanh (ví dụ "ting.mp3") nằm trong thư mục resources/sound/
            try {
                String soundUrl = getClass().getResource("/sound/ting.mp3").toExternalForm();
                AudioClip clip = new AudioClip(soundUrl);
                clip.play();
            } catch (Exception e) {
                System.out.println("Không thể phát âm thanh: " + e.getMessage());
            }

            // Đóng thông báo sau 5 giây
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), ev -> notificationStage.close()));
            timeline.setCycleCount(1);
            timeline.play();
        });
    }


}
