package com.example.scraptrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends ComponentActivity {

    private static final String TAG = "RegistrationActivity";

    private EditText nameInput, phoneInput, emailInput;
    private Button sendOtpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        sendOtpButton = findViewById(R.id.sendOtpButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://fir-ee3b3-default-rtdb.firebaseio.com/").getReference();

        sendOtpButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phoneNumber = phoneInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(email)) {
                Toast.makeText(RegistrationActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
                Toast.makeText(RegistrationActivity.this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegistrationActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the phone number already exists in the database
            checkPhoneNumberExists(phoneNumber, name, email);
        });
    }

    private void checkPhoneNumberExists(String phoneNumber, String name, String email) {
        mDatabase.child("users").orderByChild("phoneNumber").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If the phone number already exists, show a message and go to the login page
                    Toast.makeText(RegistrationActivity.this, "User is already registered", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // If the phone number does not exist, send OTP directly
                    sendOtp(phoneNumber, name, email);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "DatabaseError: " + databaseError.getMessage());
                Toast.makeText(RegistrationActivity.this, "Error checking phone number. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOtp(String phoneNumber, String name, String email) {
        String sessionId = UUID.randomUUID().toString();

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                handleVerificationFailure(e);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(RegistrationActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();

                // Pass user details to next activity
                Intent intent = new Intent(RegistrationActivity.this, OtpVerificationActivity.class);
                intent.putExtra("verificationId", verificationId);
                intent.putExtra("name", name);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("email", email);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
                finish();
            }
        };

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void handleVerificationFailure(FirebaseException e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(RegistrationActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
        } else if (e instanceof FirebaseTooManyRequestsException) {
            Toast.makeText(RegistrationActivity.this, "SMS quota exceeded. Try again later.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegistrationActivity.this, "Verification failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Handle successful sign-in
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(RegistrationActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
