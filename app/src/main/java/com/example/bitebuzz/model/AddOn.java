package com.example.bitebuzz.model;

/**
 * An optional extra that can be added to a cart line.
 */
public class AddOn {

    public final String id;
    public final String name;
    public final int price;

    public AddOn(String id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
