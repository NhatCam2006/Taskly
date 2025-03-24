package controller;

import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Task;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Conversation;


import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.time.LocalDateTime;

public class ChatbotController {
    @FXML
    private TableView<Conversation> conversationTable;
    @FXML
    private TableColumn<Conversation, String> titleColumn;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField inputField;
    @FXML
    private Button sendButton;

    private final ObservableList<Conversation> conversations = FXCollections.observableArrayList();
    private int selectedConversationId = -1;
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyCxE4zuJlZPQfoOuhpIh3ZZTEHNwcPyjdQ";
    private final OkHttpClient client = new OkHttpClient();

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        conversationTable.setItems(conversations);
        conversationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedConversationId = newVal.getId();
                loadChatHistory(selectedConversationId);
            }
        });

        loadConversations();

        // Nếu không có hội thoại nào, tạo hội thoại mới
        if (conversations.isEmpty()) {
            int userId = SessionManager.getCurrentUserId();
            if (userId != -1) {
                int newConversationId = insertNewConversation(userId, "Hội thoại mới");
                if (newConversationId != -1) {
                    Conversation defaultConversation = new Conversation(newConversationId, "Hội thoại mới");
                    conversations.add(defaultConversation);
                    conversationTable.refresh();
                    conversationTable.getSelectionModel().select(defaultConversation);
                    selectedConversationId = newConversationId;

                    // Gửi tin nhắn chào tự động
                    autoSendWelcomeMessage(userId, newConversationId);
                }
            }
        }
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        conversationTable.setItems(conversations);
        conversationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedConversationId = newVal.getId();
                loadChatHistory(selectedConversationId);
            }
        });

        loadConversations();

        // Bắt sự kiện nhấn Enter để gửi tin nhắn
        inputField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleSendMessage();
            }
        });
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        conversationTable.setItems(conversations);
        conversationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedConversationId = newVal.getId();
                loadChatHistory(selectedConversationId);
            }
        });

        loadConversations();
    }

    // Biến trạng thái toàn cục trong controller (khai báo bên ngoài phương thức handleSendMessage)
    private boolean waitingForTaskDetails = false;

    @FXML
    private void handleSendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty() || selectedConversationId == -1) return;

        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {
            chatArea.appendText("❌ Bạn chưa đăng nhập!\n");
            return;
        }

        // Hiển thị tin nhắn của người dùng và lưu vào lịch sử chat
        chatArea.appendText("Bạn: " + userMessage + "\n");
        saveChatToDatabase(userId, selectedConversationId, userMessage, "user");
        inputField.clear();

        String promptForAI;

        // Nếu người dùng nhập "Thêm công việc" và chưa chờ thông tin chi tiết
        if (userMessage.equalsIgnoreCase("Thêm công việc") && !waitingForTaskDetails) {
            // Hỏi lại người dùng thông tin công việc thay vì gửi đến AI
            chatArea.appendText("AI: Bạn muốn thêm công việc nào?\n");
            waitingForTaskDetails = true;
            return;
        }

        // Nếu đang chờ thông tin chi tiết công việc (từ bước hỏi ở trên)
        if (waitingForTaskDetails) {
            // Gắn thêm prompt yêu cầu trả về JSON vào sau tin nhắn người dùng
            promptForAI = userMessage + " " +
                    "và trả về JSON theo định dạng: {\"title\": \"...\", \"description\": \"...\", \"time\": ..., \"priority\":...} Hãy trả về JSON mà không dùng backtick hay bất kỳ đánh dấu code block nào. Riêng priority chỉ chọn số theo cấp độ 1 - 3";
            waitingForTaskDetails = false; // Reset trạng thái sau khi nhận được chi tiết từ người dùng
        } else {
            // Nếu không phải trường hợp thêm công việc, dùng tin nhắn của người dùng làm prompt
            promptForAI = userMessage;
        }

        final String finalPromptForAI = promptForAI;
        new Thread(() -> {
            String botResponse = sendMessage(finalPromptForAI);

            // Làm sạch chuỗi ngay sau khi lấy botResponse
            String cleanedResponse = botResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            javafx.application.Platform.runLater(() -> {
                chatArea.appendText("AI: " + cleanedResponse + "\n");
                saveChatToDatabase(userId, selectedConversationId, cleanedResponse, "bot");

                // Kiểm tra nếu phản hồi từ AI là JSON chứa thông tin công việc
                if (isTaskJson(cleanedResponse)) {
                    try {
                        JSONObject taskData = new JSONObject(cleanedResponse);
                        saveTaskToDatabase(taskData);
                        chatArea.appendText("✅ Công việc đã được thêm thành công!\n");
                    } catch (Exception e) {
                        chatArea.appendText("❌ Lỗi xử lý dữ liệu công việc!\n");
                        e.printStackTrace();
                    }
                }
            });
        }).start();
    }


    private boolean isTaskJson(String response) {
        try {
            JSONObject json = new JSONObject(response);
            // Kiểm tra có đủ các trường: title, description, time, priority
            return json.has("title") && json.has("description") && json.has("time") && json.has("priority");
        } catch (JSONException e) {
            return false;
        }
    }
    private void saveTaskToDatabase(JSONObject taskData) {
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {
            System.out.println("❌ Không có user đăng nhập! Không thể thêm task.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO tasks (content, description, category_level, time_to_complete, status, progress, remaining_time, user_id) " +
                    "VALUES (?, ?, ?, ?, N'Chưa bắt đầu', 0, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, taskData.getString("title"));
            pstmt.setString(2, taskData.getString("description"));
            pstmt.setInt(3, taskData.getInt("priority")); // category_level
            pstmt.setInt(4, taskData.getInt("time"));       // time_to_complete
            pstmt.setInt(5, taskData.getInt("time"));       // remaining_time = time_to_complete
            pstmt.setInt(6, userId);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Công việc đã được lưu vào database!");
            }
        } catch (SQLException | JSONException e) {
            System.out.println("❌ Lỗi khi lưu vào database: " + e.getMessage());
        }
    }





    private void autoSendWelcomeMessage(int userId, int conversationId) {
        String welcomeMessage = "Xin chào, tôi là TaskBot. Tôi có thể giúp gì cho bạn?";

        // Hiển thị tin nhắn chào trên giao diện
        javafx.application.Platform.runLater(() -> {
            chatArea.appendText("AI: " + welcomeMessage + "\n");
        });

        // Lưu vào database
        saveChatToDatabase(userId, conversationId, welcomeMessage, "bot");
    }


    private String sendMessage(String userMessage) {
        JSONObject requestBody = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject textPart = new JSONObject().put("text", userMessage);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(textPart));
        contents.put(content);
        requestBody.put("contents", contents);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "❌ Lỗi khi gửi yêu cầu: " + response.message();
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);

            if (jsonResponse.has("candidates")) {
                JSONArray candidates = jsonResponse.getJSONArray("candidates");
                if (!candidates.isEmpty()) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    if (firstCandidate.has("content") && firstCandidate.getJSONObject("content").has("parts")) {
                        JSONArray parts = firstCandidate.getJSONObject("content").getJSONArray("parts");
                        if (!parts.isEmpty()) {
                            return parts.getJSONObject(0).getString("text");
                        }
                    }
                }
            }
            return "❌ AI không trả về phản hồi hợp lệ!";
        } catch (IOException e) {
            return "❌ Lỗi kết nối API: " + e.getMessage();
        }
    }




    @FXML
    private void handleAddConversation() {
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) {
            chatArea.appendText("❌ Bạn chưa đăng nhập!\n");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("Cuộc trò chuyện mới");
        dialog.setTitle("Thêm hội thoại");
        dialog.setHeaderText("Nhập tiêu đề cho hội thoại:");

        dialog.showAndWait().ifPresent(title -> {
            // Kiểm tra xem cuộc trò chuyện đã tồn tại chưa
            for (Conversation c : conversations) {
                if (c.getTitle().equalsIgnoreCase(title)) {
                    chatArea.appendText("❌ Hội thoại này đã tồn tại!\n");
                    return;
                }
            }

            int conversationId = insertNewConversation(userId, title);
            if (conversationId != -1) {
                Conversation newConversation = new Conversation(conversationId, title);
                conversations.add(newConversation);
                conversationTable.refresh(); // Cập nhật bảng
                conversationTable.getSelectionModel().select(newConversation); // Chọn hội thoại mới
            }
        });
    }


    @FXML
    private void handleDeleteConversation() {
        Conversation selected = conversationTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa hội thoại");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa hội thoại này?");
        alert.setContentText("Hành động này không thể hoàn tác!");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteConversation(selected.getId());
                conversations.remove(selected);
                conversationTable.refresh();
                chatArea.clear(); // Xóa nội dung chat
            }
        });
    }


    private void saveChatToDatabase(int userId, int conversationId, String message, String sender) {
        String query = "INSERT INTO chat_history (user_id, conversation_id, message, sender, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, conversationId);
            pstmt.setString(3, message);
            pstmt.setString(4, sender);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi lưu lịch sử: " + e.getMessage());
        }
    }

    private void loadChatHistory(int conversationId) {
        chatArea.clear();
        String query = "SELECT message, sender FROM chat_history WHERE conversation_id = ? ORDER BY timestamp";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, conversationId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                chatArea.appendText((sender.equals("user") ? "Bạn: " : "AI: ") + message + "\n");
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tải lịch sử: " + e.getMessage());
        }
    }

    private void loadConversations() {
        int userId = SessionManager.getCurrentUserId();
        if (userId == -1) return;

        conversations.clear(); // Xóa danh sách cũ để tránh nhân bản

        String query = "SELECT id, title FROM conversations WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                conversations.add(new Conversation(rs.getInt("id"), rs.getString("title")));
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi tải danh sách hội thoại: " + e.getMessage());
        }
    }


    private int insertNewConversation(int userId, String title) {
        // Kiểm tra xem hội thoại đã tồn tại chưa
        String checkQuery = "SELECT id FROM conversations WHERE user_id = ? AND title = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setString(2, title);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // Trả về ID hội thoại đã tồn tại
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi kiểm tra hội thoại: " + e.getMessage());
        }

        // Nếu chưa tồn tại, thêm hội thoại mới
        String insertQuery = "INSERT INTO conversations (user_id, title) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setInt(1, userId);
            insertStmt.setString(2, title);
            insertStmt.executeUpdate();

            ResultSet rs = insertStmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi thêm hội thoại: " + e.getMessage());
            return -1;
        }
    }


    private void deleteConversation(int conversationId) {
        String deleteChatHistoryQuery = "DELETE FROM chat_history WHERE conversation_id = ?";
        String deleteConversationQuery = "DELETE FROM conversations WHERE id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            // Xóa tin nhắn trước
            try (PreparedStatement pstmt1 = conn.prepareStatement(deleteChatHistoryQuery)) {
                pstmt1.setInt(1, conversationId);
                pstmt1.executeUpdate();
            }

            // Xóa cuộc hội thoại
            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteConversationQuery)) {
                pstmt2.setInt(1, conversationId);
                pstmt2.setInt(2, SessionManager.getCurrentUserId()); // Xóa theo user_id
                pstmt2.executeUpdate();
            }

            conn.commit(); // Xác nhận transaction
        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi xóa hội thoại: " + e.getMessage());
        }
    }

}
