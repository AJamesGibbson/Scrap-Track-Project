package com.example.scraptrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ComponentActivity {

    private DrawerLayout drawerLayout;
    private ImageView threeDotsMenu;
    private SharedPreferences sharedPreferences;
    private TextView sessionInfo;
    private TextView balanceTextView, expensesTextView;

    private double previousMonthBalance = 0.0;
    private double monthlyExpenses = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        // Retrieve session ID from Intent or SharedPreferences
        String sessionId = getIntent().getStringExtra("sessionID");
        if (sessionId == null) {
            sessionId = sharedPreferences.getString("sessionID", "Unknown session");
        }

        // Log session ID for verification
        Log.d("MainActivity", "Received Session ID: " + sessionId);

        // Save session ID back to SharedPreferences for consistency
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sessionID", sessionId);
        editor.apply();

        ExpenseCalculator.calculateAndStoreMonthlyExpenses(sessionId);

        displayUserEmail();

        setupDrawerToggle();
        setupNavigation((NavigationView) findViewById(R.id.navigationView));

        fetchBalanceAndExpenses();
    }

    private void initializeUI() {
        drawerLayout = findViewById(R.id.drawerLayout);
        threeDotsMenu = findViewById(R.id.threeDotsMenu);
        sessionInfo = findViewById(R.id.sessionInfo);
        balanceTextView = findViewById(R.id.balanceTextView);
        expensesTextView = findViewById(R.id.expensesTextView);
    }

    private void displayUserEmail() {
        String userEmail = sharedPreferences.getString("userEmail", "No email found");
        String sessionId = sharedPreferences.getString("sessionID", "Unknown session");
        sessionInfo.setText("Logged in as: " + userEmail + "\nSession ID: " + sessionId);
    }

    private void setupDrawerToggle() {
        threeDotsMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.navigationView))) {
                drawerLayout.closeDrawer(findViewById(R.id.navigationView));
            } else {
                drawerLayout.openDrawer(findViewById(R.id.navigationView));
            }
        });
    }

    private void setupNavigation(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

                if (menuItem.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                } else if (menuItem.getItemId() == R.id.nav_transactions) {
                    // Pass the session ID to TransactionActivity
                    Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_analysis) {
                    Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }else if (menuItem.getItemId()== R.id.nav_transactions_history){
                    Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }else if (menuItem.getItemId()== R.id.nav_market_history){
                    Intent intent = new Intent(MainActivity.this, MarketHistoryActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId()== R.id.nav_marketlaod){
                    Intent intent = new Intent(MainActivity.this, MarketActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId() == R.id.nav_sign_out) {
                    signOut();
                }
                drawerLayout.closeDrawer(findViewById(R.id.navigationView));
                return true;
            }
        });
    }

    private void signOut() {
        // Clear session data in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("sessionID"); // Remove session ID
        editor.putBoolean(LoginActivity.IS_REMEMBERED, false); // Reset remember me checkbox
        editor.apply();

        // Redirect to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear stack
        startActivity(intent);
        finish();
    }


    private void updateBalanceAndExpenses() {
        double currentBalance = previousMonthBalance - monthlyExpenses;

        String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

        // Update balance in Firebase
        DatabaseReference balanceRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(sessionId)
                .child("balance");

        balanceRef.setValue(currentBalance).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Balance updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
            }
        });

        // Update UI
        balanceTextView.setText("Balance: ₹" + String.format("%.2f", currentBalance));
        expensesTextView.setText("Monthly Expenses: ₹" + String.format("%.2f", monthlyExpenses));
    }

    private void fetchBalanceAndExpenses() {
        String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

        // Fetch Balance
        DatabaseReference balanceRef = FirebaseDatabase.getInstance().getReference()
                .child("Balance")
                .child(sessionId)
                .child("Blnce");

        balanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double balance = snapshot.getValue(Double.class);
                    if (balance != null) {
                        previousMonthBalance = balance;
                    } else {
                        previousMonthBalance = 0.0; // Default to 0 if balance is null
                    }
                    updateBalanceAndExpenses();
                } else {
                    Toast.makeText(MainActivity.this, "Balance not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to fetch balance", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Expenses
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference()
                .child("Expenses")
                .child(sessionId)
                .child("Exp");

        expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double expenses = snapshot.getValue(Double.class);
                    if (expenses != null) {
                        monthlyExpenses = expenses;
                    } else {
                        monthlyExpenses = 0.0; // Default to 0 if expenses are null
                    }
                    updateBalanceAndExpenses();
                } else {
                    Toast.makeText(MainActivity.this, "Expenses not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to fetch expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
