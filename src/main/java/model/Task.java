package model;

import java.sql.Timestamp;

public class Task {
    private int id;
    private String content;
    private String description;
    private int categoryLevel;
    private int timeToComplete; // Số phút để hoàn thành
    private Timestamp startTime;
    private String status;
    private int progress;
    private int remainingTime;
    private int userId;

    public Task(int id, String content, String description, int categoryLevel, int timeToComplete, String status, int progress, int userId) {
        this.id = id;
        this.content = content;
        this.description = description;
        this.categoryLevel = categoryLevel;
        this.timeToComplete = timeToComplete;
        this.status = status;
        this.progress = progress;
        this.userId = userId;
    }
    public Task(int id, String content, String description, int categoryLevel, int timeToComplete,
                Timestamp startTime, String status, int progress, int remainingTime, int userId) {
        this.id = id;
        this.content = content;
        this.description = description;
        this.categoryLevel = categoryLevel;
        this.timeToComplete = timeToComplete;
        this.startTime = startTime;
        this.status = status;
        this.progress = progress;
        this.remainingTime = remainingTime;
        this.userId = userId; // 🆕 Gán userId
    }

    // 🆕 Constructor dành cho task mới (khi user tạo task)
    public Task(String content, String description, int categoryLevel, int timeToComplete, int userId) {
        this.content = content;
        this.description = description;
        this.categoryLevel = categoryLevel;
        this.timeToComplete = timeToComplete;
        this.status = "Chưa bắt đầu";
        this.progress = 0;
        this.startTime = null;
        this.remainingTime = timeToComplete;
        this.userId = userId; // 🆕 Gán userId
    }
    // Constructor dành cho Task lấy từ database nhưng không cần ID
    public Task(String content, String description, int categoryLevel, int timeToComplete, String status, int progress) {
        this.content = content;
        this.description = description;
        this.categoryLevel = categoryLevel;
        this.timeToComplete = timeToComplete;
        this.status = status;
        this.progress = progress;
        this.startTime = null;
    }

    // Constructor đầy đủ (dành cho database)
    public Task(int id, String content, String description, int categoryLevel, int timeToComplete, Timestamp startTime, String status, int progress) {
        this.id = id;
        this.content = content;
        this.description = description;
        this.categoryLevel = categoryLevel;
        this.timeToComplete = timeToComplete;
        this.startTime = startTime;
        this.status = status;
        this.progress = progress;
        this.remainingTime = calculateRemainingTime();
    }

    // Constructor không có startTime (phù hợp khi không cần lấy thời gian bắt đầu từ database)
    public Task(int id, String content, String description, int categoryLevel, int timeToComplete, String status, int progress) {
        this.id = id;
        this.content = content;
        this.description = description;
        this.categoryLevel = categoryLevel;
        this.timeToComplete = timeToComplete;
        this.status = status;
        this.progress = progress;
        this.remainingTime = calculateRemainingTime();
    }

    // Constructor cho Task mới (chưa bắt đầu)
    public Task(String content, String description, int categoryLevel, int timeToComplete) {
        this.content = content;
        this.description = description;
        this.categoryLevel = categoryLevel;
        this.timeToComplete = timeToComplete;
        this.status = "Chưa bắt đầu";
        this.progress = 0;
        this.startTime = null; // Task chưa bắt đầu thì để null
        this.remainingTime = timeToComplete; // Chưa bắt đầu thì còn nguyên thời gian
    }

    // Khi bắt đầu task, cập nhật thời gian
    public void startTask() {
        this.status = "Đang thực hiện";
        this.startTime = new Timestamp(System.currentTimeMillis()); // Lưu timestamp hiện tại
    }

    // Cập nhật tiến độ dựa trên thời gian đã trôi qua
    public void updateProgress() {
        if (startTime == null) return; // Nếu chưa bắt đầu thì không cập nhật

        long elapsedMillis = System.currentTimeMillis() - startTime.getTime();
        int elapsedMinutes = (int) (elapsedMillis / 60000); // Chuyển từ ms sang phút

        progress = (int) (((double) elapsedMinutes / timeToComplete) * 100);
        remainingTime = timeToComplete - elapsedMinutes;

        if (progress >= 100) {
            progress = 100;
            status = "Đã hoàn thành";
            remainingTime = 0;
        }
    }

    // Tính toán thời gian còn lại dựa trên thời gian bắt đầu
    private int calculateRemainingTime() {
        if (startTime == null) return timeToComplete;
        long elapsedMillis = System.currentTimeMillis() - startTime.getTime();
        int elapsedMinutes = (int) (elapsedMillis / 60000);
        return Math.max(0, timeToComplete - elapsedMinutes);
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
        this.remainingTime = calculateRemainingTime(); // Cập nhật thời gian còn lại
    }

    public int getTimeToComplete() {
        return timeToComplete;
    }

    public void setTimeToComplete(int minutes) {
        this.timeToComplete = minutes;
        this.remainingTime = calculateRemainingTime();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getCategoryLevel() {
        return categoryLevel;
    }

    public void setCategoryLevel(int categoryLevel) {
        this.categoryLevel = categoryLevel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
