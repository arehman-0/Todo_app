package com.todoapp.dao;

import com.todoapp.model.TaskList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskListDAO {

    public List<TaskList> getAllLists() {
        List<TaskList> lists = new ArrayList<>();
        String sql = "SELECT * FROM task_lists ORDER BY created_at ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lists.add(extractTaskList(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lists;
    }

    public TaskList createList(TaskList list) {
        String sql = "INSERT INTO task_lists (name, color_hex, created_at) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, list.getName());
            pstmt.setString(2, list.getColorHex());
            pstmt.setTimestamp(3, Timestamp.valueOf(list.getCreatedAt()));

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                list.setId(rs.getLong(1));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateList(TaskList list) {
        String sql = "UPDATE task_lists SET name = ?, color_hex = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, list.getName());
            pstmt.setString(2, list.getColorHex());
            pstmt.setLong(3, list.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteList(Long listId) {
        String sql = "DELETE FROM task_lists WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, listId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private TaskList extractTaskList(ResultSet rs) throws SQLException {
        TaskList list = new TaskList();
        list.setId(rs.getLong("id"));
        list.setName(rs.getString("name"));
        list.setColorHex(rs.getString("color_hex"));
        list.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return list;
    }
}
