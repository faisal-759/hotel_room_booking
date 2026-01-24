package com.room.bookingroom;

public class Room {
    private String name;
    private String description;
    private String price;
    private int imageResId; // Changed from String imageUrl to int imageResId

    // Constructor for local data
    public Room(String name, String description, String price, int imageResId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }
}
