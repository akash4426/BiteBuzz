package com.example.bitebuzz.model;

import androidx.annotation.DrawableRes;

/**
 * A local restaurant shown on the home screen and its own menu screen.
 */
public class Restaurant {

    public final String id;
    public final String name;
    public final String cuisine;
    @DrawableRes
    public final int imageRes;
    public final float rating;
    public final int deliveryMin;
    public final int deliveryMax;
    public final int priceForTwo;
    public final String offer;

    public Restaurant(String id,
                      String name,
                      String cuisine,
                      @DrawableRes int imageRes,
                      float rating,
                      int deliveryMin,
                      int deliveryMax,
                      int priceForTwo,
                      String offer) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.imageRes = imageRes;
        this.rating = rating;
        this.deliveryMin = deliveryMin;
        this.deliveryMax = deliveryMax;
        this.priceForTwo = priceForTwo;
        this.offer = offer;
    }

    public String getDeliveryTime() {
        return deliveryMin + "–" + deliveryMax + " min";
    }
}
