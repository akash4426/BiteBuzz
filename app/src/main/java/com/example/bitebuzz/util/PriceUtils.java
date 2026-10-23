package com.example.bitebuzz.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Tiny helpers for showing Indian rupee prices and ratings.
 */
public final class PriceUtils {

    private PriceUtils() {
    }

    public static String format(int amount) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        return "₹" + format.format(Math.max(0, amount));
    }

    public static String formatWithMinus(int amount) {
        return "−" + format(amount);
    }

    public static String formatRating(float rating) {
        return String.format(Locale.US, "%.1f", rating);
    }
}
