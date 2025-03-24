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

public class EditTaskController {

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
    private TableView<Task> tableEdit; // Đổi tên biến bảng thành tableEdit
    @FXML
    private TableColumn<Task, Task> taskColumn;

    private ObservableList<Task> taskList = FXCollections.observableArrayList();

    // Biến để lưu trữ task được chọn (giúp cập nhật hoặc xóa)
    private Task selectedTask = null;

    @FXML
    public void initialize() {
        // Cấu hình TableColumn sử dụng file FXML edit-task-cell.fxml
        taskColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        taskColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit-task-cell.fxml"));
                        HBox cellBox = loader.load();
                        EditTaskCellController controller = loader.getController();
                        controller.setTaskData(task);
                        setGraphic(cellBox);
                    } catch (IOException e) {
                        e.printStackTrace();
                        setGraphic(null);
                    }
                }
            }
        });
        tableEdit.setItems(taskList);
        loadTasksFromDatabase();

        // Thêm các mức độ vào ComboBox
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

        // Lắng nghe sự kiện chọn task từ bảng
        tableEdit.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTask = newSelection;
                inputContent.setText(selectedTask.getContent());
                inputDes.setText(selectedTask.getDescription());
                inputTime.setText(String.valueOf(selectedTask.getTimeToComplete()));
                // Đặt giá trị cho comboBox dựa trên category_level của task
                int level = selectedTask.getCategoryLevel();
                for (LevelColor lc : choiceCard.getItems()) {
                    if (lc.getLevel() == level) {
                        choiceCard.setValue(lc);
                        break;
                    }
                }
            }
        });

        saveBtn.setOnAction(e -> saveTaskToDatabase());
        delBtn.setOnAction(e -> deleteTaskFromDatabase());
    }

    // Hàm xóa task đã chọn khỏi database
    private void deleteTaskFromDatabase() {
        if (selectedTask == null) {
            System.out.println("⚠️ Chưa chọn task để xóa!");
            return;
        }
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {
            System.out.println("❌ Không có user đăng nhập! Không thể xóa task.");
            return;
        }
        if (selectedTask.getUserId() != userId) {
            System.out.println("❌ Task không thuộc về bạn! Không thể xóa.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?"; // Kiểm tra user_id khi xóa
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selectedTask.getId());
            pstmt.setInt(2, userId);  // Thêm kiểm tra user_id
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Task đã được xóa khỏi database!");
                loadTasksFromDatabase();
                clearInputFields(); // Xóa nội dung các ô nhập liệu sau khi xóa task
            } else {
                System.out.println("❌ Không thể xóa task! Task không thuộc về bạn hoặc không tồn tại.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi xóa task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm tiện ích để xóa sạch các ô nhập liệu và reset task được chọn
    private void clearInputFields() {
        inputContent.clear();
        inputDes.clear();
        inputTime.clear();
        choiceCard.setValue(choiceCard.getItems().get(0));
        selectedTask = null;
        tableEdit.getSelectionModel().clearSelection();
    }

    // Hàm cập nhật task đã chọn (sửa dữ liệu) vào database
    private void saveTaskToDatabase() {
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {  // Kiểm tra user có đăng nhập không
            System.out.println("❌ Không có user đăng nhập! Không thể sửa task.");
            return;
        }
        if (selectedTask == null) {
            System.out.println("⚠️ Chưa chọn task để sửa!");
            return;
        }
        if (selectedTask.getUserId() != userId) {
            System.out.println("❌ Task không thuộc về bạn! Không thể sửa.");
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
            String sql = "UPDATE tasks SET content = ?, description = ?, category_level = ?, time_to_complete = ?, " +
                    "status = N'Chưa bắt đầu', progress = 0 WHERE id = ? AND user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, description);
            pstmt.setInt(3, categoryLevel);
            pstmt.setInt(4, timeToComplete);
            pstmt.setInt(5, selectedTask.getId());  // Cập nhật task theo id
            pstmt.setInt(6, userId); // Kiểm tra user_id khi sửa task

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Task đã được cập nhật vào database!");
                clearInputFields();
                loadTasksFromDatabase(); // Cập nhật lại bảng ngay lập tức
            } else {
                System.out.println("❌ Task không thuộc về bạn hoặc không tồn tại.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi cập nhật vào database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm tải lại danh sách task từ database (bao gồm trường id và user_id)
    private void loadTasksFromDatabase() {
        taskList.clear();
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {
            System.out.println("❌ Không có user đăng nhập!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, content, description, category_level, time_to_complete, start_time, progress, user_id " +
                    "FROM tasks WHERE user_id = ?"; // Chỉ lấy task của user hiện tại
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                String description = rs.getString("description");
                int level = rs.getInt("category_level");
                int timeToComplete = rs.getInt("time_to_complete");
                int progress = rs.getInt("progress");
                java.sql.Timestamp startTime = rs.getTimestamp("start_time");
                int taskUserId = rs.getInt("user_id");

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

                // Khởi tạo Task với id và user_id (đảm bảo lớp Task có constructor phù hợp)
                taskList.add(new Task(id, content, description, level, timeToComplete, status, progress, taskUserId));
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tải dữ liệu từ database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
