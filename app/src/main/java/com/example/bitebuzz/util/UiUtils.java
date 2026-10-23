package com.example.bitebuzz.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.bitebuzz.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Shared UI helpers for insets, snackbars and small formatting tasks.
 */
public final class UiUtils {

    private UiUtils() {
    }

    /**
     * Adds status/navigation bar padding to a screen root. Existing padding is kept.
     */
    public static void applySystemInsets(final View root) {
        final int startLeft = root.getPaddingLeft();
        final int startTop = root.getPaddingTop();
        final int startRight = root.getPaddingRight();
        final int startBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(
                    startLeft + bars.left,
                    startTop + bars.top,
                    startRight + bars.right,
                    startBottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    /**
     * The main screen keeps its bottom navigation flush with the gesture bar,
     * while the fragment area is padded below the status bar.
     */
    public static void applyMainInsets(final View root, final View content, final View bottomNavigation) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            content.setPadding(bars.left, bars.top, bars.right, 0);
            bottomNavigation.setPadding(bars.left, 0, bars.right, bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    public static void setLightSystemBars(Activity activity, boolean lightIcons) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                activity.getWindow(), activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(lightIcons);
        controller.setAppearanceLightNavigationBars(lightIcons);
    }

    public static void showSnackbar(View anchor, CharSequence message) {
        Snackbar snackbar = Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT);
        styleSnackbar(snackbar);
        anchorAboveBottomNav(snackbar, anchor);
        snackbar.show();
    }

    /**
     * Shows a snackbar above a fixed bottom bar. The root must be a
     * CoordinatorLayout for the anchor to be positioned correctly.
     */
    public static void showSnackbar(View root, View anchorAbove, CharSequence message) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_SHORT);
        styleSnackbar(snackbar);
        if (anchorAbove != null) {
            snackbar.setAnchorView(anchorAbove);
        }
        snackbar.show();
    }

    public static void showSnackbarWithAction(View anchor,
                                              CharSequence message,
                                              CharSequence actionText,
                                              View.OnClickListener action) {
        Snackbar snackbar = Snackbar.make(anchor, message, Snackbar.LENGTH_LONG);
        styleSnackbar(snackbar);
        snackbar.setAction(actionText, action);
        anchorAboveBottomNav(snackbar, anchor);
        snackbar.show();
    }

    /**
     * Shows an action snackbar above a fixed bottom bar.
     */
    public static void showSnackbarWithAction(View root,
                                              View anchorAbove,
                                              CharSequence message,
                                              CharSequence actionText,
                                              View.OnClickListener action) {
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        styleSnackbar(snackbar);
        snackbar.setAction(actionText, action);
        if (anchorAbove != null) {
            snackbar.setAnchorView(anchorAbove);
        }
        snackbar.show();
    }

    private static void anchorAboveBottomNav(Snackbar snackbar, View anchor) {
        View bottomNav = anchor.getRootView().findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            snackbar.setAnchorView(bottomNav);
        }
    }

    private static void styleSnackbar(Snackbar snackbar) {
        Context context = snackbar.getContext();
        snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.secondary));
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.white));
        snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.accent));
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager manager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static String formatOrderDate(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("d MMM, h:mm a", Locale.getDefault());
        return format.format(new Date(timestamp));
    }

    public static String initialsOf(String name) {
        String cleaned = name == null ? "" : name.trim();
        if (cleaned.isEmpty()) {
            return "BB";
        }
        String[] words = cleaned.split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase(Locale.ROOT);
        }
        String first = words[0].substring(0, 1);
        String second = words[words.length - 1].substring(0, 1);
        return (first + second).toUpperCase(Locale.ROOT);
    }

    public static void setTextOrGone(TextView view, CharSequence text) {
        if (text == null || text.length() == 0) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(text);
            view.setVisibility(View.VISIBLE);
        }
    }
}
