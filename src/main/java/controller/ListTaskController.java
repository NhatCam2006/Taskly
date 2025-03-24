    package controller;

    import dao.TaskDAO;
    import javafx.application.Platform;
    import javafx.beans.property.SimpleObjectProperty;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.control.*;
    import javafx.scene.layout.AnchorPane;
    import javafx.scene.layout.Pane;
    import model.Task;
    import utils.SessionManager;

    import java.awt.*;
    import java.io.IOException;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.Comparator;
    import java.util.Locale;
    import java.util.Timer;
    import java.util.TimerTask;

    import javafx.scene.control.Label;
    import org.json.JSONObject;

    import java.io.BufferedReader;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import javafx.scene.image.Image;  // Để sử dụng lớp Image cho việc tạo đối tượng hình ảnh
    import javafx.scene.image.ImageView;  // Để sử dụng lớp ImageView cho việc hiển thị hình ảnh trong JavaFX
    import javafx.scene.layout.Pane;  // Để thao tác với Pane, nơi bạn sẽ thêm ImageView



    public class ListTaskController {
        private static ImageView backgroundImageView;  // Sửa kiểu từ javax.swing.text.html.ImageView thành javafx.scene.image.ImageView

        @FXML private Label dayLabel;
        @FXML private Label timeLabel;
        @FXML private Label weatherLabel;
        @FXML
        private Pane pane;

        private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi"));
        private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        @FXML private TableView<Task> taskTable;
        @FXML private TableColumn<Task, Task> taskColumn;  // Chỉnh sửa kiểu dữ liệu
        private ObservableList<Task> taskList = FXCollections.observableArrayList();

        public void initialize() {
            updateDateTime();
            startClock();
            fetchWeather();
            loadBackgroundImage();


            taskTable.setFixedCellSize(80);
            // Cấu hình TableColumn để hiển thị FXML
            taskColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));

    // Thiết lập Comparator để sắp xếp theo categoryLevel
            taskColumn.setComparator(Comparator.comparing(Task::getCategoryLevel));
            taskTable.getSortOrder().add(taskColumn);


            taskColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Task task, boolean empty) {
                    super.updateItem(task, empty);
                    if (empty || task == null) {
                        setGraphic(null);
                    } else {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/list-task-cell.fxml"));
                            AnchorPane cellBox = loader.load();
                            ListTaskCellController controller = loader.getController();
                            controller.setTaskData(task);
                            setGraphic(cellBox);
                        } catch (IOException e) {
                            e.printStackTrace();
                            setGraphic(null);
                        }
                    }
                }
            });

            // Load dữ liệu từ database
            loadTaskData();
        }

        private void updateDateTime() {
            LocalDateTime now = LocalDateTime.now();
            dayLabel.setText(dayFormatter.format(now));
            timeLabel.setText(timeFormatter.format(now));
        }

        private void startClock() {
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> updateDateTime());
                }
            }, 0, 1000);
        }

        private static long lastFetchTime = 0;
        private static String cachedWeather = "";

        private void fetchWeather() {
            new Thread(() -> {
                try {
                    long currentTime = System.currentTimeMillis();

                    // Nếu dữ liệu chưa hết hạn (30 phút), dùng cache
                    if (currentTime - lastFetchTime < 30 * 60 * 1000 && !cachedWeather.isEmpty()) {
                        Platform.runLater(() -> weatherLabel.setText(cachedWeather));
                        return;
                    }

                    // 🌍 Lấy vị trí từ ipinfo.io
                    String ipApiUrl = "https://ipinfo.io/json";
                    String ipResponseString = getJsonFromUrl(ipApiUrl);
                    JSONObject ipResponse = new JSONObject(ipResponseString);

                    // Lấy toạ độ từ chuỗi "loc": "lat,lon"
                    String[] location = ipResponse.getString("loc").split(",");
                    double lat = Double.parseDouble(location[0]);
                    double lon = Double.parseDouble(location[1]);

                    // 🌦️ Lấy dữ liệu thời tiết từ Open-Meteo
                    String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                            "&longitude=" + lon + "&current_weather=true&timezone=auto";
                    String weatherResponseString = getJsonFromUrl(weatherApiUrl);
                    JSONObject weatherResponse = new JSONObject(weatherResponseString);

                    JSONObject currentWeather = weatherResponse.getJSONObject("current_weather");
                    double temp = currentWeather.getDouble("temperature");
                    int weatherCode = currentWeather.getInt("weathercode");
                    String weatherDescription = getWeatherDescription(weatherCode);

                    // Cập nhật cache
                    cachedWeather = "🌤 " + temp + "°C - " + weatherDescription;
                    lastFetchTime = System.currentTimeMillis();

                    // Cập nhật giao diện
                    Platform.runLater(() -> {
                        weatherLabel.setText(cachedWeather);
                        changeBackgroundImage(weatherCode); // 🟢 Cập nhật video
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> weatherLabel.setText("Không lấy được thời tiết!"));
                }
            }).start();
        }

        private void changeBackgroundImage(int weatherCode) {
            String imageFile;

            // Chọn ảnh dựa trên mã thời tiết
            switch (weatherCode) {
                case 0 -> imageFile = "cleadr.jpg";
                case 1, 2, 3 -> imageFile = "cloudy.jpg";
                case 45, 48 -> imageFile = "foggy.jpg";
                case 51, 53, 55, 61, 63, 65 -> imageFile = "rainy.jpg";
                case 80, 81, 82 -> imageFile = "storm.jpg";
                default -> imageFile = "default.jpg";
            }

            // Nếu đã có ảnh nền, không cần tạo mới
            if (backgroundImageView != null) {
                if (!pane.getChildren().contains(backgroundImageView)) {
                    pane.getChildren().add(backgroundImageView);
                }
                return;
            }

            // Tạo ảnh mới nếu chưa có
            URL imageURL = getClass().getResource("/img/" + imageFile);
            if (imageURL == null) {
                System.out.println("Không tìm thấy ảnh: " + imageFile);
                return;
            }

            Image image = new Image(imageURL.toExternalForm());
            backgroundImageView = new ImageView(image);

            // Điều chỉnh kích thước ảnh phù hợp với pane
            backgroundImageView.setFitWidth(pane.getWidth());
            backgroundImageView.setFitHeight(pane.getHeight());
            backgroundImageView.setPreserveRatio(false);

            // Thêm ảnh nền vào pane
            pane.getChildren().add(backgroundImageView);
        }



        public static String getJsonFromUrl(String urlString) throws Exception {
            int retryCount = 0;
            int maxRetries = 3;

            while (retryCount < maxRetries) {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // 🛠️ Thêm nhiều Headers để tránh bị chặn
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Cache-Control", "no-cache");

                int responseCode = conn.getResponseCode();
                if (responseCode == 429) {
                    System.out.println("Lỗi HTTP 429, đợi 10 giây và thử lại...");
                    Thread.sleep(10000);
                    retryCount++;
                } else if (responseCode != 200) {
                    throw new RuntimeException("Lỗi HTTP: " + responseCode);
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                }
            }

            throw new RuntimeException("Không thể lấy dữ liệu sau nhiều lần thử!");
        }

        private String getWeatherDescription(int code) {
            return switch (code) {
                case 0 -> "Trời quang";
                case 1, 2, 3 -> "Ít mây";
                case 45, 48 -> "Sương mù";
                case 51, 53, 55 -> "Mưa phùn";
                case 61, 63, 65 -> "Mưa rào";
                case 80, 81, 82 -> "Mưa rào lớn";
                default -> "Không rõ";
            };
        }
        private void loadBackgroundImage() {
            int weatherCode = getCurrentWeatherCode();  // 🔥 Lấy mã thời tiết hiện tại
            changeBackgroundImage(weatherCode);
        }

        private int getCurrentWeatherCode() {
            // Giả sử bạn có thể lấy mã thời tiết từ biến đã lưu trước đó
            return 0; // 🌤 Mặc định trời quang (thay bằng mã thực tế)
        }


        private void loadTaskData() {
            int userId = SessionManager.getCurrentUserId();
            if (userId == -1) {
                System.out.println("❌ Không có user đăng nhập!");
                return;
            }

            taskList.clear();
            taskList.addAll(TaskDAO.getTasksByUserId(userId)); // 🆕 Chỉ lấy task của user hiện tại
            taskTable.setItems(taskList);
        }
    }
