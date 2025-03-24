package controller;

import dao.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import java.util.Map;

public class ChartController {

    @FXML
    private PieChart taskPieChart;

    @FXML
    public void initialize() {
        loadChartData();
    }

    private void loadChartData() {
        Map<Integer, Integer> taskCountMap = TaskDAO.getTaskCountByCategory();

        for (Map.Entry<Integer, Integer> entry : taskCountMap.entrySet()) {
            String label = switch (entry.getKey()) {
                case 1 -> "Ít quang trọng";
                case 2 -> "Công việc đang chờ xử lý";
                case 3 -> "Cực kỳ quan trọng";
                default -> "Không xác định";
            };
            taskPieChart.getData().add(new PieChart.Data(label, entry.getValue()));
        }
    }
}
