package com.todoapp.util;

import com.todoapp.dao.DatabaseManager;
import com.todoapp.dao.TaskListDAO;
import com.todoapp.model.TaskList;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        try {
            createDatabase();
            createTables();
            createDefaultLists();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }

    private static void createDatabase() throws SQLException {
        String createDbUrl = "jdbc:mysql://localhost:3306/";
        String dbUser = "root"; // Change to your MySQL username
        String dbPassword = ""; // Change to your MySQL password

        try (Connection conn = java.sql.DriverManager.getConnection(createDbUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS todo_app");
            System.out.println("Database 'todo_app' created or already exists");
        }
    }

    private static void createTables() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create task_lists table
            String createTaskListsTable = """
                CREATE TABLE IF NOT EXISTS task_lists (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    color_hex VARCHAR(7) DEFAULT '#2564CF',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.executeUpdate(createTaskListsTable);

            // Create tasks table
            String createTasksTable = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    list_id BIGINT NOT NULL,
                    title VARCHAR(500) NOT NULL,
                    notes TEXT,
                    completed BOOLEAN DEFAULT FALSE,
                    important BOOLEAN DEFAULT FALSE,
                    due_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    completed_at TIMESTAMP NULL,
                    FOREIGN KEY (list_id) REFERENCES task_lists(id) ON DELETE CASCADE
                )
            """;
            stmt.executeUpdate(createTasksTable);

            System.out.println("Database tables created successfully");
        }
    }

    private static void createDefaultLists() {
        TaskListDAO listDAO = new TaskListDAO();

        if (listDAO.getAllLists().isEmpty()) {
            listDAO.createList(new TaskList("Tasks", "#2564CF"));
            listDAO.createList(new TaskList("Personal", "#E74856"));
            listDAO.createList(new TaskList("Work", "#00CC6A"));
            System.out.println("Default task lists created");
        }
    }
}