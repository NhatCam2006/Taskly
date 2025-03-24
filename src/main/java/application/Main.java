package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Tải FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Thiết lập cửa sổ không có thanh tiêu đề và nút điều khiển
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(scene);

            // Hiển thị cửa sổ
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
