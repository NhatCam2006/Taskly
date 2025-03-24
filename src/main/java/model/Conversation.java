package model;  // (Tạo trong thư mục `model` để gọn gàng)

public class Conversation {
    private int id;
    private String title;

    public Conversation(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
