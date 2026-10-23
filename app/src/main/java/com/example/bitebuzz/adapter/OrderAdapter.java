package com.example.bitebuzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitebuzz.R;
import com.example.bitebuzz.databinding.ItemOrderBinding;
import com.example.bitebuzz.model.Order;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Order history rows used on the Profile screen.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final List<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public OrderAdapter(List<Order> initialOrders, OnOrderClickListener listener) {
        this.orders.addAll(initialOrders);
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        Context context = holder.binding.getRoot().getContext();

        holder.binding.orderId.setText(order.id);
        holder.binding.orderStatus.setText(statusLabel(context, order.status));
        holder.binding.orderAmount.setText(PriceUtils.format(order.total));
        int itemCount = order.getItemCount();
        holder.binding.orderMeta.setText(context.getResources().getQuantityString(
                R.plurals.order_meta,
                itemCount,
                itemCount,
                UiUtils.formatOrderDate(order.createdAt)));

        int tagBackground;
        int tagText;
        switch (order.status) {
            case Order.STATUS_DELIVERED:
                tagBackground = R.drawable.bg_tag_success;
                tagText = R.color.success;
                break;
            case Order.STATUS_OUT_FOR_DELIVERY:
                tagBackground = R.drawable.bg_tag_offer;
                tagText = R.color.primary_dark;
                break;
            case Order.STATUS_PREPARING:
                tagBackground = R.drawable.bg_tag_offer;
                tagText = R.color.secondary;
                break;
            default:
                tagBackground = R.drawable.bg_tag_neutral;
                tagText = R.color.text_secondary;
                break;
        }
        holder.binding.orderStatus.setBackgroundResource(tagBackground);
        holder.binding.orderStatus.setTextColor(ContextCompat.getColor(context, tagText));

        holder.binding.getRoot().setOnClickListener(view -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    private String statusLabel(Context context, int status) {
        @StringRes int label;
        switch (status) {
            case Order.STATUS_PREPARING:
                label = R.string.status_preparing;
                break;
            case Order.STATUS_OUT_FOR_DELIVERY:
                label = R.string.status_out_for_delivery;
                break;
            case Order.STATUS_DELIVERED:
                label = R.string.status_delivered;
                break;
            default:
                label = R.string.status_order_confirmed;
                break;
        }
        return context.getString(label);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        final ItemOrderBinding binding;

        OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
