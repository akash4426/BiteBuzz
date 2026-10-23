package com.example.bitebuzz.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Small SharedPreferences wrapper for the BiteBuzz demo.
 */
public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "bitebuzz_prefs";
    private static final String KEY_NAME = "profile_name";
    private static final String KEY_ADDRESS = "profile_address";
    private static final String KEY_FAVORITES = "favorite_food_ids";
    private static final String KEY_CART = "cart_json";
    private static final String KEY_ORDERS = "orders_json";
    private static final String KEY_COUPON = "applied_coupon";

    public static final String DEFAULT_NAME = "Aarav Sharma";
    public static final String DEFAULT_ADDRESS =
            "221B, Maple Residency, Indiranagar, Bengaluru 560038";

    private final SharedPreferences preferences;

    public SharedPreferencesHelper(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getName() {
        return preferences.getString(KEY_NAME, DEFAULT_NAME);
    }

    public void setName(String name) {
        preferences.edit().putString(KEY_NAME, name).apply();
    }

    public String getAddress() {
        return preferences.getString(KEY_ADDRESS, DEFAULT_ADDRESS);
    }

    public void setAddress(String address) {
        preferences.edit().putString(KEY_ADDRESS, address).apply();
    }

    public Set<String> getFavoriteIds() {
        Set<String> stored = preferences.getStringSet(KEY_FAVORITES, null);
        return stored == null ? new HashSet<>() : new HashSet<>(stored);
    }

    public void setFavoriteIds(Set<String> ids) {
        preferences.edit().putStringSet(KEY_FAVORITES, new HashSet<>(ids)).apply();
    }

    public String getCartJson() {
        return preferences.getString(KEY_CART, "");
    }

    public void setCartJson(String json) {
        preferences.edit().putString(KEY_CART, json).apply();
    }

    public String getOrdersJson() {
        return preferences.getString(KEY_ORDERS, "");
    }

    public void setOrdersJson(String json) {
        preferences.edit().putString(KEY_ORDERS, json).apply();
    }

    public String getCoupon() {
        return preferences.getString(KEY_COUPON, "");
    }

    public void setCoupon(String coupon) {
        preferences.edit().putString(KEY_COUPON, coupon == null ? "" : coupon).apply();
    }
}
