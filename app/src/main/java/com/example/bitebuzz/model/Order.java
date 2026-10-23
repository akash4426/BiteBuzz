package com.example.bitebuzz.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A locally stored order. Status values:
 * 0 = confirmed, 1 = preparing, 2 = out for delivery, 3 = delivered.
 */
public class Order {

    public static final int STATUS_CONFIRMED = 0;
    public static final int STATUS_PREPARING = 1;
    public static final int STATUS_OUT_FOR_DELIVERY = 2;
    public static final int STATUS_DELIVERED = 3;

    public final String id;
    public final long createdAt;
    public final List<CartItem> items;
    public final int subtotal;
    public final int deliveryFee;
    public final int discount;
    public final int total;
    public final String address;
    public final String paymentMethod;
    public int status;

    public Order(String id,
                 long createdAt,
                 List<CartItem> items,
                 int subtotal,
                 int deliveryFee,
                 int discount,
                 int total,
                 String address,
                 String paymentMethod,
                 int status) {
        this.id = id;
        this.createdAt = createdAt;
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.discount = discount;
        this.total = total;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.quantity;
        }
        return count;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("createdAt", createdAt);
        json.put("subtotal", subtotal);
        json.put("deliveryFee", deliveryFee);
        json.put("discount", discount);
        json.put("total", total);
        json.put("address", address);
        json.put("paymentMethod", paymentMethod);
        json.put("status", status);

        JSONArray itemArray = new JSONArray();
        for (CartItem item : items) {
            itemArray.put(item.toJson());
        }
        json.put("items", itemArray);
        return json;
    }

    public static Order fromJson(JSONObject json) throws JSONException {
        List<CartItem> items = new ArrayList<>();
        JSONArray itemArray = json.optJSONArray("items");
        if (itemArray != null) {
            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject itemJson = itemArray.optJSONObject(i);
                if (itemJson == null) {
                    continue;
                }
                CartItem item = CartItem.fromJson(itemJson);
                if (item != null) {
                    items.add(item);
                }
            }
        }

        return new Order(
                json.optString("id"),
                json.optLong("createdAt", System.currentTimeMillis()),
                items,
                json.optInt("subtotal"),
                json.optInt("deliveryFee"),
                json.optInt("discount"),
                json.optInt("total"),
                json.optString("address"),
                json.optString("paymentMethod"),
                json.optInt("status", STATUS_CONFIRMED)
        );
    }
}
