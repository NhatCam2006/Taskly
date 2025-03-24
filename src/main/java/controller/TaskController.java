package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import model.Task;

import java.util.Timer;
import java.util.TimerTask;

public class TaskController {
    @FXML private Label taskName;
    @FXML private Label dueDate;
    @FXML private Button startTaskBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;

    private Task task;
    private Timer timer;

    public void setTask(Task task) {
        this.task = task;
        taskName.setText(task.getContent());
        dueDate.setText(task.getStartTime() == null ? "Chưa bắt đầu" : task.getStartTime().toString());
        updateUI();
    }

    @FXML
    private void startTask() {
        if (!task.getStatus().equals("Chưa bắt đầu")) return;

        task.startTask();
        updateUI();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    task.updateProgress();
                    updateUI();
                    if (task.getProgress() >= 100) {
                        timer.cancel();
                    }
                });
            }
        }, 0, 1000);
    }

    private void updateUI() {
        progressBar.setProgress(task.getProgress() / 100.0);
        progressLabel.setText(task.getProgress() + "%");
        startTaskBtn.setDisable(task.getStatus().equals("Đang thực hiện") || task.getStatus().equals("Đã hoàn thành"));
    }
}
