package com.example.bitebuzz.model;

import androidx.annotation.DrawableRes;

/**
 * A single dish on the BiteBuzz menu.
 */
public class FoodItem {

    public final String id;
    public final String name;
    public final String description;
    public final int price;
    @DrawableRes
    public final int imageRes;
    public final String restaurantId;
    public final String category;
    public final boolean vegetarian;
    public final float rating;
    public final int reviewCount;
    public final int prepMinutes;

    public FoodItem(String id,
                    String name,
                    String description,
                    int price,
                    @DrawableRes int imageRes,
                    String restaurantId,
                    String category,
                    boolean vegetarian,
                    float rating,
                    int reviewCount,
                    int prepMinutes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageRes = imageRes;
        this.restaurantId = restaurantId;
        this.category = category;
        this.vegetarian = vegetarian;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.prepMinutes = prepMinutes;
    }
}
