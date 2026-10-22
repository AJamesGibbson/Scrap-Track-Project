package com.example.scraptrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AnalysisActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView balanceTextView, expenseTextView, profitTextView;
    private PieChart expenseChart;
    private BarChart monthlyChart;

    private double currentBalance = 0.0;
    private double monthlyExpenses = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        balanceTextView = findViewById(R.id.balanceTextView);
        expenseTextView = findViewById(R.id.expenseTextView);
        profitTextView = findViewById(R.id.profitTextView);
        expenseChart = findViewById(R.id.expensePieChart);
        monthlyChart = findViewById(R.id.monthlyBarChart);

        fetchDataForAnalysis();
    }

    private void fetchDataForAnalysis() {
        String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

        // Fetch Balance
        DatabaseReference balanceRef = FirebaseDatabase.getInstance().getReference()
                .child("Balance")
                .child(sessionId)
                .child("Blnce");

        balanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentBalance = snapshot.getValue(Double.class) != null ? snapshot.getValue(Double.class) : 0.0;
                    balanceTextView.setText("Starting Balance: ₹" + String.format("%.2f", currentBalance));
                } else {
                    balanceTextView.setText("Balance: ₹0.00");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AnalysisActivity.this, "Failed to fetch balance", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Monthly Expenses
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        DatabaseReference expensesRef = FirebaseDatabase.getInstance().getReference()
                .child("Expenses")
                .child(sessionId)
                .child(currentMonth);

        expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    monthlyExpenses = snapshot.getValue(Double.class) != null ? snapshot.getValue(Double.class) : 0.0;
                    expenseTextView.setText("Monthly Expenses: ₹" + String.format("%.2f", monthlyExpenses));
                    calculateAndDisplayProfit();
                    populateCharts();
                } else {
                    expenseTextView.setText("Monthly Expenses: ₹0.00");
                    calculateAndDisplayProfit();
                    populateCharts();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AnalysisActivity.this, "Failed to fetch expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndDisplayProfit() {
        double profit = currentBalance - monthlyExpenses;
        profitTextView.setText("Profit: ₹" + String.format("%.2f", profit));
    }

    private void populateCharts() {
        // Pie Chart for Expense Distribution
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry((float) monthlyExpenses, "Expenses"));
        pieEntries.add(new PieEntry((float) (currentBalance - monthlyExpenses), "Balance"));

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Expense Analysis");
        PieData pieData = new PieData(pieDataSet);
        expenseChart.setData(pieData);
        expenseChart.invalidate();

        // Bar Chart for Monthly Metrics
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, (float) monthlyExpenses));
        barEntries.add(new BarEntry(2, (float) currentBalance));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Metrics");
        BarData barData = new BarData(barDataSet);
        monthlyChart.setData(barData);
        monthlyChart.invalidate();
    }
}
