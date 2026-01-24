package com.room.bookingroom;

import com.google.firebase.Timestamp;

// This class is a model for booking data.
// It is used by Firestore to automatically convert documents to objects.
// IMPORTANT: For Firestore automatic mapping, a public no-argument constructor is required.
// All fields should have public getters.
public class Booking {
    // Fields must match the document in Firestore
    private String userId;
    private String roomName;
    private String name;
    private String phone;
    private String checkInDate;
    private String checkOutDate;
    private String guests;
    private double totalPrice;
    private Timestamp bookingDate; // Correct type for Firestore server timestamp

    // Public no-argument constructor REQUIRED for Firestore data mapping
    public Booking() {
    }

    // --- Getters ---
    // Getters are required for Firestore to serialize the object
    public String getUserId() { return userId; }

    public String getRoomName() {
        return roomName;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public String getGuests() {
        return guests;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Timestamp getBookingDate() { // Correct getter
        return bookingDate;
    }


    // --- Setters ---
    // Setters are used by Firestore to deserialize the document into an object
    public void setUserId(String userId) { this.userId = userId; }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public void setGuests(String guests) {
        this.guests = guests;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setBookingDate(Timestamp bookingDate) { // Correct setter
        this.bookingDate = bookingDate;
    }
}
