package com.example.bitebuzz.data;

import android.content.Context;
import android.util.Log;

import com.example.bitebuzz.model.AddOn;
import com.example.bitebuzz.model.CartItem;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Order;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Single source of truth for cart, favourites, orders and profile details.
 * Everything is stored locally, so the app keeps working between launches.
 */
public class AppRepository {

    private static final String TAG = "AppRepository";
    public static final int DELIVERY_FEE = 40;
    public static final int FREE_DELIVERY_THRESHOLD = 499;
    public static final String COUPON_BUZZ10 = "BUZZ10";
    public static final String COUPON_FIRSTBITE = "FIRSTBITE";

    private static AppRepository instance;

    private final SharedPreferencesHelper preferences;
    private final List<CartItem> cartItems = new ArrayList<>();
    private final Set<String> favoriteIds = new HashSet<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<DataListener> listeners = new ArrayList<>();
    private final Random random = new Random();

    private String couponCode = "";

    private AppRepository(Context context) {
        preferences = new SharedPreferencesHelper(context);
        favoriteIds.addAll(preferences.getFavoriteIds());
        couponCode = preferences.getCoupon();
        loadCart();
        loadOrders();
    }

    public static synchronized AppRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AppRepository(context.getApplicationContext());
        }
        return instance;
    }

    public interface DataListener {
        void onDataChanged();
    }

    public void addListener(DataListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DataListener listener) {
        listeners.remove(listener);
    }

    private void notifyChanged() {
        List<DataListener> copy = new ArrayList<>(listeners);
        for (DataListener listener : copy) {
            listener.onDataChanged();
        }
    }

    // ------------------------------------------------------------------
    // Profile
    // ------------------------------------------------------------------

    public String getName() {
        return preferences.getName();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        preferences.setName(name.trim());
        notifyChanged();
    }

    public String getAddress() {
        return preferences.getAddress();
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return;
        }
        preferences.setAddress(address.trim());
        notifyChanged();
    }

    public String getFirstName() {
        String name = getName().trim();
        int space = name.indexOf(' ');
        return space > 0 ? name.substring(0, space) : name;
    }

    // ------------------------------------------------------------------
    // Favourites
    // ------------------------------------------------------------------

    public boolean isFavorite(String foodId) {
        return favoriteIds.contains(foodId);
    }

    public boolean toggleFavorite(String foodId) {
        boolean nowFavorite;
        if (favoriteIds.contains(foodId)) {
            favoriteIds.remove(foodId);
            nowFavorite = false;
        } else {
            favoriteIds.add(foodId);
            nowFavorite = true;
        }
        preferences.setFavoriteIds(favoriteIds);
        notifyChanged();
        return nowFavorite;
    }

    public List<FoodItem> getFavoriteFoods() {
        List<FoodItem> favorites = new ArrayList<>();
        for (FoodItem food : FoodData.getFoods()) {
            if (favoriteIds.contains(food.id)) {
                favorites.add(food);
            }
        }
        return favorites;
    }

    public int getFavoriteCount() {
        int count = 0;
        for (FoodItem food : FoodData.getFoods()) {
            if (favoriteIds.contains(food.id)) {
                count++;
            }
        }
        return count;
    }

    // ------------------------------------------------------------------
    // Cart
    // ------------------------------------------------------------------

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public boolean isCartEmpty() {
        return cartItems.isEmpty();
    }

    public int getCartCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.quantity;
        }
        return count;
    }

    public void addToCart(FoodItem food, int quantity) {
        addToCart(food, quantity, new ArrayList<>(), "");
    }

    public void addToCart(FoodItem food, int quantity, List<AddOn> addOns, String instructions) {
        if (food == null) {
            return;
        }
        List<AddOn> safeAddOns = addOns == null ? new ArrayList<>() : new ArrayList<>(addOns);
        String safeInstructions = instructions == null ? "" : instructions.trim();
        CartItem existing = findMatchingLine(food, safeAddOns, safeInstructions);
        if (existing != null) {
            existing.quantity += Math.max(1, quantity);
        } else {
            cartItems.add(new CartItem(food, Math.max(1, quantity), safeAddOns, safeInstructions));
        }
        persistCart();
        notifyChanged();
    }

    public void incrementCartItem(CartItem item) {
        CartItem line = findLine(item);
        if (line == null) {
            return;
        }
        line.quantity++;
        persistCart();
        notifyChanged();
    }

    public void decrementCartItem(CartItem item) {
        CartItem line = findLine(item);
        if (line == null) {
            return;
        }
        if (line.quantity > 1) {
            line.quantity--;
        } else {
            cartItems.remove(line);
        }
        persistCart();
        notifyChanged();
    }

    public void removeCartItem(CartItem item) {
        CartItem line = findLine(item);
        if (line != null) {
            cartItems.remove(line);
            persistCart();
            notifyChanged();
        }
    }

    public void clearCart() {
        cartItems.clear();
        couponCode = "";
        preferences.setCoupon("");
        persistCart();
        notifyChanged();
    }

    public void addOrderItemsToCart(Order order) {
        if (order == null) {
            return;
        }
        for (CartItem orderItem : order.items) {
            CartItem existing = findMatchingLine(
                    orderItem.food,
                    orderItem.addOns,
                    orderItem.instructions);
            if (existing != null) {
                existing.quantity += orderItem.quantity;
            } else {
                cartItems.add(orderItem.copy());
            }
        }
        persistCart();
        notifyChanged();
    }

    private CartItem findMatchingLine(FoodItem food, List<AddOn> addOns, String instructions) {
        for (CartItem item : cartItems) {
            if (item.matches(food, addOns, instructions)) {
                return item;
            }
        }
        return null;
    }

    private CartItem findLine(CartItem item) {
        if (item == null) {
            return null;
        }
        for (CartItem line : cartItems) {
            if (line == item) {
                return line;
            }
        }
        return findMatchingLine(item.food, item.addOns, item.instructions);
    }

    private void persistCart() {
        JSONArray array = new JSONArray();
        try {
            for (CartItem item : cartItems) {
                array.put(item.toJson());
            }
            preferences.setCartJson(array.toString());
        } catch (JSONException exception) {
            Log.e(TAG, "Could not save cart", exception);
        }
        if (cartItems.isEmpty()) {
            couponCode = "";
            preferences.setCoupon("");
        }
    }

    private void loadCart() {
        String json = preferences.getCartJson();
        if (json == null || json.isEmpty()) {
            return;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                CartItem item = CartItem.fromJson(object);
                if (item != null) {
                    cartItems.add(item);
                }
            }
        } catch (JSONException exception) {
            Log.e(TAG, "Could not load cart", exception);
        }
    }

    // ------------------------------------------------------------------
    // Coupons and totals
    // ------------------------------------------------------------------

    public String getCouponCode() {
        return couponCode;
    }

    public boolean isValidCoupon(String code) {
        return COUPON_BUZZ10.equals(code) || COUPON_FIRSTBITE.equals(code);
    }

    public boolean applyCoupon(String code) {
        String cleaned = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (!isValidCoupon(cleaned)) {
            return false;
        }
        couponCode = cleaned;
        preferences.setCoupon(cleaned);
        notifyChanged();
        return true;
    }

    public void removeCoupon() {
        couponCode = "";
        preferences.setCoupon("");
        notifyChanged();
    }

    public PriceSummary calculateTotals() {
        PriceSummary summary = new PriceSummary();
        summary.couponCode = couponCode;

        for (CartItem item : cartItems) {
            summary.subtotal += item.getLineTotal();
        }

        if (summary.subtotal > 0) {
            summary.deliveryFee = summary.subtotal >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;
        }

        if (COUPON_BUZZ10.equals(couponCode)) {
            summary.discount = Math.round(summary.subtotal * 0.10f);
        } else if (COUPON_FIRSTBITE.equals(couponCode)) {
            summary.discount = 30;
        }

        summary.discount = Math.min(summary.discount, summary.subtotal);
        summary.discount = Math.max(summary.discount, 0);
        summary.total = Math.max(0, summary.subtotal - summary.discount + summary.deliveryFee);
        return summary;
    }

    public static class PriceSummary {
        public int subtotal;
        public int deliveryFee;
        public int discount;
        public int total;
        public String couponCode = "";

        public boolean hasDiscount() {
            return discount > 0;
        }

        public boolean isFreeDelivery() {
            return subtotal > 0 && deliveryFee == 0;
        }

        public int getAmountForFreeDelivery() {
            return Math.max(0, FREE_DELIVERY_THRESHOLD - subtotal);
        }
    }

    // ------------------------------------------------------------------
    // Orders
    // ------------------------------------------------------------------

    public Order placeOrder(String address, String paymentMethod) {
        if (cartItems.isEmpty()) {
            return null;
        }

        PriceSummary summary = calculateTotals();
        String orderId = generateOrderId();
        Order order = new Order(
                orderId,
                System.currentTimeMillis(),
                cartItems,
                summary.subtotal,
                summary.deliveryFee,
                summary.discount,
                summary.total,
                address,
                paymentMethod,
                Order.STATUS_CONFIRMED);
        orders.add(0, order);
        cartItems.clear();
        couponCode = "";
        preferences.setCoupon("");
        persistCart();
        persistOrders();
        notifyChanged();
        return order;
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public Order getOrderById(String orderId) {
        if (orderId == null) {
            return null;
        }
        for (Order order : orders) {
            if (order.id.equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    public void updateOrderStatus(String orderId, int newStatus) {
        Order order = getOrderById(orderId);
        if (order == null) {
            return;
        }
        order.status = Math.max(Order.STATUS_CONFIRMED, Math.min(Order.STATUS_DELIVERED, newStatus));
        persistOrders();
        notifyChanged();
    }

    private String generateOrderId() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String candidate = "BB-" + (100000 + random.nextInt(900000));
            if (getOrderById(candidate) == null) {
                return candidate;
            }
        }
        return "BB-" + System.currentTimeMillis();
    }

    private void persistOrders() {
        JSONArray array = new JSONArray();
        try {
            for (Order order : orders) {
                array.put(order.toJson());
            }
            preferences.setOrdersJson(array.toString());
        } catch (JSONException exception) {
            Log.e(TAG, "Could not save orders", exception);
        }
    }

    private void loadOrders() {
        String json = preferences.getOrdersJson();
        if (json == null || json.isEmpty()) {
            return;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                orders.add(Order.fromJson(object));
            }
        } catch (JSONException exception) {
            Log.e(TAG, "Could not load orders", exception);
        }
    }
}
