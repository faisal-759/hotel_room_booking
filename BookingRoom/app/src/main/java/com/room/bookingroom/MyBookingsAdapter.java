package com.room.bookingroom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyBookingsAdapter extends RecyclerView.Adapter<MyBookingsAdapter.BookingViewHolder> {

    private List<Booking> bookings;

    public MyBookingsAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView roomNameTextView;
        private TextView userNameTextView;
        private TextView userPhoneTextView;
        private TextView checkInDateTextView;
        private TextView checkOutDateTextView;
        private TextView guestsTextView;
        private TextView bookingDateTextView;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameTextView = itemView.findViewById(R.id.room_name);
            userNameTextView = itemView.findViewById(R.id.user_name);
            userPhoneTextView = itemView.findViewById(R.id.user_phone);
            checkInDateTextView = itemView.findViewById(R.id.check_in_date);
            checkOutDateTextView = itemView.findViewById(R.id.check_out_date);
            guestsTextView = itemView.findViewById(R.id.number_of_guests);
            bookingDateTextView = itemView.findViewById(R.id.booking_date);
        }

        public void bind(Booking booking) {
            roomNameTextView.setText(booking.getRoomName());
            userNameTextView.setText(booking.getName());
            userPhoneTextView.setText("Phone: " + booking.getPhone());
            checkInDateTextView.setText("Check-in: " + booking.getCheckInDate());
            checkOutDateTextView.setText("Check-out: " + booking.getCheckOutDate());
            guestsTextView.setText("Guests: " + booking.getGuests());

            if (booking.getBookingDate() != null) {
                Date date = booking.getBookingDate().toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                bookingDateTextView.setText("Booked on: " + sdf.format(date));
            } else {
                bookingDateTextView.setText("Booking date not available");
            }
        }
    }
}
