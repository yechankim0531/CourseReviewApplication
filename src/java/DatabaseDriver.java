package edu.virginia.sde.reviews;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;


public class DatabaseDriver {
    public static final String DATABASE_CONNECTION = "jdbc:sqlite:course_reviews.sqlite";
    private static Connection connection;

    public DatabaseDriver () {
    }

    /**
     * Connect to a SQLite Database. This turns out Foreign Key enforcement, and disables auto-commits
     * @throws SQLException
     */
    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The connection is already opened");
        }
        connection = DriverManager.getConnection(DATABASE_CONNECTION);
        //the next line enables foreign key enforcement - do not delete/comment out
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        //the next line disables auto-commit - do not delete/comment out
        connection.setAutoCommit(false);
    }

    /**
     * Commit all changes since the connection was opened OR since the last commit/rollback
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Rollback to the last commit, or when the connection was opened
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Ends the connection to the database
     */
    public void disconnect() throws SQLException {
        connection.close();
    }

    // Creates the tables for the database
    public void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Courses (" +
                    "Subject TEXT NOT NULL," +
                    "Number TEXT NOT NULL," +
                    "Name TEXT NOT NULL," +
                    "Rating REAL NOT NULL," +
                    "PRIMARY KEY (Subject, Number, Name));");

            stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "Username TEXT NOT NULL," +
                    "Password TEXT NOT NULL," +
                    "PRIMARY KEY (Username));");

            stmt.execute("CREATE TABLE IF NOT EXISTS Reviews (" +
                    "Subject TEXT NOT NULL," +
                    "Number TEXT NOT NULL," +
                    "Name TEXT NOT NULL," +
                    "Rating Integer NOT NULL," +
                    "Comment TEXT,"+
                    "Time TEXT NOT NULL,"+
                    "Username TEXT NOT NULL,"+
                    "PRIMARY KEY (Rating, Comment, Time)," +
                    "FOREIGN KEY (Username) REFERENCES Users(Username));");

        }
    }

    // Adds the Courses in the list to the database
    public void addCourses(List<Course> courses) throws SQLException {
        String sql = "INSERT OR IGNORE INTO Courses (Subject, Number, Name, Rating) VALUES (?, ?, ?, ?)";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Course course: courses){
                stmt.setString(1,course.getSubject());
                stmt.setString(2,course.getNumber());
                stmt.setString(3,course.getName());
                stmt.setString(4, course.getRating());
                stmt.execute();
            }

        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    // Adds new review into Reviews Table
    public void addNewReview(String subject, String number, String name, int rating, Optional<String> comment, String time, String user) throws SQLException {

        String sql = "INSERT OR IGNORE INTO Reviews (Subject, Number, Name, Rating,Comment, Time, Username) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, subject);
            stmt.setString(2, number);
            stmt.setString(3, name);
            stmt.setInt(4, rating);
            stmt.setString(5, comment.orElse(" "));
            stmt.setString(6, time);
            stmt.setString(7, user);
            stmt.execute();
            commit();  // Commit changes immediately for user data


        } catch (SQLException e) {
            rollback();
            throw e;
        }
        updateCourseRating(subject, number, name, getAverageRatingForCourse(subject, number, name));
    }
//
public void updateReview(String subject, String number, String name, int rating, Optional<String> comment, String time, String user) throws SQLException {
    String sql = "UPDATE Reviews SET Rating = ?, Comment= ?, Time = ? WHERE Subject = ? AND Number=? AND Name=? AND Username=?";

    try(PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, rating);
        stmt.setString(2, comment.orElse(" "));
        stmt.setString(3, time);
        stmt.setString(4, subject);
        stmt.setString(5, number);
        stmt.setString(6, name);
        stmt.setString(7, user);
        stmt.executeUpdate();
        commit();  // Commit changes immediately for user data
    } catch (SQLException e) {
        rollback();
        throw e;
    }
    updateCourseRating(subject, number, name, getAverageRatingForCourse(subject, number, name));
}



    // Adds a new user to the login information
    public boolean addUser(String username, String password) throws SQLException {
        // Check if the username already exists
        String checkUserSql = "SELECT Username FROM Users WHERE Username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSql)) {
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                // If the ResultSet has a row, the username is taken
                return false;
            }
        } catch (SQLException e) {
            rollback();
            throw e;
        }



        // If the username does not exist, proceed with adding new user
        String insertSql = "INSERT INTO Users (Username, Password) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.execute();
            commit();  // Commit changes immediately for user data
            return true;
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    // Get a List of all Courses in the database, from the Courses table
    public List<Course> getAllCourses() throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM Courses";
        try(PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()){
            while(rs.next()){
                courses.add(new Course(rs.getString("Subject"), rs.getString("Number"),
                        rs.getString("Name"), rs.getString("Rating")));
            }
        }
        return courses;
    }

    // Gets all the reviews for the given course
    public List<Review> getAllReviews(String subject, String number, String name) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE Subject = ? AND Number=? AND Name=?";
        try(PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, subject);
            stmt.setString(2, number);
            stmt.setString(3, name);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(rs.getInt("Rating"), rs.getString("Comment"),
                            rs.getString("Time")));
                }
            }
            catch (SQLException e) {
                rollback();
                throw e;
            }
        }
        return reviews;
    }
    public void deleteReview(String subject, String number, String name, String user) throws SQLException {
        String sql = "DELETE FROM Reviews WHERE Subject = ? AND Number = ? AND Name = ? AND Username = ?";


        try(PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, subject);
            stmt.setString(2, number);
            stmt.setString(3, name);
            stmt.setString(4, user);
            stmt.execute();
            commit();


        } catch (SQLException e) {
            rollback();
            throw e;
        }
        updateCourseRating(subject, number, name, getAverageRatingForCourse(subject, number, name));
    }
    // Gets the user's review from the Reviews Table
    public List<Review> myReviews(String subject, String number, String name, String user) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE Subject = ? AND Number=? AND Name=? AND Username=?";
        try(PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, subject);
            stmt.setString(2, number);
            stmt.setString(3, name);
            stmt.setString(4, user);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(rs.getInt("Rating"), rs.getString("Comment"),
                            rs.getString("Time")));
                }
            }
            catch (SQLException e) {
                rollback();
                throw e;
            }
        }
        return reviews;
    }

    public List<Course> userReviews(String user) throws SQLException {
        List<Course> reviews = new ArrayList<>();
        String sql = "SELECT * FROM Reviews WHERE Username=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String subject = rs.getString("Subject");
                    String number = rs.getString("Number");
                    String name = rs.getString("Name");
                    int ratingNum = rs.getInt("Rating");
                    String rating = String.valueOf(ratingNum);
                    // Construct Review object and add to the list
                    reviews.add(new Course(subject, number, name, rating));
                }
            }
        }
        return reviews;
    }

    // Validates a login attempt
    public boolean validateLogin(String username, String password) throws SQLException {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // Return true if a row exists, i.e., credentials are valid
            }
        }
    }
 // Checks if user has submitted review already for the course
    public boolean userHasSubmitted(String username, String subject, String number, String name) throws SQLException{
        String sql = "SELECT * FROM Reviews WHERE Username = ? AND Subject = ? AND Number=? AND Name=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, subject);
            stmt.setString(3, number);
            stmt.setString(4, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();  // Return true if a row exists, i.e., user has submitted
            }
        }

    }

    public String getAverageRatingForCourse(String subject, String number, String name) throws SQLException {
        String sql = "SELECT AVG(Rating) AS AverageRating FROM Reviews WHERE Subject = ? AND Number = ? AND Name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, subject);
            stmt.setString(2, number);
            stmt.setString(3, name);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double averageRating = rs.getDouble("AverageRating");
                if (rs.wasNull()) {
                    return "";
                }
                return String.valueOf(averageRating).substring(0, 4);
            }
        }
        return "";
    }

    public void updateCourseRating(String subject, String number, String name, String newAverageRating) throws SQLException {
        String sql = "UPDATE Courses SET Rating = ? WHERE Subject = ? AND Number = ? AND Name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, Double.parseDouble(newAverageRating));
            stmt.setString(2, subject);
            stmt.setString(3, number);
            stmt.setString(4, name);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating the course failed, no rows affected.");
            }
            commit();
        }
    }

    // Removes all data from the tables
    public void clearTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Courses");
        } catch (SQLException e) {
            rollback();
            throw e;
        }
    }

    public void deleteTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE Courses");
        } catch (SQLException e) {
            rollback();
            throw e;
        }

    }
}

