package com.room.bookingroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class RoomListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        CardView singleRoomCard = findViewById(R.id.single_room_card);
        CardView doubleRoomCard = findViewById(R.id.double_room_card);
        CardView deluxeRoomCard = findViewById(R.id.deluxe_room_card);
        CardView suiteRoomCard = findViewById(R.id.suite_room_card);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomListActivity.this, RoomDetailsActivity.class);
                startActivity(intent);
            }
        };

        singleRoomCard.setOnClickListener(listener);
        doubleRoomCard.setOnClickListener(listener);
        deluxeRoomCard.setOnClickListener(listener);
        suiteRoomCard.setOnClickListener(listener);
    }
}
