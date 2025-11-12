package com.todoapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    private Long id;
    private Long listId;
    private String title;
    private String notes;
    private boolean completed;
    private boolean important;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Task() {
        this.createdAt = LocalDateTime.now();
    }

    public Task(String title) {
        this();
        this.title = title;
    }

    // Getters
    public Long getId() { return id; }
    public Long getListId() { return listId; }
    public String getTitle() { return title; }
    public String getNotes() { return notes; }
    public boolean isCompleted() { return completed; }
    public boolean isImportant() { return important; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setListId(Long listId) { this.listId = listId; }
    public void setTitle(String title) { this.title = title; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }
    public void setImportant(boolean important) { this.important = important; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    @Override
    public String toString() {
        return title;
    }
}