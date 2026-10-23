package com.example.bitebuzz.model;

import com.example.bitebuzz.data.FoodData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One line in the cart: a dish, a quantity and any chosen extras.
 */
public class CartItem {

    public final FoodItem food;
    public int quantity;
    public final List<AddOn> addOns;
    public final String instructions;

    public CartItem(FoodItem food, int quantity, List<AddOn> addOns, String instructions) {
        this.food = food;
        this.quantity = Math.max(1, quantity);
        this.addOns = addOns == null ? new ArrayList<>() : new ArrayList<>(addOns);
        this.instructions = instructions == null ? "" : instructions.trim();
    }

    public int getUnitPrice() {
        int total = food.price;
        for (AddOn addOn : addOns) {
            total += addOn.price;
        }
        return total;
    }

    public int getLineTotal() {
        return getUnitPrice() * quantity;
    }

    public String getExtrasLabel() {
        List<String> parts = new ArrayList<>();
        for (AddOn addOn : addOns) {
            parts.add(addOn.name);
        }
        if (!instructions.isEmpty()) {
            parts.add(instructions);
        }
        return join(parts, " · ");
    }

    public boolean matches(FoodItem otherFood, List<AddOn> otherAddOns, String otherInstructions) {
        if (!food.id.equals(otherFood.id)) {
            return false;
        }
        if (!normalize(instructions).equals(normalize(otherInstructions))) {
            return false;
        }
        if (addOns.size() != otherAddOns.size()) {
            return false;
        }
        for (int i = 0; i < addOns.size(); i++) {
            if (!addOns.get(i).id.equals(otherAddOns.get(i).id)) {
                return false;
            }
        }
        return true;
    }

    public CartItem copy() {
        return new CartItem(food, quantity, addOns, instructions);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("foodId", food.id);
        json.put("quantity", quantity);
        json.put("instructions", instructions);

        JSONArray addOnArray = new JSONArray();
        for (AddOn addOn : addOns) {
            addOnArray.put(addOn.id);
        }
        json.put("addOns", addOnArray);
        return json;
    }

    public static CartItem fromJson(JSONObject json) throws JSONException {
        FoodItem food = FoodData.getFoodById(json.optString("foodId"));
        if (food == null) {
            return null;
        }

        List<AddOn> addOns = new ArrayList<>();
        JSONArray addOnArray = json.optJSONArray("addOns");
        if (addOnArray != null) {
            for (int i = 0; i < addOnArray.length(); i++) {
                AddOn addOn = FoodData.getAddOnById(addOnArray.optString(i));
                if (addOn != null) {
                    addOns.add(addOn);
                }
            }
        }

        return new CartItem(
                food,
                Math.max(1, json.optInt("quantity", 1)),
                addOns,
                json.optString("instructions", "")
        );
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String join(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(values.get(i));
        }
        return builder.toString();
    }
}
