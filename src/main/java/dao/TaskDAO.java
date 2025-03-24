package dao;

import database.DatabaseConnection;
import model.Task;
import utils.SessionManager; // 🆕 Import để lấy user đăng nhập

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskDAO {

    // 🆕 Lấy task theo user_id
    public static List<Task> getTasksByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT id, content, description, category_level, time_to_complete, start_time, status, progress, remaining_time, user_id FROM tasks WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getString("content"),
                        rs.getString("description"),
                        rs.getInt("category_level"),
                        rs.getInt("time_to_complete"),
                        rs.getTimestamp("start_time"),
                        rs.getString("status"),
                        rs.getInt("progress"),
                        rs.getInt("remaining_time"),
                        rs.getInt("user_id") // 🆕 Thêm user_id
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // 🆕 Thêm task mới với user_id
    public static boolean addTask(Task task) {
        String sql = "INSERT INTO tasks (content, description, category_level, time_to_complete, status, progress, remaining_time, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getContent());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.getCategoryLevel());
            pstmt.setInt(4, task.getTimeToComplete());
            pstmt.setString(5, task.getStatus());
            pstmt.setInt(6, task.getProgress());
            pstmt.setInt(7, task.getRemainingTime());
            pstmt.setInt(8, task.getUserId()); // 🆕 Thêm user_id

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void updateTaskStatus(int taskId, String status, Timestamp startTime) {
        String query = "UPDATE tasks SET status = ?, start_time = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setTimestamp(2, startTime);
            pstmt.setInt(3, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateTaskProgress(int taskId, int remainingTime, int progress) {
        String sql = "UPDATE tasks SET remaining_time = ?, progress = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, remainingTime);
            pstmt.setInt(2, progress);
            pstmt.setInt(3, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🆕 Thống kê số lượng task theo category nhưng chỉ của user đăng nhập
    public static Map<Integer, Integer> getTaskCountByCategory() {
        Map<Integer, Integer> taskCountMap = new HashMap<>();
        String query = "SELECT category_level, COUNT(*) AS count FROM tasks WHERE user_id = ? GROUP BY category_level";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int userId = SessionManager.getCurrentUserId(); // 🆕 Lấy user đang đăng nhập
            if (userId == -1) {
                System.out.println("❌ Không có user đăng nhập!");
                return taskCountMap;
            }

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                taskCountMap.put(rs.getInt("category_level"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return taskCountMap;
    }
}
