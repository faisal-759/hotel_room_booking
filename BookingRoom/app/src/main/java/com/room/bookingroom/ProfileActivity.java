package com.room.bookingroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView profileName, profileEmail, phoneNumberTextView;
    private Button signOutButton, editProfileButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String profileImageUriString; // To hold the image URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.user_name_profile);
        profileEmail = findViewById(R.id.user_email_profile);
        phoneNumberTextView = findViewById(R.id.user_phone_profile);
        signOutButton = findViewById(R.id.logout_button);
        editProfileButton = findViewById(R.id.edit_profile_button);

        signOutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        editProfileButton.setOnClickListener(v -> {
            // Pass current data to EditProfileActivity
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("CURRENT_NAME", profileName.getText().toString());
            intent.putExtra("CURRENT_PHONE", phoneNumberTextView.getText().toString());
            if (profileImageUriString != null) {
                intent.putExtra("CURRENT_IMAGE_URI", profileImageUriString);
            }
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data every time the activity is shown
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        profileEmail.setText(currentUser.getEmail());

        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            profileName.setText(currentUser.getDisplayName());
        }

        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String phone = document.getString("phone");
                    phoneNumberTextView.setText(phone);

                    if (profileName.getText().toString().isEmpty()) {
                        profileName.setText(document.getString("name"));
                    }
                } else {
                    phoneNumberTextView.setText("Phone not set");
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to load profile details.", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        profileImageUriString = prefs.getString("profile_image_uri", null);
        if (profileImageUriString != null) {
            Glide.with(this)
                 .load(Uri.parse(profileImageUriString))
                 .placeholder(R.drawable.ic_profile)
                 .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
