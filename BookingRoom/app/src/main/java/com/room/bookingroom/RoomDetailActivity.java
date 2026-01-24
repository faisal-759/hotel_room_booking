package com.room.bookingroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;

public class RoomDetailActivity extends AppCompatActivity {

    private String roomName;
    private String roomPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        ImageView roomDetailImage = findViewById(R.id.room_detail_image);
        TextView roomDetailName = findViewById(R.id.room_detail_name);
        TextView roomDetailDescription = findViewById(R.id.room_detail_description);
        TextView roomDetailPriceView = findViewById(R.id.room_detail_price);
        Button bookNowButton = findViewById(R.id.book_now_button);

        // Get the data from the intent
        roomName = getIntent().getStringExtra("ROOM_NAME");
        String description = getIntent().getStringExtra("ROOM_DESC");
        roomPrice = getIntent().getStringExtra("ROOM_PRICE");

        // Set the data to the views
        roomDetailName.setText(roomName);
        roomDetailDescription.setText(description);
        roomDetailPriceView.setText(String.format("$%s/night", roomPrice));

        // Handle book now button click
        bookNowButton.setOnClickListener(v -> showDateRangePicker());
    }

    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Check-in and Check-out Dates");

        final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;

            // Create intent for BookingConfirmationActivity
            Intent intent = new Intent(RoomDetailActivity.this, BookingConfirmationActivity.class);
            intent.putExtra("ROOM_NAME", roomName);
            intent.putExtra("ROOM_PRICE", roomPrice);
            intent.putExtra("CHECK_IN_DATE", startDate);
            intent.putExtra("CHECK_OUT_DATE", endDate);
            startActivity(intent);
        });
    }
}
