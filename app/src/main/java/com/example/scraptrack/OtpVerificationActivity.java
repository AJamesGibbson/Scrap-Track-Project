package com.example.scraptrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OtpVerificationActivity extends ComponentActivity {

    private EditText otpInput;
    private Button verifyOtpButton;
    private String verificationId, name, phoneNumber, email, sessionId;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otpverification_activity);

        otpInput = findViewById(R.id.otpInput);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get the passed data from the Intent
        verificationId = getIntent().getStringExtra("verificationId");
        name = getIntent().getStringExtra("name");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        email = getIntent().getStringExtra("email");
        sessionId = getIntent().getStringExtra("sessionId");

        verifyOtpButton.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();
            if (TextUtils.isEmpty(otp)) {
                Toast.makeText(OtpVerificationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(otp);
        });
    }

    private void verifyOtp(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save the user details to Firebase
                        saveUserDetailsToFirebase();
                    } else {
                        Toast.makeText(OtpVerificationActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDetailsToFirebase() {
        User user = new User(name, phoneNumber, email, sessionId);
        mDatabase.child("users").child(sessionId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(OtpVerificationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        // Navigate to the next activity
                        Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(OtpVerificationActivity.this, "Error saving user details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class User {
        public String name, phoneNumber, email, sessionId;

        public User(String name, String phoneNumber, String email, String sessionId) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.sessionId = sessionId;
        }
    }
}
