package com.example.scraptrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionActivity extends ComponentActivity {

    private EditText customerName, customerPhoneNumber, quantity;
    private Spinner itemDropdown, unitDropdown;
    private TextView totalPriceTextView, transactionInfo;
    private Button addMaterialBtn, submitBtn;
    private RecyclerView materialsRecyclerView;

    private List<Material> materialsList;
    private MaterialAdapter materialAdapter;
    private double totalPrice = 0.0;
    private double pricePerKg = 0.0;
    private Map<String, Double> itemsMap;
    private List<String> itemsList;
    private DrawerLayout drawerLayout;
    private ImageView threeDotsMenu;
    private SharedPreferences sharedPreferences;
    private String[] unitOptions = {"Kg", "Gram"};
    private String sessionId; // Session ID field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        transactionInfo = findViewById(R.id.transactionInfo);

        // Get session ID from MainActivity
        sessionId = getIntent().getStringExtra("sessionID");

        if (sessionId == null || sessionId.isEmpty()) {
            transactionInfo.setText("Unknown session");
            Toast.makeText(this, "Session ID is missing!", Toast.LENGTH_SHORT).show();
        } else {
            transactionInfo.setText("Session ID: " + sessionId);
        }

        // Initialize views
        customerName = findViewById(R.id.customerName);
        customerPhoneNumber = findViewById(R.id.customerPhoneNumber);
        quantity = findViewById(R.id.quantity);
        itemDropdown = findViewById(R.id.itemDropdown);
        unitDropdown = findViewById(R.id.unitDropdown);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        addMaterialBtn = findViewById(R.id.addMaterialBtn);
        submitBtn = findViewById(R.id.submitBtn);
        materialsRecyclerView = findViewById(R.id.materialsRecyclerView);
        drawerLayout = findViewById(R.id.drawerLayout);
        threeDotsMenu = findViewById(R.id.threeDotsMenu);

        materialsList = new ArrayList<>();
        itemsMap = new HashMap<>();
        itemsList = new ArrayList<>();

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        setupNavigation((NavigationView) findViewById(R.id.navigationView));

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitOptions);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitDropdown.setAdapter(unitAdapter);

        materialAdapter = new MaterialAdapter(materialsList, new MaterialAdapter.OnMaterialActionListener() {
            @Override
            public void onEdit(Material material, int position) {
                editMaterial(material, position);
            }

            @Override
            public void onDelete(int position) {
                deleteMaterial(position);
            }
        });

        materialsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        materialsRecyclerView.setAdapter(materialAdapter);

        addMaterialBtn.setOnClickListener(v -> addMaterial());
        submitBtn.setOnClickListener(v -> submitTransaction());

        fetchItemsFromFirebase();

        itemDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!itemsList.isEmpty()) {
                    String selectedItem = itemsList.get(position);
                    pricePerKg = itemsMap.get(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        threeDotsMenu.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

    }

    private void setupNavigation(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

                if (menuItem.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(TransactionActivity.this, MainActivity.class));
                } else if (menuItem.getItemId() == R.id.nav_transactions) {
                    // Pass the session ID to TransactionActivity
                    Intent intent = new Intent(TransactionActivity.this, TransactionActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(TransactionActivity.this, ProfileActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_analysis) {
                    Intent intent = new Intent(TransactionActivity.this, AnalysisActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }else if (menuItem.getItemId()== R.id.nav_transactions_history){
                    Intent intent = new Intent(TransactionActivity.this, TransactionHistoryActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId()== R.id.nav_marketlaod){
                    Intent intent = new Intent(TransactionActivity.this, MarketActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId()== R.id.nav_market_history){
                    Intent intent = new Intent(TransactionActivity.this, MarketHistoryActivity.class);
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
        Intent intent = new Intent(TransactionActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear stack
        startActivity(intent);
        finish();
    }

    private void fetchItemsFromFirebase() {
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("scrapItems");
        firebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String itemName = itemSnapshot.getKey();
                    Double itemPrice = itemSnapshot.getValue(Double.class);
                    if (itemPrice != null) {
                        itemsList.add(itemName);
                        itemsMap.put(itemName, itemPrice);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(TransactionActivity.this, android.R.layout.simple_spinner_item, itemsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                itemDropdown.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TransactionActivity.this, "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMaterial() {
        String itemName = itemDropdown.getSelectedItem().toString();
        double itemQuantity;

        try {
            itemQuantity = Double.parseDouble(quantity.getText().toString());
        } catch (NumberFormatException e) {
            quantity.setError("Please enter a valid quantity");
            return;
        }

        String selectedUnit = unitDropdown.getSelectedItem().toString();
        double quantityInKg = selectedUnit.equals("Gram") ? itemQuantity / 1000.0 : itemQuantity;

        double itemPrice = pricePerKg * quantityInKg;
        totalPrice += itemPrice;

        totalPriceTextView.setText("Total Price: " + totalPrice);
        materialsList.add(new Material(itemName, itemQuantity, selectedUnit, itemPrice));
        materialAdapter.notifyDataSetChanged();

        quantity.setText("");
    }

    private void submitTransaction() {
        if (materialsList.isEmpty()) {
            Toast.makeText(this, "No materials added", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "Session ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        String customer = customerName.getText().toString();
        String phoneNumber = customerPhoneNumber.getText().toString();
        String transactionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Transaction transaction = new Transaction(customer, phoneNumber, transactionDate, materialsList, totalPrice, sessionId);

        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference()
                .child("transactions").child(sessionId).push(); // Save under session ID node
        transactionsRef.setValue(transaction).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Transaction submitted successfully!", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(this, "Failed to submit transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFields() {
        customerName.setText("");
        customerPhoneNumber.setText("");
        quantity.setText("");
        totalPriceTextView.setText("Total Price: 0.0");
        materialsList.clear();
        materialAdapter.notifyDataSetChanged();
        totalPrice = 0.0;
    }

    private void deleteMaterial(int position) {
        Material material = materialsList.get(position);
        totalPrice -= material.getPrice();
        totalPriceTextView.setText("Total Price: " + totalPrice);
        materialsList.remove(position);
        materialAdapter.notifyDataSetChanged();
    }
    private void editMaterial(Material material, int position) {
        // Set the selected material's details in the input fields
        itemDropdown.setSelection(itemsList.indexOf(material.getName())); // Set the selected item in the dropdown
        quantity.setText(String.valueOf(material.getQuantity())); // Set the quantity field
        unitDropdown.setSelection(material.getUnit().equals("Gram") ? 0 : 1); // Set the unit dropdown

        // Update the button to say "Save" instead of "Add"
        addMaterialBtn.setText("Save");

        // Set the OnClickListener for "Save" action
        addMaterialBtn.setOnClickListener(v -> {
            String itemName = itemDropdown.getSelectedItem().toString();
            double itemQuantity;

            try {
                itemQuantity = Double.parseDouble(quantity.getText().toString());
            } catch (NumberFormatException e) {
                quantity.setError("Please enter a valid quantity");
                return;
            }

            String selectedUnit = unitDropdown.getSelectedItem().toString();
            double quantityInKg = selectedUnit.equals("Gram") ? itemQuantity / 1000.0 : itemQuantity;

            double itemPrice = pricePerKg * quantityInKg;

            // Update the total price
            totalPrice -= material.getPrice(); // Subtract the old price
            totalPrice += itemPrice; // Add the new price

            // Update the material in the list
            material.setName(itemName);
            material.setQuantity(itemQuantity);
            material.setUnit(selectedUnit);
            material.setPrice(itemPrice);

            // Notify the adapter of the changes
            materialAdapter.notifyItemChanged(position);

            // Update the total price text
            totalPriceTextView.setText("Total Price: " + totalPrice);

            // Reset the "Add" button and clear the input fields
            addMaterialBtn.setText("Add Material");
            quantity.setText("");
            unitDropdown.setSelection(0);
        });
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

