package com.room.bookingroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RoomDetailsActivity extends AppCompatActivity {

    private ImageView roomImage;
    private TextView roomName, roomPrice, roomDescription, roomFacilities;
    private Button bookNowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize Views
        roomImage = findViewById(R.id.room_image_details);
        roomName = findViewById(R.id.room_name_details);
        roomPrice = findViewById(R.id.room_price_details);
        roomDescription = findViewById(R.id.room_description_details);
        roomFacilities = findViewById(R.id.room_facilities_details);
        bookNowButton = findViewById(R.id.check_availability_button);

        // Get data from Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("ROOM_NAME");
        String price = intent.getStringExtra("ROOM_PRICE");
        String desc = intent.getStringExtra("ROOM_DESC");
        int imageResId = intent.getIntExtra("ROOM_IMAGE_RES_ID", 0);

        // Set data to views
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name);
        }
        roomName.setText(name);
        roomPrice.setText(price);
        roomDescription.setText(desc);
        if (imageResId != 0) {
            roomImage.setImageResource(imageResId);
        }

        // Set some dummy facilities text
        roomFacilities.setText("• Free Wi-Fi\n• Air Conditioning\n• Breakfast Included");


        bookNowButton.setOnClickListener(v -> {
            Intent bookingIntent = new Intent(RoomDetailsActivity.this, BookingActivity.class);
            bookingIntent.putExtra("ROOM_NAME", name);
            bookingIntent.putExtra("ROOM_PRICE", price);
            startActivity(bookingIntent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
