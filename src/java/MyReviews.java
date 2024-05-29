package edu.virginia.sde.reviews;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyReviews {
    private Scene scene;
    private DatabaseDriver databaseDriver;
    public MyReviews() throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("myReviews.fxml"));
        this.scene = new Scene(fxmlLoader.load());
    }

    public Scene getScene() {
        return this.scene;
    }
}

