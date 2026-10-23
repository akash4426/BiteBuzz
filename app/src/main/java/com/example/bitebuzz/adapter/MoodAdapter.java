package com.example.bitebuzz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bitebuzz.R;
import com.example.bitebuzz.databinding.ItemMoodBinding;
import com.example.bitebuzz.model.Mood;

import java.util.ArrayList;
import java.util.List;

/**
 * The six colourful mood cards at the heart of Buzz Mode.
 */
public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {

    public interface OnMoodClickListener {
        void onMoodClick(Mood mood);
    }

    private final List<Mood> moods = new ArrayList<>();
    private final OnMoodClickListener listener;
    private String selectedMoodId;

    public MoodAdapter(List<Mood> moods, OnMoodClickListener listener) {
        this.moods.addAll(moods);
        this.listener = listener;
    }

    public void setSelectedMoodId(String moodId) {
        selectedMoodId = moodId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMoodBinding binding = ItemMoodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Mood mood = moods.get(position);
        Context context = holder.binding.getRoot().getContext();
        boolean selected = mood.id.equals(selectedMoodId);

        holder.binding.moodCard.setCardBackgroundColor(ContextCompat.getColor(context, mood.colorRes));
        holder.binding.moodCard.setStrokeColor(ContextCompat.getColor(
                context, selected ? R.color.primary : R.color.transparent));
        holder.binding.moodCard.setStrokeWidth(selected ? dp(context, 2) : 0);
        holder.binding.moodEmoji.setText(mood.emoji);
        holder.binding.moodTitle.setText(mood.titleRes);
        holder.binding.moodSubtitle.setText(mood.subtitleRes);

        holder.binding.getRoot().setOnClickListener(view -> {
            if (listener != null) {
                listener.onMoodClick(mood);
            }
        });
    }

    @Override
    public int getItemCount() {
        return moods.size();
    }

    private int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        final ItemMoodBinding binding;

        MoodViewHolder(ItemMoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
