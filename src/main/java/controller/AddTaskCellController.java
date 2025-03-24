package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Task;

public class AddTaskCellController {
    @FXML
    private Label taskContent;
    @FXML
    private Label taskDescription;
    @FXML
    private Label taskStatus;

    @FXML
    private Rectangle taskLevelColor;

    public void setTaskData(Task task) {
        taskContent.setText(task.getContent());
        taskDescription.setText(task.getDescription());
        taskStatus.setText(task.getStatus());

        // Xác định màu theo độ quan trọng
        switch (task.getCategoryLevel()) {
            case 1 -> taskLevelColor.setFill(Color.web("#84fab0FF"));
            case 2 -> taskLevelColor.setFill(Color.web("#74c0fcFF"));
            case 3 -> taskLevelColor.setFill(Color.web("#ff7eb3FF"));
        }
    }
}
