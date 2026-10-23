package com.example.bitebuzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitebuzz.R;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.ItemFoodMiniBinding;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Restaurant;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact horizontal food cards used for Buzz Mode recommendations.
 */
public class FoodMiniAdapter extends RecyclerView.Adapter<FoodMiniAdapter.FoodMiniViewHolder> {

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem food);
    }

    private final List<FoodItem> foods = new ArrayList<>();
    private final OnFoodClickListener listener;
    private final AppRepository repository;

    public FoodMiniAdapter(Context context, List<FoodItem> initialFoods, OnFoodClickListener listener) {
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
    public FoodMiniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodMiniBinding binding = ItemFoodMiniBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodMiniViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodMiniViewHolder holder, int position) {
        FoodItem food = foods.get(position);
        Context context = holder.binding.getRoot().getContext();
        Restaurant restaurant = FoodData.getRestaurantById(food.restaurantId);

        holder.binding.miniFoodImage.setImageResource(food.imageRes);
        holder.binding.miniFoodName.setText(food.name);
        holder.binding.miniFoodRestaurant.setText(restaurant == null ? "" : restaurant.name);
        holder.binding.miniFoodRating.setText(PriceUtils.formatRating(food.rating));
        holder.binding.miniFoodPrice.setText(PriceUtils.format(food.price));

        holder.binding.getRoot().setOnClickListener(view -> {
            if (listener != null) {
                listener.onFoodClick(food);
            }
        });

        holder.binding.miniAddButton.setOnClickListener(view -> {
            repository.addToCart(food, 1);
            UiUtils.showSnackbar(holder.binding.getRoot(),
                    context.getString(R.string.added_to_cart, food.name));
        });
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class FoodMiniViewHolder extends RecyclerView.ViewHolder {
        final ItemFoodMiniBinding binding;

        FoodMiniViewHolder(ItemFoodMiniBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
