package com.room.bookingroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editName, editPhone, editPassword;
    private CircleImageView profileImageView;
    private Button saveButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Uri imageUri;
    private boolean isNewImageSelected = false; // Flag to track if a new image was picked

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName = findViewById(R.id.edit_name_field);
        editPhone = findViewById(R.id.edit_phone_field);
        editPassword = findViewById(R.id.edit_new_password_field);
        profileImageView = findViewById(R.id.edit_profile_image);
        saveButton = findViewById(R.id.save_profile_button);
        progressBar = findViewById(R.id.progressBar);

        // Get data from Intent and display it
        Intent intent = getIntent();
        editName.setText(intent.getStringExtra("CURRENT_NAME"));
        editPhone.setText(intent.getStringExtra("CURRENT_PHONE"));
        String imageUriString = intent.getStringExtra("CURRENT_IMAGE_URI");
        if (imageUriString != null) {
            // Don't set the class-level imageUri here, just load it.
            Glide.with(this).load(Uri.parse(imageUriString)).placeholder(R.drawable.ic_profile).into(profileImageView);
        } else {
             profileImageView.setImageResource(R.drawable.ic_profile);
        }

        profileImageView.setOnClickListener(v -> openFileChooser());
        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            isNewImageSelected = true; // A new image has been selected
            Glide.with(this).load(imageUri).into(profileImageView);
        }
    }

    private void saveUserProfile() {
        String newName = editName.getText().toString().trim();
        String newPhone = editPhone.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            editName.setError("Name is required.");
            editName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        List<Task<?>> tasks = new ArrayList<>();

        // Task 1: Update Firebase Auth display name
        if (currentUser.getDisplayName() == null || !currentUser.getDisplayName().equals(newName)) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();
            tasks.add(currentUser.updateProfile(profileUpdates));
        }

        // Task 2: Update Firestore phone number and name
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("name", newName);
        userDetails.put("phone", newPhone);
        // Use set with merge to create the document if it doesn't exist, or update it if it does.
        tasks.add(db.collection("users").document(currentUser.getUid()).set(userDetails, SetOptions.merge()));

        // Task 3: Update password if provided
        if (!TextUtils.isEmpty(newPassword)) {
            if (newPassword.length() < 6) {
                editPassword.setError("Password should be at least 6 characters");
                editPassword.requestFocus();
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                return;
            }
            tasks.add(currentUser.updatePassword(newPassword));
        }

        // Now, handle the completion of all tasks
        Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
            progressBar.setVisibility(View.GONE);
            saveButton.setEnabled(true);

            boolean allSucceeded = true;
            String errorMessage = "An unknown error occurred.";

            for (Task<?> task : allTasks.getResult()) {
                if (!task.isSuccessful()) {
                    allSucceeded = false;
                    Exception e = task.getException();
                    if (e != null) {
                        errorMessage = e.getMessage();
                         // Check for specific re-authentication error for password change
                        if (e instanceof com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
                            errorMessage = "Password change requires recent login. Please log out and log in again.";
                        }
                    }
                    break; // Stop on first error
                }
            }

            if (allSucceeded) {
                 // Save image URI to SharedPreferences ONLY if a new image was selected and all other tasks succeeded
                if (isNewImageSelected && imageUri != null) {
                    SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("profile_image_uri", imageUri.toString());
                    editor.apply(); // Use apply() for background saving
                }
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to ProfileActivity
            } else {
                 Toast.makeText(EditProfileActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
