package com.example.bitebuzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitebuzz.R;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.ItemFoodListBinding;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Restaurant;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Wide food row used for search results, favourites and menus.
 */
public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.FoodViewHolder> {

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem food);
    }

    private final List<FoodItem> foods = new ArrayList<>();
    private final OnFoodClickListener listener;
    private final AppRepository repository;

    public FoodListAdapter(Context context, List<FoodItem> initialFoods, OnFoodClickListener listener) {
        this.foods.addAll(initialFoods);
        this.listener = listener;
        this.repository = AppRepository.getInstance(context);
    }

    public void setFoods(List<FoodItem> newFoods) {
        foods.clear();
        foods.addAll(newFoods);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodListBinding binding = ItemFoodListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem food = foods.get(position);
        Context context = holder.binding.getRoot().getContext();
        Restaurant restaurant = FoodData.getRestaurantById(food.restaurantId);

        holder.binding.foodImage.setImageResource(food.imageRes);
        holder.binding.foodName.setText(food.name);
        holder.binding.foodRestaurant.setText(restaurant == null ? "" : restaurant.name);
        holder.binding.foodRating.setText(PriceUtils.formatRating(food.rating));
        holder.binding.reviewCount.setText(
                context.getString(R.string.review_count_short, food.reviewCount));
        holder.binding.foodPrepTime.setText(context.getString(R.string.preparation_time, food.prepMinutes));
        holder.binding.foodPrice.setText(PriceUtils.format(food.price));
        holder.binding.vegIndicator.setImageResource(
                food.vegetarian ? R.drawable.ic_veg : R.drawable.ic_nonveg);

        boolean favorite = repository.isFavorite(food.id);
        holder.binding.favoriteButton.setImageResource(
                favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        holder.binding.favoriteButton.setColorFilter(ContextCompat.getColor(
                context, favorite ? R.color.primary : R.color.text_hint));

        holder.binding.getRoot().setOnClickListener(view -> {
            if (listener != null) {
                listener.onFoodClick(food);
            }
        });

        holder.binding.favoriteButton.setOnClickListener(view -> {
            boolean nowFavorite = repository.toggleFavorite(food.id);
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(adapterPosition);
            }
            UiUtils.showSnackbar(holder.binding.getRoot(), context.getString(
                    nowFavorite ? R.string.favorite_added : R.string.favorite_removed));
        });

        holder.binding.addButton.setOnClickListener(view -> {
            repository.addToCart(food, 1);
            UiUtils.showSnackbar(holder.binding.getRoot(),
                    context.getString(R.string.added_to_cart, food.name));
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        final ItemFoodListBinding binding;

        FoodViewHolder(ItemFoodListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
