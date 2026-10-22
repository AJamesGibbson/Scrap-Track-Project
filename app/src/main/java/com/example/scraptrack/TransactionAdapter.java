package com.example.scraptrack;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        // Check if the position is valid and the transactionList is not null
        if (transactionList != null && position < transactionList.size()) {
            Transaction transaction = transactionList.get(position);

            if (transaction != null) {
                // Set the customer name
                holder.customerName.setText("Customer: " + transaction.getCustomerName());

                // Set the total price
                holder.totalPrice.setText("Total Price: $" + transaction.getTotalPrice());

                // Format materials
                if (transaction.getMaterials() != null && !transaction.getMaterials().isEmpty()) {
                    StringBuilder materialsBuilder = new StringBuilder("Materials:\n");
                    for (Material material : transaction.getMaterials()) {
                        if (material != null) {
                            materialsBuilder.append(material.getName())
                                    .append(" - ").append(material.getQuantity()).append(" ").append(material.getUnit())
                                    .append(" @ $").append(material.getPrice()).append("\n");
                        }
                    }
                    holder.materials.setText(materialsBuilder.toString());
                } else {
                    holder.materials.setText("No materials available.");
                }

                // Set the transaction date
                holder.transactionDate.setText("Date: " + transaction.getTransactionDate());
            }
        } else {
            Log.e("TransactionAdapter", "Error: Invalid position or transactionList is null");
        }
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView customerName, transactionDate, materials, totalPrice;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customerNameTextView);
            transactionDate = itemView.findViewById(R.id.transactionDateTextView);
            materials = itemView.findViewById(R.id.materialsTextView);
            totalPrice = itemView.findViewById(R.id.totalPriceTextView);
        }
    }
}
