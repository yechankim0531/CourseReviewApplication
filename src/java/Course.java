package edu.virginia.sde.reviews;

public class Course {
    private String subject;
    private String number;
    private String name;
    private String rating;

    public Course(String subject, String number, String name, String rating) {
        this.subject = subject;
        this.number = number;
        this.name = name;
        this.rating = rating;
    }

    public Course(String subject, String number, String name) {
        this.subject = subject;
        this.number = number;
        this.name = name;
        this.rating = "";
    }

    public Course() {

    }

    public String getSubject() {
        return this.subject;
    }

    public String getNumber() {
        return this.number;
    }

    public String getName() {
        return this.name;
    }

    public String getRating() {
        return this.rating;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
