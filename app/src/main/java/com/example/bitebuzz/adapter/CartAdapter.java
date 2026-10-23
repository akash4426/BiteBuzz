package com.example.bitebuzz.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitebuzz.R;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.ItemCartBinding;
import com.example.bitebuzz.model.CartItem;
import com.example.bitebuzz.model.Restaurant;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the cart list. Quantity changes go straight
 * through the repository, which refreshes every screen.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> items = new ArrayList<>();
    private final AppRepository repository;

    public CartAdapter(Context context, List<CartItem> initialItems) {
        this.items.addAll(initialItems);
        this.repository = AppRepository.getInstance(context);
    }

    public void setItems(List<CartItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        Context context = holder.binding.getRoot().getContext();
        Restaurant restaurant = FoodData.getRestaurantById(item.food.restaurantId);

        holder.binding.cartItemImage.setImageResource(item.food.imageRes);
        holder.binding.cartItemName.setText(item.food.name);
        holder.binding.cartItemRestaurant.setText(restaurant == null ? "" : restaurant.name);
        holder.binding.cartItemQuantity.setText(String.valueOf(item.quantity));
        holder.binding.cartItemTotal.setText(PriceUtils.format(item.getLineTotal()));

        String extras = item.getExtrasLabel();
        if (TextUtils.isEmpty(extras)) {
            holder.binding.cartItemExtras.setVisibility(View.GONE);
        } else {
            holder.binding.cartItemExtras.setVisibility(View.VISIBLE);
            holder.binding.cartItemExtras.setText(extras);
        }

        holder.binding.increaseButton.setOnClickListener(view ->
                repository.incrementCartItem(item));

        holder.binding.decreaseButton.setOnClickListener(view -> {
            if (item.quantity <= 1) {
                UiUtils.showSnackbar(holder.binding.getRoot(),
                        context.getString(R.string.item_removed, item.food.name));
            }
            repository.decrementCartItem(item);
        });

        holder.binding.removeItemButton.setOnClickListener(view -> {
            UiUtils.showSnackbar(holder.binding.getRoot(),
                    context.getString(R.string.item_removed, item.food.name));
            repository.removeCartItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
