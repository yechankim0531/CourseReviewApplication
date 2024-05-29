package edu.virginia.sde.reviews;

public class Review {

    private int rating;  // Changed from static to instance variable
    private String comment;  // Changed from static to instance variable
    private String time;

    public Review(int rating, String comment, String time) {
        this.rating = rating;
        this.comment = comment;
        this.time = time;
    }

    // Ensure all getters and setters are instance methods, not static
    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getTime() {
        return time;
    }
}
