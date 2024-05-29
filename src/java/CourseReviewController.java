package edu.virginia.sde.reviews;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.collections.transformation.FilteredList;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.security.auth.Subject;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class CourseReviewController {

    @FXML
    private TableView reviewTable;
    @FXML
    private TableColumn ratingColumn;
    @FXML
    private TableColumn commentColumn;
    @FXML
    private TableColumn timeColumn;
    @FXML
    private Label courseShow;
    @FXML
    private Label rating;
    @FXML
    private TextField userRating;
    @FXML
    private TextArea userComment;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button submit;
    private DatabaseDriver databaseDriver;
    private String subject;
    private String number;
    private String name;
    private int rate; // Individual rating from reviews
    private String user;
    private String AvgRating; // Average Rating from Courses Table
    private ObservableList<Review> Reviews;

    public CourseReviewController() throws SQLException {
    }
    @FXML
    public void initialize() throws SQLException {
        this.databaseDriver = new DatabaseDriver();
        this.databaseDriver.connect();
        this.subject = ReviewsApplication.lastClickedOnCourse.getSubject();
        this.number = ReviewsApplication.lastClickedOnCourse.getNumber();
        this.name = ReviewsApplication.lastClickedOnCourse.getName();
        this.user = ReviewsApplication.username;
        this.AvgRating = ReviewsApplication.lastClickedOnCourse.getRating();
        initializeTable();
        courseShow.setText(subject +" "+number +": "+name );
        rating.setText(AvgRating);

        if (this.databaseDriver.userHasSubmitted(user, subject, number, name)){
            editButton.setVisible(true);
            deleteButton.setVisible(true);
        }
        this.databaseDriver.disconnect();
    }

    private void initializeTable() throws SQLException {
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("Rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("Comment"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("Time"));

        reviewTable.getItems().clear();  // Clear existing data before populating

        // Set custom cell factory for comments to include a scrollable text area
        commentColumn.setCellFactory(tc -> new TableCell<Review, String>() {
            private TextArea textArea;
            private ScrollPane scrollPane;

            @Override
            protected void updateItem(String comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (textArea == null) {
                        textArea = new TextArea(comment);
                        textArea.setWrapText(true);
                        textArea.setEditable(false);
                        textArea.setPrefHeight(100);  // Preferred height
                        scrollPane = new ScrollPane(textArea);
                        scrollPane.setFitToWidth(true);
                        scrollPane.setPrefHeight(100);
                    } else {
                        textArea.setText(comment);
                    }
                    setGraphic(scrollPane);
                    setText(null);
                }
            }
        });

        // Set custom cell factory for time column to include a horizontal scroll bar
        timeColumn.setCellFactory(tc -> new TableCell<Review, String>() {
            private Label label;
            private ScrollPane scrollPane;

            @Override
            protected void updateItem(String time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    if (label == null) {
                        label = new Label(time);
                        scrollPane = new ScrollPane(label);
                        scrollPane.setFitToWidth(false);  // Disable horizontal auto resize
                        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);  // Horizontal scroll bar
                    } else {
                        label.setText(time);
                    }
                    setGraphic(scrollPane);
                    setText(null);
                }
            }
        });

        this.Reviews = FXCollections.observableList(this.databaseDriver.getAllReviews(subject, number, name));
        reviewTable.setItems(Reviews);  // Use setItems for better practice

        // To make the entire table scrollable vertically, you can ensure your TableView is inside a ScrollPane in your FXML layout or when setting up your scene
    }




    @FXML
    private void handleSubmit() throws SQLException {
        this.databaseDriver.connect();
        if (userRating.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Rating cannot be empty.");
            alert.showAndWait();
            this.databaseDriver.disconnect();
            return;
        }
        try {
            int rating = Integer.parseInt(userRating.getText());  // Try parsing the text to an integer
            if (rating < 1 || rating > 5) {
                // If the number is not between 1 and 5, show an error message
                Alert alert = new Alert(Alert.AlertType.ERROR, "Rating must be a number between 1 and 5.");
                alert.showAndWait();
                this.databaseDriver.disconnect();
                return;
            }
        } catch (NumberFormatException e) {
            // If parsing fails, show an error message
            Alert alert = new Alert(Alert.AlertType.ERROR, "Rating has to be a number.");
            alert.showAndWait();
            this.databaseDriver.disconnect();
            return;
        }
        try {
            if (this.databaseDriver.userHasSubmitted(user, subject, number, name) && submit.getText().equals("Submit")) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You have already reviewed this course.");
                alert.showAndWait();
                this.databaseDriver.disconnect();
                return;
            }
            else{
                rate = Integer.parseInt(userRating.getText());
                String comment = userComment.getText();
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = sdf.format(timestamp);
                if(submit.getText().equals("Submit")) {
                    this.databaseDriver.addNewReview(subject, number, name, rate, Optional.ofNullable(comment), time, user);

                }
                else {
                    this.databaseDriver.updateReview(subject, number, name, rate, Optional.ofNullable(comment), time, user);

                }
                editButton.setVisible(true);
                deleteButton.setVisible(true);
                initializeTable();
                submit.setText("Submit");
                userRating.clear();
                userComment.clear();
                this.databaseDriver.disconnect();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.databaseDriver.disconnect();
    }

    @FXML
    private void handleEdit() throws SQLException {
        this.databaseDriver.connect();
        try {
            List<Review> reviews = this.databaseDriver.myReviews(subject, number, name, user);
            if (!reviews.isEmpty()) {
                Review review = reviews.get(0);  // Assuming there is only one review per user per course
                userRating.setText(String.valueOf(review.getRating()));
                userComment.setText(review.getComment());
                submit.setText("Submit Edit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.databaseDriver.disconnect();
    }

    @FXML
    private void handleDelete() throws SQLException {
        try {
            this.databaseDriver.connect();
            this.databaseDriver.deleteReview(subject, number, name, user);
            this.databaseDriver.commit();  // Make sure to commit the transaction
            initializeTable();
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        } catch (SQLException e) {
            this.databaseDriver.rollback();  // Rollback on exception
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to delete the review: " + e.getMessage());
            alert.showAndWait();
        } finally {
            this.databaseDriver.disconnect();  // Ensure connection is always closed
        }
    }

    @FXML
    protected void handleBack() throws SQLException, IOException {
        this.databaseDriver.disconnect();
        ReviewsApplication.back();
    }
}






