package com.example.scraptrack;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHistoryActivity extends ComponentActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private List<Transaction> filteredTransactions;
    private SharedPreferences sharedPreferences;
    private ImageView threeDotsMenu;
    private DrawerLayout drawerLayout;
    private TextView dateFilterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        recyclerView = findViewById(R.id.transactionHistoryRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionList = new ArrayList<>();
        filteredTransactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(filteredTransactions);
        recyclerView.setAdapter(transactionAdapter);
        drawerLayout = findViewById(R.id.drawerLayout);
        dateFilterTextView = findViewById(R.id.dateFilterTextView);
        threeDotsMenu = findViewById(R.id.threeDotsMenu);

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

        fetchTransactionHistory(sessionId);

        dateFilterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        setupNavigation((NavigationView) findViewById(R.id.navigationView));
        threeDotsMenu.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void fetchTransactionHistory(String sessionId) {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference()
                .child("transactions")
                .child(sessionId);

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                filteredTransactions.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                        Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                        if (transaction != null) {
                            transactionList.add(transaction);
                        }
                    }
                    transactionList.sort((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()));
                    filteredTransactions.addAll(transactionList);
                    transactionAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(TransactionHistoryActivity.this, "No transactions found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TransactionHistoryActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        // Update the TextView with the selected date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String selectedDate = dateFormat.format(calendar.getTime());
                        dateFilterTextView.setText("Selected Date: " + selectedDate); // Display the selected date
                        filterTransactionsByDate(calendar.getTime());
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void filterTransactionsByDate(Date selectedDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateString = dateFormat.format(selectedDate); // Format the selected date

        filteredTransactions.clear();
        for (Transaction transaction : transactionList) {
            try {
                String transactionDateString = transaction.getTransactionDate(); // Assume it's a String
                if (transactionDateString != null) {
                    // Parse the transaction date string to a Date object
                    Date transactionDate = dateFormat.parse(transactionDateString);
                    if (transactionDate != null && dateFormat.format(transactionDate).equals(selectedDateString)) {
                        filteredTransactions.add(transaction);
                    }
                }
            } catch (ParseException e) {
                Log.e("DateParseError", "Error parsing transaction date", e);
            }
        }

        if (filteredTransactions.isEmpty()) {
            Toast.makeText(this, "No transactions found for the selected date", Toast.LENGTH_SHORT).show();
        }
        transactionAdapter.notifyDataSetChanged(); // Update RecyclerView
    }

    private void setupNavigation(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

                if (menuItem.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(TransactionHistoryActivity.this, MainActivity.class));
                } else if (menuItem.getItemId() == R.id.nav_transactions) {
                    // Pass the session ID to TransactionActivity
                    Intent intent = new Intent(TransactionHistoryActivity.this, TransactionActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(TransactionHistoryActivity.this, ProfileActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_analysis) {
                    Intent intent = new Intent(TransactionHistoryActivity.this, AnalysisActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }else if (menuItem.getItemId()== R.id.nav_transactions_history){
                    Intent intent = new Intent(TransactionHistoryActivity.this, TransactionHistoryActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId()== R.id.nav_marketlaod){
                    Intent intent = new Intent(TransactionHistoryActivity.this, MarketActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId()== R.id.nav_market_history){
                    Intent intent = new Intent(TransactionHistoryActivity.this, MarketHistoryActivity.class);
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
        Intent intent = new Intent(TransactionHistoryActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear stack
        startActivity(intent);
        finish();
    }
}
