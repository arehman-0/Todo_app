package com.todoapp.dao;

import com.todoapp.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public List<Task> getTasksByListId(Long listId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE list_id = ? ORDER BY completed ASC, created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, listId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public List<Task> getImportantTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE important = TRUE ORDER BY completed ASC, created_at DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public Task createTask(Task task) {
        String sql = "INSERT INTO tasks (list_id, title, notes, completed, important, due_date, created_at, completed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, task.getListId());
            pstmt.setString(2, task.getTitle());
            pstmt.setString(3, task.getNotes());
            pstmt.setBoolean(4, task.isCompleted());
            pstmt.setBoolean(5, task.isImportant());
            pstmt.setDate(6, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            pstmt.setTimestamp(7, Timestamp.valueOf(task.getCreatedAt()));
            pstmt.setTimestamp(8, task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null);

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                task.setId(rs.getLong(1));
            }

            return task;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, notes = ?, completed = ?, important = ?, due_date = ?, completed_at = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getNotes());
            pstmt.setBoolean(3, task.isCompleted());
            pstmt.setBoolean(4, task.isImportant());
            pstmt.setDate(5, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            pstmt.setTimestamp(6, task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null);
            pstmt.setLong(7, task.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTask(Long taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTaskCountByListId(Long listId) {
        String sql = "SELECT COUNT(*) as count FROM tasks WHERE list_id = ? AND completed = FALSE";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, listId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Task extractTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setListId(rs.getLong("list_id"));
        task.setTitle(rs.getString("title"));
        task.setNotes(rs.getString("notes"));
        task.setCompleted(rs.getBoolean("completed"));
        task.setImportant(rs.getBoolean("important"));

        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            task.setDueDate(dueDate.toLocalDate());
        }

        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            task.setCompletedAt(completedAt.toLocalDateTime());
        }

        return task;
    }
}