package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class MainController {
    @FXML
    private AnchorPane contentPane; // Trùng với fx:id trong main-layout.fxml

    // Hàm để thay đổi nội dung trung tâm
    public void switchToView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
            Parent newView = loader.load();
            contentPane.getChildren().setAll(newView); // Load nội dung mới vào
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
