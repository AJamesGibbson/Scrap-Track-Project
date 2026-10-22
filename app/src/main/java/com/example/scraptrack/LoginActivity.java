package com.example.scraptrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends ComponentActivity {

    private EditText phoneInput, otpInput;
    private Button requestOtpButton, loginButton;
    private TextView changeLoginTypeText1;
    private CheckBox rememberMeCheckBox;
    private FirebaseAuth mAuth;
    private String verificationId;

    private SharedPreferences sharedPreferences;
    public static final String PREF_NAME = "LoginPrefs";
    public static final String IS_REMEMBERED = "isRemembered";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        phoneInput = findViewById(R.id.phoneInput);
        otpInput = findViewById(R.id.otpInput);
        requestOtpButton = findViewById(R.id.requestOtpButton);
        loginButton = findViewById(R.id.loginButton);
        changeLoginTypeText1= findViewById(R.id.changeLoginTypeText);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // Initialize Firebase Auth and SharedPreferences
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        if (sharedPreferences.getBoolean(IS_REMEMBERED, false)) {
            navigateToMainActivity();
        }

        changeLoginTypeText1.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        // Request OTP
        requestOtpButton.setOnClickListener(v -> {
            String phoneNumber = phoneInput.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 10) {
                Toast.makeText(LoginActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            requestOtpButton.setEnabled(false);
            Toast.makeText(this, "Requesting OTP...", Toast.LENGTH_SHORT).show();
            sendOtp(phoneNumber);

            // Re-enable the button after a delay (to prevent multiple requests)
            new android.os.Handler().postDelayed(() -> requestOtpButton.setEnabled(true), 60000);
        });

        // Verify OTP
        loginButton.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();
            if (TextUtils.isEmpty(otp)) {
                Toast.makeText(LoginActivity.this, "Enter the OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            verifyOtp(otp);
        });
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+91" + phoneNumber) // Add country code
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        String otp = credential.getSmsCode();
                        if (otp != null) {
                            otpInput.setText(otp);
                            verifyOtp(otp);
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(LoginActivity.this, "OTP Request Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        LoginActivity.this.verificationId = verificationId;
                        Toast.makeText(LoginActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOtp(String otp) {
        if (verificationId == null) {
            Toast.makeText(this, "Verification ID is null. Request OTP again.", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                // Generate or retrieve session ID (for demonstration, using user ID)
                String sessionId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Save session ID in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("sessionID", sessionId);
                editor.putBoolean(IS_REMEMBERED, rememberMeCheckBox.isChecked());
                editor.apply();

                // Navigate to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("sessionID", sessionId);  // Pass session ID
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
