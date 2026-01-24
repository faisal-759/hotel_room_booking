package com.room.bookingroom;

import java.util.ArrayList;
import java.util.List;

public class BookingDataHolder {
    private static BookingDataHolder instance;
    private List<Booking> bookings;

    private BookingDataHolder() {
        bookings = new ArrayList<>();
    }

    public static synchronized BookingDataHolder getInstance() {
        if (instance == null) {
            instance = new BookingDataHolder();
        }
        return instance;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
    }
}
