package com.example.aimentor.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aimentor.R;
import com.example.aimentor.models.CategoryModel;

import java.util.ArrayList;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryItemViewHolder> {

    public ArrayList<CategoryModel> categoryModels;
    public Context context;
    public CategoryListAdapter(ArrayList<CategoryModel> model, Context myContext){
        categoryModels = model;
        context = myContext;
    }

    @NonNull
    @Override
    public CategoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_list, parent, false);
        return new CategoryItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryItemViewHolder holder, int position) {
        CategoryModel model = categoryModels.get(position); // lay du lieu theo tung item
        holder.tvCategoryName.setText(model.getCategoryName());
        holder.tvCreatedAt.setText(String.format("Created at: %s", model.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return categoryModels.size(); // co nhieu du lieu trong database
    }

    public static class CategoryItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCreatedAt;
        View viewItem;
        public CategoryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            viewItem = itemView;
            tvCategoryName = viewItem.findViewById(R.id.tvNameCategory);
            tvCreatedAt    = viewItem.findViewById(R.id.tvTimeCategory);
        }
    }
}
