package controller;

import database.DatabaseConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Task;
import utils.SessionManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddTaskController {

    @FXML
    private ComboBox<LevelColor> choiceCard;
    @FXML
    private TextField inputContent;
    @FXML
    private TextField inputDes;
    @FXML
    private TextField inputTime;
    @FXML
    private Button saveBtn;
    @FXML
    private Button delBtn;

    @FXML
    private TableView<Task> tableadd; // Thay vì TableView<String>, đổi thành TableView<Task>
    @FXML
    private TableColumn<Task, Task> taskColumn; // Thay vì TableColumn<String, String>, đổi thành TableColumn<Task, Task>

    private ObservableList<Task> taskList = FXCollections.observableArrayList();





    @FXML
    public void initialize() {
        // Cấu hình TableColumn để hiển thị FXML
        taskColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        taskColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add-task-cell.fxml"));
                        HBox cellBox = loader.load();
                        AddTaskCellController controller = loader.getController();
                        controller.setTaskData(task);
                        setGraphic(cellBox);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });

        tableadd.setItems(taskList);
        loadTasksFromDatabase();

        // Thêm cấp độ vào ComboBox
        choiceCard.getItems().addAll(
                new LevelColor(1, Color.web("#84fab0")),
                new LevelColor(2, Color.web("#74c0fc")),
                new LevelColor(3, Color.web("#ff7eb3"))
        );
        choiceCard.setValue(choiceCard.getItems().get(0));

        // Hiển thị màu trong ComboBox
        choiceCard.setCellFactory(lv -> new ListCell<>() {
            private final Rectangle rect = new Rectangle(150, 20);
            @Override
            protected void updateItem(LevelColor levelColor, boolean empty) {
                super.updateItem(levelColor, empty);
                if (empty || levelColor == null) {
                    setGraphic(null);
                } else {
                    rect.setFill(levelColor.getColor());
                    setGraphic(rect);
                }
            }
        });

        choiceCard.setButtonCell(new ListCell<>() {
            private final Rectangle rect = new Rectangle(100, 20);
            @Override
            protected void updateItem(LevelColor levelColor, boolean empty) {
                super.updateItem(levelColor, empty);
                if (empty || levelColor == null) {
                    setGraphic(null);
                } else {
                    rect.setFill(levelColor.getColor());
                    setGraphic(rect);
                }
            }
        });

        // Gán sự kiện cho nút
        saveBtn.setOnAction(e -> saveTaskToDatabase());
        delBtn.setOnAction(e -> clearInputFields());

        // Load dữ liệu khi khởi chạy
        loadTasksFromDatabase();
    }

    private void clearInputFields() {
        inputContent.clear();
        inputDes.clear();
        inputTime.clear();
        choiceCard.setValue(choiceCard.getItems().get(0));
    }

    private void saveTaskToDatabase() {
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {  // Kiểm tra user có đăng nhập không
            System.out.println("❌ Không có user đăng nhập! Không thể thêm task.");
            return;
        }

        String content = inputContent.getText().trim();
        String description = inputDes.getText().trim();
        int timeToComplete;

        if (content.isEmpty() || inputTime.getText().trim().isEmpty()) {
            System.out.println("⚠️ Vui lòng nhập đầy đủ nội dung và thời gian hoàn thành!");
            return;
        }

        LevelColor selectedLevel = choiceCard.getValue();
        int categoryLevel = selectedLevel.getLevel();

        try {
            timeToComplete = Integer.parseInt(inputTime.getText().trim());
            if (timeToComplete <= 0) {
                System.out.println("⚠️ Thời gian hoàn thành phải là số nguyên dương!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Lỗi: Thời gian phải là số nguyên!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO tasks (content, description, category_level, time_to_complete, status, progress, user_id) " +
                    "VALUES (?, ?, ?, ?, N'Chưa bắt đầu', 0, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, description);
            pstmt.setInt(3, categoryLevel);
            pstmt.setInt(4, timeToComplete);
            pstmt.setInt(5, userId); // 🆕 Thêm user_id vào câu lệnh INSERT

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Công việc đã được lưu vào database!");
                clearInputFields();
                loadTasksFromDatabase(); // 🔥 Cập nhật lại bảng ngay lập tức
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi lưu vào database: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadTasksFromDatabase() {
        taskList.clear();
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {
            System.out.println("❌ Không có user đăng nhập!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT content, description, category_level, time_to_complete, start_time, progress " +
                    "FROM tasks WHERE user_id = ?"; // 🆕 Chỉ lấy task của user hiện tại
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String content = rs.getString("content");
                String description = rs.getString("description");
                int level = rs.getInt("category_level");
                int timeToComplete = rs.getInt("time_to_complete");
                int progress = rs.getInt("progress");
                java.sql.Timestamp startTime = rs.getTimestamp("start_time");

                String status = "🛑 Chưa bắt đầu"; // Mặc định

                if (progress >= 100) {
                    status = "✅ Hoàn thành";
                } else if (startTime != null) {
                    long elapsedMinutes = (System.currentTimeMillis() - startTime.getTime()) / (60 * 1000);
                    if (elapsedMinutes < timeToComplete) {
                        status = "🟢 Đang thực hiện";
                    } else {
                        status = "🔴 Quá hạn";
                    }
                }

                taskList.add(new Task(content, description, level, timeToComplete, status, progress));
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tải dữ liệu từ database: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
