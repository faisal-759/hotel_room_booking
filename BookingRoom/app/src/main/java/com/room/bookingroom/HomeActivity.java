package com.room.bookingroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements RoomAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RoomAdapter roomAdapter;
    private List<Room> roomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Movenpick");
        }

        recyclerView = findViewById(R.id.rooms_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        roomList = new ArrayList<>();
        setupLocalRoomData(); // Populate list with local data

        roomAdapter = new RoomAdapter(roomList, this);
        recyclerView.setAdapter(roomAdapter);
    }

    private void setupLocalRoomData() {
        // Create local room data
        roomList.add(new Room("Single Room", "Rs. 25000/night", "A cozy room for one person.", R.drawable.single_room));
        roomList.add(new Room("Double Room", "Rs. 40000/night", "A spacious room for two.", R.drawable.double_room));
        roomList.add(new Room("Suite", "Rs. 75000/night", "A luxurious suite with a panoramic view.", R.drawable.suite_room));
        roomList.add(new Room("Presidential Suite", "Rs. 150000/night", "The ultimate luxury experience.", R.drawable.deluxe_room));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_history) {
            startActivity(new Intent(this, MyBookingsActivity.class));
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Room room) {
        Intent intent = new Intent(this, RoomDetailsActivity.class);
        // Pass room details to the next activity
        intent.putExtra("ROOM_NAME", room.getName());
        intent.putExtra("ROOM_PRICE", room.getPrice());
        intent.putExtra("ROOM_DESC", room.getDescription());
        intent.putExtra("ROOM_IMAGE_RES_ID", room.getImageResId());
        startActivity(intent);
    }
}
