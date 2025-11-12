package com.todoapp.controller;

import com.todoapp.dao.TaskDAO;
import com.todoapp.dao.TaskListDAO;
import com.todoapp.model.Task;
import com.todoapp.model.TaskList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController {

    @FXML private VBox sidebarListsContainer;
    @FXML private VBox tasksContainer;
    @FXML private TextField newTaskField;
    @FXML private Label listTitleLabel;
    @FXML private Label taskCountLabel;
    @FXML private ScrollPane taskScrollPane;
    @FXML private Button addListButton;

    private TaskListDAO listDAO;
    private TaskDAO taskDAO;
    private TaskList currentList;
    private Task selectedTask;
    private VBox detailPanel;

    @FXML
    public void initialize() {
        listDAO = new TaskListDAO();
        taskDAO = new TaskDAO();

        setupUI();
        loadLists();
    }

    private void setupUI() {
        // Setup detail panel (initially hidden)
        detailPanel = new VBox(15);
        detailPanel.setPrefWidth(350);
        detailPanel.setStyle("-fx-background-color: #FAFAFA; -fx-padding: 20;");
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);

        // Add icon to add list button
        FontIcon plusIcon = new FontIcon(FontAwesomeSolid.PLUS);
        plusIcon.setIconSize(14);
        addListButton.setGraphic(plusIcon);
    }

    private void loadLists() {
        sidebarListsContainer.getChildren().clear();

        var lists = listDAO.getAllLists();
        for (TaskList list : lists) {
            sidebarListsContainer.getChildren().add(createListItem(list));
        }

        if (!lists.isEmpty() && currentList == null) {
            selectList(lists.get(0));
        }
    }

    private HBox createListItem(TaskList list) {
        HBox listItem = new HBox(10);
        listItem.setAlignment(Pos.CENTER_LEFT);
        listItem.setPadding(new Insets(12, 15, 12, 15));
        listItem.getStyleClass().add("list-item");

        // Color indicator
        Region colorBox = new Region();
        colorBox.setPrefSize(4, 24);
        colorBox.setStyle("-fx-background-color: " + list.getColorHex() + "; -fx-background-radius: 2;");

        // List name
        Label nameLabel = new Label(list.getName());
        nameLabel.getStyleClass().add("list-name");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        // Task count
        int count = taskDAO.getTaskCountByListId(list.getId());
        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("task-count");

        listItem.getChildren().addAll(colorBox, nameLabel, countLabel);

        // Click handler
        listItem.setOnMouseClicked(e -> selectList(list));

        // Context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Rename");
        MenuItem deleteItem = new MenuItem("Delete");

        renameItem.setOnAction(e -> renameList(list));
        deleteItem.setOnAction(e -> deleteList(list));

        contextMenu.getItems().addAll(renameItem, deleteItem);
        listItem.setOnContextMenuRequested(e ->
                contextMenu.show(listItem, e.getScreenX(), e.getScreenY())
        );

        return listItem;
    }

    private void selectList(TaskList list) {
        currentList = list;
        listTitleLabel.setText(list.getName());
        selectedTask = null;
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
        loadTasks();
    }

    private void loadTasks() {
        tasksContainer.getChildren().clear();

        var tasks = taskDAO.getTasksByListId(currentList.getId());
        int activeCount = (int) tasks.stream().filter(t -> !t.isCompleted()).count();
        taskCountLabel.setText(activeCount + " task" + (activeCount != 1 ? "s" : ""));

        for (Task task : tasks) {
            tasksContainer.getChildren().add(createTaskItem(task));
        }

        // Refresh list counts
        loadLists();

        // Reselect current list to highlight
        if (currentList != null) {
            for (var node : sidebarListsContainer.getChildren()) {
                HBox listItem = (HBox) node;
                Label nameLabel = (Label) listItem.getChildren().get(1);
                if (nameLabel.getText().equals(currentList.getName())) {
                    listItem.getStyleClass().add("selected");
                } else {
                    listItem.getStyleClass().remove("selected");
                }
            }
        }
    }

    private HBox createTaskItem(Task task) {
        HBox taskItem = new HBox(12);
        taskItem.setAlignment(Pos.CENTER_LEFT);
        taskItem.setPadding(new Insets(15));
        taskItem.getStyleClass().add("task-item");

        if (task.isCompleted()) {
            taskItem.getStyleClass().add("completed");
        }

        // Checkbox
        CheckBox checkbox = new CheckBox();
        checkbox.setSelected(task.isCompleted());
        checkbox.getStyleClass().add("task-checkbox");
        checkbox.setOnAction(e -> {
            task.setCompleted(checkbox.isSelected());
            taskDAO.updateTask(task);
            loadTasks();
        });

        // Task content
        VBox contentBox = new VBox(4);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("task-title");
        if (task.isCompleted()) {
            titleLabel.getStyleClass().add("completed-text");
        }
        contentBox.getChildren().add(titleLabel);

        // Due date or notes indicator
        if (task.getDueDate() != null || (task.getNotes() != null && !task.getNotes().isEmpty())) {
            HBox metaBox = new HBox(10);
            metaBox.setAlignment(Pos.CENTER_LEFT);

            if (task.getDueDate() != null) {
                FontIcon calendarIcon = new FontIcon(FontAwesomeSolid.CALENDAR_ALT);
                calendarIcon.setIconSize(12);
                calendarIcon.getStyleClass().add("meta-icon");

                Label dateLabel = new Label(formatDate(task.getDueDate()));
                dateLabel.getStyleClass().add("meta-text");

                metaBox.getChildren().addAll(calendarIcon, dateLabel);
            }

            if (task.getNotes() != null && !task.getNotes().isEmpty()) {
                FontIcon noteIcon = new FontIcon(FontAwesomeSolid.STICKY_NOTE);
                noteIcon.setIconSize(12);
                noteIcon.getStyleClass().add("meta-icon");
                metaBox.getChildren().add(noteIcon);
            }

            contentBox.getChildren().add(metaBox);
        }

        // Important star
        Button starButton = new Button();
        FontIcon starIcon = new FontIcon(task.isImportant() ?
                FontAwesomeSolid.STAR : FontAwesomeSolid.STAR);
        starIcon.setIconSize(16);
        starButton.setGraphic(starIcon);
        starButton.getStyleClass().add("star-button");
        if (task.isImportant()) {
            starButton.getStyleClass().add("important");
        }

        starButton.setOnAction(e -> {
            task.setImportant(!task.isImportant());
            taskDAO.updateTask(task);
            loadTasks();
        });

        taskItem.getChildren().addAll(checkbox, contentBox, starButton);

        // Click to show details
        taskItem.setOnMouseClicked(e -> {
            if (e.getTarget() != checkbox && e.getTarget() != starButton) {
                showTaskDetails(task);
            }
        });

        return taskItem;
    }

    private void showTaskDetails(Task task) {
        selectedTask = task;
        detailPanel.getChildren().clear();

        // Close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("×");
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setOnAction(e -> {
            detailPanel.setVisible(false);
            detailPanel.setManaged(false);
            selectedTask = null;
        });
        headerBox.getChildren().add(closeBtn);

        // Task title
        TextField titleField = new TextField(task.getTitle());
        titleField.getStyleClass().add("detail-title");
        titleField.setOnAction(e -> {
            task.setTitle(titleField.getText());
            taskDAO.updateTask(task);
            loadTasks();
        });

        // Important toggle
        HBox importantBox = new HBox(10);
        importantBox.setAlignment(Pos.CENTER_LEFT);
        importantBox.setPadding(new Insets(10));
        importantBox.getStyleClass().add("detail-row");

        FontIcon starIcon = new FontIcon(FontAwesomeSolid.STAR);
        starIcon.setIconSize(16);
        Label importantLabel = new Label("Mark as important");
        CheckBox importantCheck = new CheckBox();
        importantCheck.setSelected(task.isImportant());
        importantCheck.setOnAction(e -> {
            task.setImportant(importantCheck.isSelected());
            taskDAO.updateTask(task);
            loadTasks();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        importantBox.getChildren().addAll(starIcon, importantLabel, spacer, importantCheck);

        // Due date
        HBox dueDateBox = new HBox(10);
        dueDateBox.setAlignment(Pos.CENTER_LEFT);
        dueDateBox.setPadding(new Insets(10));
        dueDateBox.getStyleClass().add("detail-row");

        FontIcon calIcon = new FontIcon(FontAwesomeSolid.CALENDAR_ALT);
        calIcon.setIconSize(16);
        Label dueDateLabel = new Label("Due date");
        DatePicker datePicker = new DatePicker(task.getDueDate());
        datePicker.setOnAction(e -> {
            task.setDueDate(datePicker.getValue());
            taskDAO.updateTask(task);
            loadTasks();
        });

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        dueDateBox.getChildren().addAll(calIcon, dueDateLabel, spacer2, datePicker);

        // Notes
        VBox notesBox = new VBox(8);
        notesBox.setPadding(new Insets(10));
        notesBox.getStyleClass().add("detail-row");

        HBox notesHeader = new HBox(10);
        FontIcon noteIcon = new FontIcon(FontAwesomeSolid.STICKY_NOTE);
        noteIcon.setIconSize(16);
        Label notesLabel = new Label("Notes");
        notesHeader.getChildren().addAll(noteIcon, notesLabel);

        TextArea notesArea = new TextArea(task.getNotes());
        notesArea.setPrefRowCount(5);
        notesArea.setWrapText(true);
        notesArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                task.setNotes(notesArea.getText());
                taskDAO.updateTask(task);
                loadTasks();
            }
        });

        notesBox.getChildren().addAll(notesHeader, notesArea);

        // Delete button
        Button deleteBtn = new Button("Delete Task");
        deleteBtn.getStyleClass().add("delete-button");
        FontIcon trashIcon = new FontIcon(FontAwesomeSolid.TRASH_ALT);
        trashIcon.setIconSize(14);
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setOnAction(e -> deleteTask(task));

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        detailPanel.getChildren().addAll(
                headerBox, titleField, importantBox, dueDateBox, notesBox,
                bottomSpacer, deleteBtn
        );

        detailPanel.setVisible(true);
        detailPanel.setManaged(true);

        // Add to main layout if not already there
        if (!((HBox) taskScrollPane.getParent()).getChildren().contains(detailPanel)) {
            ((HBox) taskScrollPane.getParent()).getChildren().add(detailPanel);
        }
    }

    @FXML
    private void handleAddTask() {
        String title = newTaskField.getText().trim();
        if (title.isEmpty() || currentList == null) return;

        Task task = new Task(title);
        task.setListId(currentList.getId());
        taskDAO.createTask(task);

        newTaskField.clear();
        loadTasks();
    }

    @FXML
    private void handleAddList() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New List");
        dialog.setHeaderText("Create a new list");
        dialog.setContentText("List name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                TaskList newList = new TaskList(name.trim());
                listDAO.createList(newList);
                loadLists();
            }
        });
    }

    private void renameList(TaskList list) {
        TextInputDialog dialog = new TextInputDialog(list.getName());
        dialog.setTitle("Rename List");
        dialog.setHeaderText("Rename " + list.getName());
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                list.setName(name.trim());
                listDAO.updateList(list);
                loadLists();
                if (currentList != null && currentList.getId().equals(list.getId())) {
                    listTitleLabel.setText(name.trim());
                }
            }
        });
    }

    private void deleteList(TaskList list) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete List");
        alert.setHeaderText("Delete " + list.getName() + "?");
        alert.setContentText("This will also delete all tasks in this list. This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            listDAO.deleteList(list.getId());

            if (currentList != null && currentList.getId().equals(list.getId())) {
                currentList = null;
                tasksContainer.getChildren().clear();
                listTitleLabel.setText("");
                taskCountLabel.setText("");
            }

            loadLists();
        }
    }

    private void deleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Delete this task?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            taskDAO.deleteTask(task.getId());
            detailPanel.setVisible(false);
            detailPanel.setManaged(false);
            selectedTask = null;
            loadTasks();
        }
    }

    private String formatDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        if (date.equals(today)) {
            return "Today";
        } else if (date.equals(tomorrow)) {
            return "Tomorrow";
        } else {
            return date.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }
}
