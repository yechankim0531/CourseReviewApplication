package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {

    private DatabaseDriver databaseDriver;
    @FXML
    private TextField username;
    @FXML
    private TextField password;
    @FXML
    private TextField newUsername;
    @FXML
    private TextField newPassword;
    @FXML
    private Label loginInstructions;
    private Label usernameLabel;
    private Label passwordLabel;
    private Label inputUsernameLabel;
    private Label inputPasswordLabel;
    private String user;



    public LoginController() throws SQLException {
    }

    // Login handler
    @FXML
    private void handleLogin() throws SQLException {
        this.databaseDriver.connect();
        user = username.getText();
        String pass = password.getText();
        try {

            if (this.databaseDriver.validateLogin(user, pass)) {
                // Login success, switch to course search scene
                username.setText("");
                password.setText("");
                newUsername.setText("");
                newPassword.setText("");
                switchToCourseSearchScene();
                ReviewsApplication.username = user;
            } else {
                loginInstructions.setText("Login Failed. Try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Log the exception details for debugging purposes
            loginInstructions.setText("Error connecting to the database. Please try again later.");
        }
        this.databaseDriver.disconnect();
    }

    // Switches to course search scene when successful login
    private void switchToCourseSearchScene() {
        try {
            // Load the FXML for the course search scene
            this.databaseDriver.disconnect();
            ReviewsApplication.login();
        } catch (Exception e) {
            e.printStackTrace();
            loginInstructions.setText("Failed to load the course search scene.");
        }
    }

    @FXML
    private void handleRegistration() throws SQLException {
        this.databaseDriver.connect();
        String newUser = newUsername.getText();
        String newPass = newPassword.getText();
        try {
            if (newPass.length()<8) {
                loginInstructions.setText("New password is too short.");
                return;
            }
            if (newUser.equals("")) {
                loginInstructions.setText("Cannot have a blank username.");
                return;
            }
            if (this.databaseDriver.addUser(newUser, newPass)) {
                loginInstructions.setText("Registration Successful. Please log in.");
            } else {
                loginInstructions.setText("Registration Failed. Try a different username.");
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Log the exception details for debugging purposes
            loginInstructions.setText("Error connecting to the database. Please try again later.");
        }
        this.databaseDriver.disconnect();
    }

    @FXML
    public void initialize() throws SQLException {
        databaseDriver = new DatabaseDriver();

    }

    @FXML
    private void handleCloseApp() {
        Platform.exit();
    }


}
