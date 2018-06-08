package com.github.sangalaa.facerecognitionapp;

public class FaceData {
    private double height;
    private double width;
    private double left;
    private double top;

    private String gender;

    private long minAge;
    private long maxAge;

    public FaceData(double height, double width, double left, double top, String gender, long minAge, long maxAge) {
        this.height = height;
        this.width = width;
        this.left = left;
        this.top = top;
        this.gender = gender;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    @Override
    public String toString() {
        return "FaceData{" +
                "height=" + height +
                ", width=" + width +
                ", left=" + left +
                ", top=" + top +
                ", gender='" + gender + '\'' +
                ", minAge=" + minAge +
                ", maxAge=" + maxAge +
                '}';
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public String getGender() {
        return gender;
    }

    public long getMinAge() {
        return minAge;
    }

    public long getMaxAge() {
        return maxAge;
    }
}
