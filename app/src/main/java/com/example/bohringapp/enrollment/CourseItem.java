package com.example.bohringapp.enrollment;

public class CourseItem {
    private String courseName;
    private final String courseCode;
    private int colour;
    private boolean selected;
    private String URL;
    private String URL_desc;

    public CourseItem(String courseCode, String courseName, int colour) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.colour = colour;
    }

    public CourseItem(String courseCode, String courseName) {
        this.courseName = courseName;
        this.courseCode = courseCode;
    }

    public CourseItem(String courseCode, String URL, String URL_desc){
        this.courseCode = courseCode;
        this.URL = URL;
        this.URL_desc = URL_desc;
    }
    public String getURL(){
        return this.URL;
    }

    public String getURL_desc(){
        return this.URL_desc;
    }

    public String getName() {
        return this.courseName;
    }

    public String getCode() {
        return this.courseCode;
    }

    public int getColour() {
        return this.colour;
    }

    public void setColour(int colour){
        this.colour = colour;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void select() {
        this.selected = true;
    }

    public void deselect() {
        this.selected = false;
    }
}
