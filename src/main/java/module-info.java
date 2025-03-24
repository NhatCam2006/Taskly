module com.example.taskmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires fontawesomefx;
    requires java.sql;
    requires org.json;
    requires jakarta.mail;
    requires okhttp3;
    opens model to javafx.base;
    opens application to javafx.fxml;
    exports application;
    exports controller;
    opens controller to javafx.fxml;
    opens service to javafx.fxml;
}