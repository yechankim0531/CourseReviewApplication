package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.SQLException;

public class MyReviewsController {
    private DatabaseDriver databaseDriver;
    private ObservableList<Course> reviews;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn subjectColumn;
    @FXML
    private TableColumn numberColumn;
    @FXML
    private TableColumn ratingColumn;

    private String user;


    public MyReviewsController() throws SQLException {
    }
    @FXML
    public void initialize() throws SQLException {
        this.databaseDriver = new DatabaseDriver();
        initializeTable();
    }

    private void initializeTable() throws SQLException {
        this.databaseDriver.connect();
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        this.user = ReviewsApplication.username;
        this.reviews = FXCollections.observableList(this.databaseDriver.userReviews(user));
        for (Course review: reviews) {
            tableView.getItems().add(review);
        }

        // Got the code below from here to make a tableRow clickable:
        // "https://stackoverflow.com/questions/26563390/detect-doubleclick-on-row-of-tableview-javafx"
        tableView.setRowFactory(tv -> {
            TableRow<Course> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) ) {
                    Course review = row.getItem();
                    try {
                        switchToCourseReviewScene(review);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row ;
        });
        this.databaseDriver.disconnect();
    }

    private void switchToCourseReviewScene(Course review) throws SQLException, IOException {
        ReviewsApplication.lastClickedOnCourse = review;
        this.databaseDriver.disconnect();
        ReviewsApplication.goReview();
    }

    @FXML
    protected void goBack() throws SQLException, IOException {
        this.databaseDriver.disconnect();
        ReviewsApplication.back();
    }


}
