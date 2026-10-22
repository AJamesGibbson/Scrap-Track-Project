package com.example.scraptrack;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BalanceActivity extends ComponentActivity {

    private EditText balanceEditText;
    private Button submitButton;
    private TextView totalExpensesTextView;
    private double totalExpenses = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        // Initialize UI components
        balanceEditText = findViewById(R.id.balanceEditText);
        submitButton = findViewById(R.id.submitButton);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);

        // Fetch and calculate total expenses from transactions
        calculateTotalExpenses();

        submitButton.setOnClickListener(v -> submitBalance());
    }

    private void calculateTotalExpenses() {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference().child("transactions");

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                totalExpenses = 0.0;

                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    Double price = transactionSnapshot.child("totalPrice").getValue(Double.class);
                    if (price != null) {
                        totalExpenses += price;
                    }
                }

                // Display the total expenses
                totalExpensesTextView.setText("Total Expenses: $" + String.format("%.2f", totalExpenses));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BalanceActivity.this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitBalance() {
        String balanceInput = balanceEditText.getText().toString().trim();

        if (balanceInput.isEmpty()) {
            Toast.makeText(this, "Please enter a balance", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse input to double
        double balance = Double.parseDouble(balanceInput);

        // Save balance to Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("balance").setValue(balance)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BalanceActivity.this, "Balance updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BalanceActivity.this, "Failed to update balance", Toast.LENGTH_SHORT).show();
                    }
                });

        // Save total expenses to Firebase
        databaseReference.child("expenses").setValue(totalExpenses)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BalanceActivity.this, "Expenses updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BalanceActivity.this, "Failed to update expenses", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
