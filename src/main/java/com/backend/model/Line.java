package com.backend.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;


public class Line {
    private Point currentPoint;
    private Point prevPoint;
    private String color;

    public Line(Point currentPoint, Point prevPoint, String color) {
        this.currentPoint = currentPoint;
        this.prevPoint = prevPoint;
        this.color = color;
    }

    public Point getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
    }

    public Point getPrevPoint() {
        return prevPoint;
    }

    public void setPrevPoint(Point prevPoint) {
        this.prevPoint = prevPoint;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Line() {
    }
}
