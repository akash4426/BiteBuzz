package com.example.bitebuzz.model;

/**
 * A food category chip such as Pizza, Biryani or Drinks.
 */
public class Category {

    public final String name;
    public final String emoji;

    public Category(String name, String emoji) {
        this.name = name;
        this.emoji = emoji;
    }
}
