package com.example.bitebuzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitebuzz.R;
import com.example.bitebuzz.databinding.ItemCategoryBinding;
import com.example.bitebuzz.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal category picker used on Home and Explore.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    private final List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private int selectedPosition;

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories.addAll(categories);
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        if (position < 0 || position >= categories.size()) {
            return;
        }
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setCategories(List<Category> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        Context context = holder.binding.getRoot().getContext();
        boolean selected = position == selectedPosition;

        holder.binding.categoryEmoji.setText(category.emoji);
        holder.binding.categoryName.setText(category.name);
        holder.binding.categoryCard.setCardBackgroundColor(ContextCompat.getColor(
                context, selected ? R.color.primary_light : R.color.card));
        holder.binding.categoryCard.setStrokeColor(ContextCompat.getColor(
                context, selected ? R.color.primary : R.color.stroke));
        holder.binding.categoryName.setTextColor(ContextCompat.getColor(
                context, selected ? R.color.primary : R.color.text_primary));

        holder.binding.getRoot().setOnClickListener(view -> {
            int clickedPosition = holder.getBindingAdapterPosition();
            if (clickedPosition == RecyclerView.NO_POSITION) {
                return;
            }
            if (listener != null) {
                listener.onCategoryClick(categories.get(clickedPosition), clickedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
