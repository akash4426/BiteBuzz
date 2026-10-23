package com.example.bitebuzz.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import java.util.List;

/**
 * One of the six food moods used by Buzz Mode.
 */
public class Mood {

    public final String id;
    public final String emoji;
    @StringRes
    public final int titleRes;
    @StringRes
    public final int subtitleRes;
    @ColorRes
    public final int colorRes;
    public final String shortLabel;
    public final List<String> foodIds;

    public Mood(String id,
                String emoji,
                @StringRes int titleRes,
                @StringRes int subtitleRes,
                @ColorRes int colorRes,
                String shortLabel,
                List<String> foodIds) {
        this.id = id;
        this.emoji = emoji;
        this.titleRes = titleRes;
        this.subtitleRes = subtitleRes;
        this.colorRes = colorRes;
        this.shortLabel = shortLabel;
        this.foodIds = foodIds;
    }
}
