package com.room.bookingroom;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    private static final String TAG = "MyBookingsActivity";
    private RecyclerView recyclerView;
    private MyBookingsAdapter myBookingsAdapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Bookings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.bookings_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        myBookingsAdapter = new MyBookingsAdapter(bookingList);
        recyclerView.setAdapter(myBookingsAdapter);

        fetchBookingsFromFirestore();
    }

    private void fetchBookingsFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "fetchBookingsFromFirestore: No user is signed in.");
            return;
        }
        String userId = currentUser.getUid();

        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Booking booking = document.toObject(Booking.class);
                                    bookingList.add(booking);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error converting document: " + document.getId(), e);
                                }
                            }
                        } else {
                            Log.d(TAG, "User has no bookings.");
                        }
                        myBookingsAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error getting user bookings: ", task.getException());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_bookings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_clear_history) {
            showClearHistoryConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearHistoryConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to delete all your bookings? This action cannot be undone.")
                .setPositiveButton("Yes, Clear It", (dialog, which) -> clearUserBookings())
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void clearUserBookings() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        db.collection("bookings").whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        WriteBatch batch = db.batch();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            batch.delete(document.getReference());
                        }
                        batch.commit().addOnCompleteListener(batchTask -> {
                            if (batchTask.isSuccessful()) {
                                bookingList.clear();
                                myBookingsAdapter.notifyDataSetChanged();
                                Toast.makeText(MyBookingsActivity.this, "History cleared.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MyBookingsActivity.this, "Failed to clear history.", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error clearing history", batchTask.getException());
                            }
                        });
                    } else {
                        Toast.makeText(MyBookingsActivity.this, "Error finding bookings to clear.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting documents to delete", task.getException());
                    }
                });
    }
}
