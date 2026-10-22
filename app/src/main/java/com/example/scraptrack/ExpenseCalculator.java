package com.example.scraptrack;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpenseCalculator {

    public static void calculateAndStoreMonthlyExpenses(String sessionId) {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        final double[] totalExpenses = {0.0};

        // Reference to the transactions for the sessionId
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference()
                .child("transactions")
                .child(sessionId);

        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Iterate through all transactions for the given sessionId
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    // Extract the transactionDate and check if it belongs to the current month
                    String transactionDate = transactionSnapshot.child("transactionDate").getValue(String.class);

                    // Extract the year-month part from the date string (e.g., "2024-11")
                    String transactionMonth = transactionDate != null ? transactionDate.substring(0, 7) : "";

                    // If the transaction is in the current month, sum up the price
                    if (transactionMonth.equals(currentMonth)) {
                        // Get the transaction's totalPrice and add it to the total
                        Double transactionPrice = transactionSnapshot.child("totalPrice").getValue(Double.class);
                        if (transactionPrice != null) {
                            totalExpenses[0] += transactionPrice;
                        }
                    }
                }

                // Reference to store the calculated total expenses in the 'Expenses' node
                DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference()
                        .child("Expenses")
                        .child(sessionId);

                // Check if expenses data exists for the session ID
                expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // If no data exists, create a new node and set the total expenses for the current month
                            expensesRef.child(currentMonth).setValue(totalExpenses[0]);
                        } else {
                            // Update the expenses for the current month if data already exists
                            expensesRef.child(currentMonth).setValue(totalExpenses[0]);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Handle any errors during database read
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database read errors
            }
        });
    }
}
