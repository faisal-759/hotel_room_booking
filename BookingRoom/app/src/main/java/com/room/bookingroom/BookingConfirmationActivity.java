package com.room.bookingroom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BookingConfirmationActivity extends AppCompatActivity {

    private static final String TAG = "BookingConfirmation";
    private TextView textViewBookingConfirmation, textViewRoomName, textViewName, textViewPhone,
            textViewCheckIn, textViewCheckOut, textViewGuests;
    private ImageView imageViewSuccess;
    private Button buttonBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        // Initialize UI components
        imageViewSuccess = findViewById(R.id.image_view_success);
        textViewBookingConfirmation = findViewById(R.id.text_view_booking_confirmation);
        textViewRoomName = findViewById(R.id.text_view_room_name_confirmed);
        textViewName = findViewById(R.id.text_view_name_confirmed);
        textViewPhone = findViewById(R.id.text_view_phone_confirmed);
        textViewCheckIn = findViewById(R.id.text_view_check_in_confirmed);
        textViewCheckOut = findViewById(R.id.text_view_check_out_confirmed);
        textViewGuests = findViewById(R.id.text_view_guests_confirmed);
        
        buttonBackToHome = findViewById(R.id.button_back_to_home);

        // Get data from Intent
        Intent intent = getIntent();
        String roomName = intent.getStringExtra("ROOM_NAME");
        String name = intent.getStringExtra("GUEST_NAME");
        String phone = intent.getStringExtra("USER_PHONE");
        String checkInDate = intent.getStringExtra("CHECK_IN_DATE");
        String checkOutDate = intent.getStringExtra("CHECK_OUT_DATE");
        String guests = intent.getStringExtra("GUESTS_COUNT");
        double totalPriceValue = intent.getDoubleExtra("TOTAL_PRICE", 0.0);

        // Set text for the views
        textViewRoomName.setText("Room: " + roomName);
        textViewName.setText("Name: " + name);
        textViewPhone.setText("Phone: " + phone);
        textViewCheckIn.setText("Check-in: " + checkInDate);
        textViewCheckOut.setText("Check-out: " + checkOutDate);
        textViewGuests.setText("Guests: " + guests);

        imageViewSuccess.setImageResource(android.R.drawable.checkbox_on_background);

        // Save booking to Firestore
        saveBookingToHistory(roomName, name, phone, checkInDate, checkOutDate, guests, totalPriceValue);

        buttonBackToHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(BookingConfirmationActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    private void saveBookingToHistory(String roomName, String name, String phone, String checkInDate, String checkOutDate, String guests, double totalPrice) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Map<String, Object> booking = new HashMap<>();
            booking.put("userId", userId); // <-- THE CRITICAL FIX: Add the user ID
            booking.put("roomName", roomName);
            booking.put("name", name);
            booking.put("phone", phone);
            booking.put("checkInDate", checkInDate);
            booking.put("checkOutDate", checkOutDate);
            booking.put("guests", guests);
            booking.put("totalPrice", totalPrice); // Save as double
            booking.put("bookingDate", FieldValue.serverTimestamp()); // Use server timestamp for ordering

            // Corrected to save to the top-level "bookings" collection
            db.collection("bookings") 
                    .add(booking)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "Booking saved successfully to 'bookings' collection!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error saving booking", e));
        } else {
            Log.w(TAG, "User is not logged in. Cannot save booking.");
        }
    }
}
