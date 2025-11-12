package com.todoapp.model;

import java.time.LocalDateTime;

public class TaskList {
    private Long id;
    private String name;
    private String colorHex;
    private LocalDateTime createdAt;

    public TaskList() {
        this.createdAt = LocalDateTime.now();
        this.colorHex = "#2564CF"; // Default blue
    }

    public TaskList(String name) {
        this();
        this.name = name;
    }

    public TaskList(String name, String colorHex) {
        this();
        this.name = name;
        this.colorHex = colorHex;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}