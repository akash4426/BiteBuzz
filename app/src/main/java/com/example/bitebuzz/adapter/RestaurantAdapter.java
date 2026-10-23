package com.example.bitebuzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitebuzz.R;
import com.example.bitebuzz.databinding.ItemRestaurantBinding;
import com.example.bitebuzz.model.Restaurant;
import com.example.bitebuzz.util.PriceUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal restaurant carousel on the Home screen.
 */
public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    private final List<Restaurant> restaurants = new ArrayList<>();
    private final OnRestaurantClickListener listener;

    public RestaurantAdapter(List<Restaurant> initialRestaurants, OnRestaurantClickListener listener) {
        this.restaurants.addAll(initialRestaurants);
        this.listener = listener;
    }

    public void setRestaurants(List<Restaurant> newRestaurants) {
        restaurants.clear();
        restaurants.addAll(newRestaurants);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRestaurantBinding binding = ItemRestaurantBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        Context context = holder.binding.getRoot().getContext();

        holder.binding.restaurantImage.setImageResource(restaurant.imageRes);
        holder.binding.restaurantName.setText(restaurant.name);
        holder.binding.restaurantCuisine.setText(restaurant.cuisine);
        holder.binding.restaurantRating.setText(PriceUtils.formatRating(restaurant.rating));
        holder.binding.restaurantTime.setText(restaurant.getDeliveryTime());
        holder.binding.restaurantOffer.setText(restaurant.offer);
        holder.binding.restaurantPriceTwo.setText(
                context.getString(R.string.price_for_two, restaurant.priceForTwo));

        holder.binding.getRoot().setOnClickListener(view -> {
            if (listener != null) {
                listener.onRestaurantClick(restaurant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        final ItemRestaurantBinding binding;

        RestaurantViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
