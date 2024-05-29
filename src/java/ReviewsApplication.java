package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import java.sql.SQLException;

public class ReviewsApplication extends Application {
    private static Stage stage;
    private static Login login;
    private static CourseSearch courseSearch;
    public static Course lastClickedOnCourse;
    public static String username;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ReviewsApplication.stage = stage;
        ReviewsApplication.stage.setTitle("Reviews");
        ReviewsApplication.stage.show();
        DatabaseDriver databaseDriver = connectToDatabase();
        databaseDriver.disconnect();

        ReviewsApplication.login = new Login();
        ReviewsApplication.courseSearch = new CourseSearch();
        stage.setScene(ReviewsApplication.login.getScene());
    }

    // Helper method to connect to the database
    private DatabaseDriver connectToDatabase() throws SQLException {

        DatabaseDriver databaseDriver = new DatabaseDriver();

        databaseDriver.connect();
        //databaseDriver.clearTables();
        //databaseDriver.deleteTables();
        //databaseDriver.commit();
        databaseDriver.createTables();
        databaseDriver.commit();
        addCourses(databaseDriver);
        return databaseDriver;
    }
    // Testing method, add some courses for testing
    public void addCourses(DatabaseDriver databaseDriver) throws SQLException {
        List<Course> courses = new ArrayList<>();


        courses.add(new Course("CS", "3100", "DSA2", "4.5"));
        courses.add(new Course("CS", "3130", "CSO2", "4.0"));
        courses.add(new Course("APMA", "3100", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"));


        databaseDriver.addCourses(courses);
        databaseDriver.commit();
    }

    public static void logout() {
        stage.setScene(login.getScene());
    }

    public static void login() {
        stage.setScene(courseSearch.getScene());
    }

    public static void goReview() throws SQLException, IOException {
        stage.setScene(new CourseReview().getScene());
    }


    public static void back() throws SQLException, IOException {
        stage.setScene(new CourseSearch().getScene());
    }

    public static void goMyReviews() throws SQLException, IOException {
        stage.setScene(new MyReviews().getScene());
    }



}
