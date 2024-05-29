package edu.virginia.sde.reviews;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Login {
    private Scene scene;
    private DatabaseDriver databaseDriver;

    public Login() throws SQLException, IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        this.scene = new Scene(fxmlLoader.load());
    }

    public Scene getScene() {
        return this.scene;
    }
}
