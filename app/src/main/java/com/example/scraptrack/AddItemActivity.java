package com.example.scraptrack;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddItemActivity extends ComponentActivity {

    private EditText itemName, itemPrice;
    private Button saveItemBtn;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize Firebase database reference
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("scrapItems");

        // Find views by ID
        itemName = findViewById(R.id.itemName);
        itemPrice = findViewById(R.id.itemPrice);
        saveItemBtn = findViewById(R.id.saveItemBtn);

        // Set click listener for the save button
        saveItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToFirebase();
            }
        });
    }

    private void addItemToFirebase() {
        String name = itemName.getText().toString().trim();
        String priceString = itemPrice.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Please enter an item price", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceString);

        // Save the item and its price to Firebase
        firebaseDatabase.child(name).setValue(price).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddItemActivity.this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                // Optionally, clear the input fields after saving
                itemName.setText("");
                itemPrice.setText("");
            } else {
                Toast.makeText(AddItemActivity.this, "Failed to save item", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
