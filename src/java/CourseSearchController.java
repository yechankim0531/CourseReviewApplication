package edu.virginia.sde.reviews;


import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class CourseSearchController {
    private DatabaseDriver databaseDriver;
    private ObservableList<Course> courses;
    @FXML
    private TextField subjectSearchBar;
    @FXML
    private TextField numberSearchBar;
    @FXML
    private TextField nameSearchBar;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn subjectColumn;
    @FXML
    private TableColumn numberColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn ratingColumn;
    @FXML
    private TextField newCourseSubjectBar;
    @FXML
    private TextField newCourseNumberBar;
    @FXML
    private TextField newCourseNameBar;

    public CourseSearchController() throws SQLException {
    }


    @FXML
    public void initialize() throws SQLException {
        this.databaseDriver = new DatabaseDriver();
        initializeTable();
    }

    public void initializeTable() throws SQLException {
        this.databaseDriver.connect();
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        this.courses = FXCollections.observableList(this.databaseDriver.getAllCourses());
        for (Course course: courses) {
            tableView.getItems().add(course);
        }

        // Got the code below from here to make a tableRow clickable:
        // "https://stackoverflow.com/questions/26563390/detect-doubleclick-on-row-of-tableview-javafx"
        tableView.setRowFactory(tv -> {
                TableRow<Course> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                        Course course = row.getItem();
                        try {
                            switchToCourseScene(course);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                return row ;
        });
        this.databaseDriver.disconnect();
    }

    private void switchToCourseScene(Course course) throws SQLException, IOException {
        ReviewsApplication.lastClickedOnCourse = course;
        this.databaseDriver.disconnect();
        ReviewsApplication.goReview();
    }

    @FXML
    protected void handleNameSearch() {

    }

    @FXML
    protected void handleSearch() {
        ObservableList<Course> filteredList = this.courses;
        filteredList = filteredList.filtered(course -> course.getSubject().contains(subjectSearchBar.getText().strip())
                && course.getNumber().contains(numberSearchBar.getText().strip())
                && course.getName().contains(nameSearchBar.getText().strip()));
        tableView.setItems(filteredList);
    }

    @FXML
    protected void handleLogout() throws SQLException {
        this.databaseDriver.disconnect();
        clearFields();
        ReviewsApplication.logout();
    }

    @FXML
    protected void handleAddNewCourse() {
        String subject = newCourseSubjectBar.getText().trim().toUpperCase();
        String number = newCourseNumberBar.getText().trim();
        String name = newCourseNameBar.getText().trim();

        if (isValidCourse(subject, number, name)) {
            try {
                databaseDriver.connect();
                Course newCourse = new Course(subject, number, name, "");
                databaseDriver.addCourses(List.of(newCourse));
                courses.add(newCourse);
                databaseDriver.commit();
                databaseDriver.disconnect();
                updateTable();
            } catch (SQLException e) {
                showAlert("Error", "Failed to add course to the database.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Invalid Input", "Please check your input data. The Course Subject must be 2-4 " +
                    "letters. The Course number must be an integer with exactly 4 digits. The Course name must " +
                    "have, at most, 50 characters", Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Had to go online for help with regex
    private boolean isValidCourse(String subject, String number, String name) {
        return subject.matches("[a-zA-Z]{2,4}") && number.matches("\\d{4}") && !name.isEmpty()
                && name.length() <= 50;
    }

    private void updateTable() throws SQLException {
        this.databaseDriver.connect();
        // Re-fetch the list of all courses from the database
        ObservableList<Course> updatedCourses = FXCollections.observableList(this.databaseDriver.getAllCourses());
        tableView.setItems(updatedCourses); // Set the new list to the TableView
        this.databaseDriver.disconnect();
    }

    private void clearFields() {
        subjectSearchBar.clear();
        numberSearchBar.clear();
        nameSearchBar.clear();
        newCourseSubjectBar.clear();
        newCourseNumberBar.clear();
        newCourseNameBar.clear();
    }

    @FXML
    protected void handleMyReviews() throws SQLException, IOException {
        this.databaseDriver.disconnect();
        clearFields();
        ReviewsApplication.goMyReviews();
    }
}
