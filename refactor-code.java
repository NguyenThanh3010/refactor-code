package code;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManager {
    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JSONArray loadTasks() {
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = new JSONParser().parse(reader);
            if (obj instanceof JSONArray) return (JSONArray) obj;
        } catch (IOException | ParseException e) {
            System.err.println("Lỗi khi đọc file database: " + e.getMessage());
        }
        return new JSONArray();
    }

    private void saveTasks(JSONArray tasks) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasks.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Lỗi khi ghi vào file database: " + e.getMessage());
        }
    }

    private boolean isDuplicate(JSONArray tasks, String title, LocalDate dueDate) {
        return tasks.stream().anyMatch(obj -> {
            JSONObject task = (JSONObject) obj;
            return task.get("title").toString().equalsIgnoreCase(title)
                    && task.get("due_date").toString().equals(dueDate.format(DATE_FORMATTER));
        });
    }

    private boolean isValidPriority(String priority) {
        return priority.equals("Thấp") || priority.equals("Trung bình") || priority.equals("Cao");
    }

    public JSONObject addTask(String title, String description, String dueDateStr, String priority) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return null;
        }
        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Lỗi: Ngày đến hạn không được để trống.");
            return null;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (Exception e) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ.");
            return null;
        }

        if (!isValidPriority(priority)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ.");
            return null;
        }

        JSONArray tasks = loadTasks();
        if (isDuplicate(tasks, title, dueDate)) {
            System.out.println("Lỗi: Nhiệm vụ đã tồn tại.");
            return null;
        }

        JSONObject task = new JSONObject();
        task.put("id", UUID.randomUUID().toString());
        task.put("title", title);
        task.put("description", description);
        task.put("due_date", dueDate.format(DATE_FORMATTER));
        task.put("priority", priority);
        task.put("status", "Chưa hoàn thành");
        task.put("created_at", LocalDateTime.now().toString());
        task.put("last_updated_at", LocalDateTime.now().toString());

        tasks.add(task);
        saveTasks(tasks);

        System.out.println("Thêm nhiệm vụ thành công!");
        return task;
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        manager.addTask("Mua sách", "Sách lập trình Java", "2025-07-20", "Cao");
        manager.addTask("Mua sách", "Sách lập trình Java", "2025-07-20", "Cao"); 
        manager.addTask("", "Không có tiêu đề", "2025-07-21", "Thấp"); 
    }
}