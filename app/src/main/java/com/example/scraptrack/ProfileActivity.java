package com.example.scraptrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends ComponentActivity {

    private static final int PICK_IMAGE = 100;

    private ImageView profilePic, threeDotsMenu;
    private TextView appName, transactionInfo;
    private EditText nameEditText, phoneEditText, emailEditText, passwordEditText, addressEditText;
    private Button changeProfilePicButton, saveButton;
    private SharedPreferences sharedPreferences;
    private Uri imageUri;
    private DatabaseReference userDatabaseRef;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profilePic = findViewById(R.id.profilePic);
        threeDotsMenu = findViewById(R.id.threeDotsMenu);  // Hamburger menu button
        appName = findViewById(R.id.appName);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        addressEditText = findViewById(R.id.addressEditText);
        changeProfilePicButton = findViewById(R.id.changeProfilePicButton);
        saveButton = findViewById(R.id.saveButton);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // Get the session ID from SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String sessionId = sharedPreferences.getString("sessionID", null);

        if (sessionId != null) {
            userDatabaseRef = FirebaseDatabase.getInstance().getReference("users").child(sessionId);
            loadUserProfile();
        } else {
            Toast.makeText(this, "Session ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupNavigation(navigationView);

        // Open gallery to change profile picture
        changeProfilePicButton.setOnClickListener(v -> openGallery());

        // Save profile changes
        saveButton.setOnClickListener(v -> saveUserProfile());

        // Open the navigation drawer when the hamburger menu is clicked
        threeDotsMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void loadUserProfile() {
        userDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve data
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String profilePicUrl = snapshot.child("profilePicUrl").getValue(String.class);

                    // Update UI
                    if (name != null) nameEditText.setText(name);
                    if (phone != null) phoneEditText.setText(phone);
                    if (email != null) emailEditText.setText(email);
                    if (password != null) passwordEditText.setText(password);
                    if (address != null) addressEditText.setText(address);
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(profilePicUrl)
                                .into(profilePic);
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error loading user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

            if (menuItem.getItemId() == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            } else if (menuItem.getItemId() == R.id.nav_transactions) {
                // Pass the session ID to TransactionActivity
                Intent intent = new Intent(ProfileActivity.this, TransactionActivity.class);
                intent.putExtra("sessionID", sessionId);  // Pass session ID
                startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_profile) {
                Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                intent.putExtra("sessionID", sessionId);  // Pass session ID
                startActivity(intent);
            } else if (menuItem.getItemId() == R.id.nav_analysis) {
                Intent intent = new Intent(ProfileActivity.this, AnalysisActivity.class);
                intent.putExtra("sessionID", sessionId);  // Pass session ID
                startActivity(intent);
            }else if (menuItem.getItemId()== R.id.nav_transactions_history){
                Intent intent = new Intent(ProfileActivity.this, TransactionHistoryActivity.class);
                intent.putExtra("sessionID", sessionId);  // Pass session ID
                startActivity(intent);
            }else if (menuItem.getItemId()== R.id.nav_market_history){
                Intent intent = new Intent(ProfileActivity.this, MarketHistoryActivity.class);
                intent.putExtra("sessionID", sessionId);  // Pass session ID
                startActivity(intent);
            }
            else if (menuItem.getItemId()== R.id.nav_marketlaod){
                Intent intent = new Intent(ProfileActivity.this, MarketActivity.class);
                intent.putExtra("sessionID", sessionId);  // Pass session ID
                startActivity(intent);
            }
            else if (menuItem.getItemId() == R.id.nav_sign_out) {
                signOut();
            }
            drawerLayout.closeDrawer(findViewById(R.id.navigationView));
            return true;
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
        }
    }

    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pics/" + System.currentTimeMillis());
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.getDownloadUrl().addOnSuccessListener(uri ->
                            updateUserProfile(name, phone, email, password, address, uri.toString())
                    )
            ).addOnFailureListener(e ->
                    Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            );
        } else {
            updateUserProfile(name, phone, email, password, address, null);
        }
    }

    private void updateUserProfile(String name, String phone, String email, String password, String address, String profilePicUrl) {
        userDatabaseRef.child("name").setValue(name);
        userDatabaseRef.child("phone").setValue(phone);
        userDatabaseRef.child("email").setValue(email);
        userDatabaseRef.child("password").setValue(password);
        userDatabaseRef.child("address").setValue(address);
        if (profilePicUrl != null) {
            userDatabaseRef.child("profilePicUrl").setValue(profilePicUrl);
        }
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void signOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
