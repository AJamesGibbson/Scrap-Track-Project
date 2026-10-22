package com.example.scraptrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketActivity extends ComponentActivity implements MaterialAdapter.OnMaterialActionListener {

    private EditText customerNameEditText, customerPhoneNumberEditText, quantityEditText;
    private Spinner itemDropdown, unitDropdown;
    private Button addMaterialBtn, submitBtn;
    private RecyclerView materialsRecyclerView;
    private TextView totalPriceTextView;
    private DrawerLayout drawerLayout;
    private ImageView threeDotsMenu;

    private List<String> itemsList;
    private Map<String, Double> itemsMap;
    private double totalPrice = 0.0;
    private SharedPreferences sharedPreferences;
    private String sessionId;
    private List<Material> materialsList;
    private String selectedUnit = "Kg";
    private MaterialAdapter materialAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        // Get session ID from MainActivity
        sessionId = getIntent().getStringExtra("sessionID");

        // Initialize views
        customerNameEditText = findViewById(R.id.customerName);
        customerPhoneNumberEditText = findViewById(R.id.customerPhoneNumber);
        itemDropdown = findViewById(R.id.itemDropdown);
        unitDropdown = findViewById(R.id.unitDropdown);
        quantityEditText = findViewById(R.id.quantity);
        addMaterialBtn = findViewById(R.id.addMaterialBtn);
        submitBtn = findViewById(R.id.submitBtn);
        materialsRecyclerView = findViewById(R.id.materialsRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        drawerLayout = findViewById(R.id.drawerLayout);
        threeDotsMenu = findViewById(R.id.threeDotsMenu);

        materialsList = new ArrayList<>();
        itemsMap = new HashMap<>();
        itemsList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        // Set up navigation drawer
        setupNavigation((NavigationView) findViewById(R.id.navigationView));

        materialAdapter = new MaterialAdapter(materialsList, this);

        materialsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        materialsRecyclerView.setAdapter(materialAdapter);

        // Fetch items from Firebase
        fetchItemsFromFirebase();

        // Set up Item Dropdown
        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemsList);
        itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemDropdown.setAdapter(itemAdapter);

        // Set up Unit Dropdown
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Kg", "Gram"});
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitDropdown.setAdapter(unitAdapter);

        // Unit Dropdown Listener
        unitDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUnit = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Add Material Button
        addMaterialBtn.setOnClickListener(v -> addMaterial());

        // Submit Button
        submitBtn.setOnClickListener(v -> submitItems());

        // Three dots menu to open navigation drawer
        threeDotsMenu.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupNavigation(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                String sessionId = sharedPreferences.getString("sessionID", "Unknown session");

                if (menuItem.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(MarketActivity.this, MainActivity.class));
                } else if (menuItem.getItemId() == R.id.nav_transactions) {
                    // Pass the session ID to TransactionActivity
                    Intent intent = new Intent(MarketActivity.this, TransactionActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_profile) {
                    Intent intent = new Intent(MarketActivity.this, ProfileActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                } else if (menuItem.getItemId() == R.id.nav_analysis) {
                    Intent intent = new Intent(MarketActivity.this, AnalysisActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }else if (menuItem.getItemId()== R.id.nav_transactions_history){
                    Intent intent = new Intent(MarketActivity.this, TransactionHistoryActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId()== R.id.nav_marketlaod){
                    Intent intent = new Intent(MarketActivity.this, MarketActivity.class);
                    intent.putExtra("sessionID", sessionId);  // Pass session ID
                    startActivity(intent);
                }
                else if (menuItem.getItemId()== R.id.nav_market_history){
                    Intent intent = new Intent(MarketActivity.this, MarketHistoryActivity.class);
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
        Intent intent = new Intent(MarketActivity.this, LoginActivity.class);
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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MarketActivity.this, android.R.layout.simple_spinner_item, itemsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                itemDropdown.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MarketActivity.this, "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMaterial() {
        String customerName = customerNameEditText.getText().toString();
        String customerPhoneNumber = customerPhoneNumberEditText.getText().toString();
        String itemName = itemDropdown.getSelectedItem().toString();
        String quantityStr = quantityEditText.getText().toString();

        if (customerName.isEmpty() || customerPhoneNumber.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity = Double.parseDouble(quantityStr);
        double itemPrice = itemsMap.get(itemName);
        double totalItemPrice = itemPrice * quantity;

        totalPrice += totalItemPrice;
        totalPriceTextView.setText("Total Price: " + totalPrice);

        // Add material to the list for RecyclerView
        materialsList.add(new Material(itemName, quantity, selectedUnit, totalItemPrice));
        materialAdapter.notifyDataSetChanged();

        quantityEditText.setText(""); // Clear the quantity field
    }

    private void submitItems() {
        if (totalPrice <= 0) {
            Toast.makeText(this, "No items added", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save items in Firebase under "marketload" node
        DatabaseReference marketLoadRef = FirebaseDatabase.getInstance().getReference().child("marketload").child(sessionId);

        Map<String, Object> marketData = new HashMap<>();
        marketData.put("customerName", customerNameEditText.getText().toString());
        marketData.put("customerPhoneNumber", customerPhoneNumberEditText.getText().toString());
        marketData.put("totalPrice", totalPrice);

        // Add each material and its price
        for (Material material : materialsList) {
            marketData.put(material.getName(), material.getPrice());
        }

        marketLoadRef.push().setValue(marketData);

        Toast.makeText(this, "Items submitted successfully", Toast.LENGTH_SHORT).show();
        totalPrice = 0; // Reset the total price
        totalPriceTextView.setText("Total Price: 0.00"); // Reset display
        materialsList.clear();
        materialAdapter.notifyDataSetChanged();
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
        quantityEditText.setText(String.valueOf(material.getQuantity())); // Set the quantity field
        unitDropdown.setSelection(material.getUnit().equals("Gram") ? 0 : 1); // Set the unit dropdown

        // Update the button to say "Save" instead of "Add"
        addMaterialBtn.setText("Save");

        // Set the OnClickListener for "Save" action
        addMaterialBtn.setOnClickListener(v -> {
            String itemName = itemDropdown.getSelectedItem().toString();
            double itemQuantity;

            try {
                itemQuantity = Double.parseDouble(quantityEditText.getText().toString());
            } catch (NumberFormatException e) {
                quantityEditText.setError("Please enter a valid quantity");
                return;
            }

            String selectedUnit = unitDropdown.getSelectedItem().toString();
            double quantityInKg = selectedUnit.equals("Gram") ? itemQuantity / 1000.0 : itemQuantity;

            double itemPrice = itemsMap.get(itemName) * quantityInKg;

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
            quantityEditText.setText("");
            unitDropdown.setSelection(0);
        });
    }

    @Override
    public void onEdit(Material material, int position) {
        // Call the editMaterial method
        editMaterial(material, position);
    }

    @Override
    public void onDelete(int position) {
        // Call the deleteMaterial method
        deleteMaterial(position);
    }
}
