package com.example.bohringapp.calendar;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TodoItem implements Comparable, Parcelable {
    private int dbKey;
    private String title;
    private String description;
    private Long dueDate;
    private String courseCode;
    private int colour;
    private boolean completed;

    public TodoItem() {

    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDueDate() {
        return this.dueDate;
    }

    public void setDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public String getPrettyDueDate() {
        return new SimpleDateFormat("MM/dd/yyyy").format(new Date(this.dueDate));
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseCode() {
        return this.courseCode;
    }

    public void setDbKey(int dbKey) {
        this.dbKey = dbKey;
    }

    public int getDbKey() {
        return this.dbKey;
    }

    public int getColour() {
        return this.colour;
    }

    public boolean getCompleted() {
        return this.completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public int compareTo(Object o) {
        if (o.getClass() != this.getClass()) {
            throw new IllegalArgumentException("Only TodoItems may be compared to TodoItems");
        }

        return this.dueDate.compareTo(((TodoItem) o).dueDate);
    }

    protected TodoItem(Parcel in) {
        courseCode = in.readString();
        title = in.readString();
        description = in.readString();
        dueDate = in.readLong();
        colour = in.readInt();
        completed = in.readByte() != 0;
        dbKey = in.readInt();
    }

    public static final Creator<TodoItem> CREATOR = new Creator<TodoItem>() {
        @Override
        public TodoItem createFromParcel(Parcel in) {
            return new TodoItem(in);
        }

        @Override
        public TodoItem[] newArray(int size) {
            return new TodoItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseCode);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeLong(dueDate);
        dest.writeInt(colour);
        dest.writeByte((byte) (completed ? 1 : 0));
        dest.writeInt(dbKey);
    }
}
