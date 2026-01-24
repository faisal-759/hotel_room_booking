package com.room.bookingroom;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingActivity extends AppCompatActivity {

    private TextInputEditText editTextName, editTextPhone, editTextMembers;
    private Button buttonCheckInDate, buttonCheckOutDate, buttonProceed;
    private TextView textViewCheckInDate, textViewCheckOutDate;

    private Calendar checkInCalendar = Calendar.getInstance();
    private Calendar checkOutCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private String roomName;
    private double roomPrice;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Get data from RoomDetailsActivity
        Intent intent = getIntent();
        roomName = intent.getStringExtra("ROOM_NAME");
        
        // The price comes as a String "₹ XXXX", so we need to parse it robustly.
        String priceString = intent.getStringExtra("ROOM_PRICE");
        if (priceString != null && !priceString.isEmpty()) {
            StringBuilder digitsOnly = new StringBuilder();
            for (char c : priceString.toCharArray()) {
                if (Character.isDigit(c) || c == '.') {
                    digitsOnly.append(c);
                }
            }
            
            if (digitsOnly.length() > 0) {
                try {
                    roomPrice = Double.parseDouble(digitsOnly.toString());
                } catch (NumberFormatException e) {
                    Log.e("BookingActivity", "Could not parse price from extracted digits: " + digitsOnly.toString(), e);
                    roomPrice = 0.0;
                }
            } else {
                roomPrice = 0.0;
            }
        } else {
            roomPrice = 0.0;
        }


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editTextName = findViewById(R.id.edit_text_name);
        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextMembers = findViewById(R.id.edit_text_members);
        buttonCheckInDate = findViewById(R.id.button_check_in_date);
        buttonCheckOutDate = findViewById(R.id.button_check_out_date);
        buttonProceed = findViewById(R.id.button_proceed_to_confirmation);
        textViewCheckInDate = findViewById(R.id.text_view_check_in_date_selected);
        textViewCheckOutDate = findViewById(R.id.text_view_check_out_date_selected);

        // Set listeners
        buttonCheckInDate.setOnClickListener(v -> showDatePickerDialog(true));
        buttonCheckOutDate.setOnClickListener(v -> showDatePickerDialog(false));
        buttonProceed.setOnClickListener(v -> validateAndCheckAvailability());

        updateDateLabels();
    }

    private void showDatePickerDialog(boolean isCheckIn) {
        Calendar calendar = isCheckIn ? checkInCalendar : checkOutCalendar;

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabels();
        };

        DatePickerDialog dialog = new DatePickerDialog(this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        // Prevent selecting past dates
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void updateDateLabels() {
        textViewCheckInDate.setText("Check-in: " + dateFormat.format(checkInCalendar.getTime()));
        textViewCheckOutDate.setText("Check-out: " + dateFormat.format(checkOutCalendar.getTime()));
    }

    private void validateAndCheckAvailability() {
        // Get user input
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String members = editTextMembers.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(members)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkInCalendar.after(checkOutCalendar) || checkInCalendar.equals(checkOutCalendar)) {
            Toast.makeText(this, "Check-out date must be after check-in date", Toast.LENGTH_SHORT).show();
            return;
        }

        checkRoomAvailability(name, phone, members);
    }

    private void checkRoomAvailability(String name, String phone, String members) {
        final Date userCheckIn = checkInCalendar.getTime();
        final Date userCheckOut = checkOutCalendar.getTime();

        db.collection("bookings")
                .whereEqualTo("roomName", roomName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isAvailable = true;
                        try {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String existingCheckInStr = document.getString("checkInDate");
                                String existingCheckOutStr = document.getString("checkOutDate");

                                if (existingCheckInStr != null && existingCheckOutStr != null) {
                                    Date existingCheckIn = dateFormat.parse(existingCheckInStr);
                                    Date existingCheckOut = dateFormat.parse(existingCheckOutStr);

                                    // Overlap check: (start1 < end2) and (start2 < end1)
                                    if (userCheckIn.before(existingCheckOut) && existingCheckIn.before(userCheckOut)) {
                                        isAvailable = false;
                                        break; // Found an overlapping booking
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            Log.e("BookingActivity", "Error parsing date from Firestore", e);
                            Toast.makeText(BookingActivity.this, "Error checking availability.", Toast.LENGTH_SHORT).show();
                            isAvailable = false; // Treat as unavailable to be safe
                        }

                        if (isAvailable) {
                            proceedToConfirmation(name, phone, members);
                        } else {
                            Toast.makeText(BookingActivity.this, "Sorry, this room is not available for the selected dates.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e("BookingActivity", "Error getting documents: ", task.getException());
                        Toast.makeText(BookingActivity.this, "Failed to check availability. Check connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedToConfirmation(String name, String phone, String members) {
        // Calculate number of days
        long diffInMillis = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long totalDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        // A booking must be for at least one day. The validation already ensures this, but as a safeguard:
        if (totalDays < 1) {
            totalDays = 1;
        }

        int numberOfGuests = 1;
        try {
            // Convert the members string to an integer
            numberOfGuests = Integer.parseInt(members);
        } catch (NumberFormatException e) {
            // If parsing fails, log the error and default to 1 guest to avoid calculation errors.
            Log.e("BookingActivity", "Failed to parse number of guests from string: " + members, e);
        }

        // Calculate total price based on room price per day, number of guests, and total days.
        double totalPrice = roomPrice * numberOfGuests * totalDays;

        // Create intent for the confirmation activity
        Intent intent = new Intent(this, BookingConfirmationActivity.class);
        intent.putExtra("ROOM_NAME", roomName);
        intent.putExtra("CHECK_IN_DATE", dateFormat.format(checkInCalendar.getTime()));
        intent.putExtra("CHECK_OUT_DATE", dateFormat.format(checkOutCalendar.getTime()));
        intent.putExtra("TOTAL_DAYS", totalDays);
        intent.putExtra("TOTAL_PRICE", totalPrice);
        intent.putExtra("GUEST_NAME", name); // Consistent key for guest's name
        intent.putExtra("USER_PHONE", phone);
        intent.putExtra("GUESTS_COUNT", members); // The number of guests as a string

        startActivity(intent);
    }
}