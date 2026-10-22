package com.example.scraptrack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder> {

    private List<Material> materialsList;
    private OnMaterialActionListener materialActionListener;

    public MaterialAdapter(List<Material> materialsList, OnMaterialActionListener listener) {
        this.materialsList = materialsList;
        this.materialActionListener = listener;
    }

    @NonNull
    @Override
    public MaterialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_material, parent, false);
        return new MaterialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialViewHolder holder, int position) {
        Material material = materialsList.get(position);
        holder.itemName.setText(material.getName());
        holder.itemQuantity.setText("Quantity: " + material.getQuantity() + " " + material.getUnit());
        holder.itemPrice.setText("Price: " + material.getPrice());

        // Set edit and delete actions
        holder.editButton.setOnClickListener(v -> materialActionListener.onEdit(material, position));
        holder.deleteButton.setOnClickListener(v -> materialActionListener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return materialsList.size();
    }

    public static class MaterialViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity, itemPrice;
        Button editButton, deleteButton;

        public MaterialViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            editButton = itemView.findViewById(R.id.editBtn);
            deleteButton = itemView.findViewById(R.id.deleteBtn);
        }
    }

    public interface OnMaterialActionListener {
        void onEdit(Material material, int position);
        void onDelete(int position);
    }
}
