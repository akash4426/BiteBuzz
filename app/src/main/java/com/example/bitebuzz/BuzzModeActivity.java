package com.example.bitebuzz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitebuzz.adapter.FoodMiniAdapter;
import com.example.bitebuzz.adapter.MoodAdapter;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.ActivityBuzzModeBinding;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Mood;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Buzz Mode: pick a food mood and get three matching local recommendations.
 */
public class BuzzModeActivity extends AppCompatActivity implements AppRepository.DataListener {

    private static final String EXTRA_MOOD_ID = "extra_mood_id";
    private static final String EXTRA_SURPRISE = "extra_surprise";
    private static final String STATE_MOOD_ID = "state_mood_id";
    private static final String STATE_SURPRISE = "state_surprise";
    private static final String STATE_RECOMMENDATION_IDS = "state_recommendation_ids";
    private static final String STATE_RESULTS_VISIBLE = "state_results_visible";
    private static final String STATE_LOADING = "state_loading";
    private static final String STATE_RESULT_TITLE = "state_result_title";
    private static final String STATE_RESULT_SUBTITLE = "state_result_subtitle";
    private static final String STATE_HINT = "state_hint";

    private ActivityBuzzModeBinding binding;
    private AppRepository repository;
    private MoodAdapter moodAdapter;
    private FoodMiniAdapter recommendationsAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingRecommendation;
    private Mood selectedMood;
    private boolean surpriseActive;
    private List<FoodItem> currentPool = new ArrayList<>();
    private List<FoodItem> currentRecommendations = new ArrayList<>();
    private TextView cartBadge;

    public static Intent createIntent(Context context, String moodId) {
        Intent intent = new Intent(context, BuzzModeActivity.class);
        intent.putExtra(EXTRA_MOOD_ID, moodId);
        return intent;
    }

    public static Intent surpriseIntent(Context context) {
        Intent intent = new Intent(context, BuzzModeActivity.class);
        intent.putExtra(EXTRA_SURPRISE, true);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityBuzzModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.applySystemInsets(binding.buzzRoot);

        repository = AppRepository.getInstance(this);
        cartBadge = binding.getRoot().findViewById(R.id.cartCountBadge);

        setupMoodGrid();
        setupRecommendations();
        setupActions();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            launchFromIntent();
        }
    }

    private void launchFromIntent() {
        boolean surpriseMode = getIntent().getBooleanExtra(EXTRA_SURPRISE, false);
        String moodId = getIntent().getStringExtra(EXTRA_MOOD_ID);
        Mood mood = FoodData.getMoodById(moodId);
        if (surpriseMode) {
            binding.buzzScroll.post(this::surpriseMe);
        } else if (mood != null) {
            binding.buzzScroll.post(() -> selectMood(mood));
        }
    }

    private void restoreState(Bundle state) {
        surpriseActive = state.getBoolean(STATE_SURPRISE, false);
        selectedMood = FoodData.getMoodById(state.getString(STATE_MOOD_ID));
        moodAdapter.setSelectedMoodId(selectedMood == null ? null : selectedMood.id);

        if (surpriseActive) {
            currentPool = FoodData.getFoods();
        } else if (selectedMood != null) {
            currentPool = FoodData.getFoodsForMood(selectedMood);
        }

        List<FoodItem> restored = new ArrayList<>();
        ArrayList<String> recommendationIds =
                state.getStringArrayList(STATE_RECOMMENDATION_IDS);
        if (recommendationIds != null) {
            for (String id : recommendationIds) {
                FoodItem food = FoodData.getFoodById(id);
                if (food != null) {
                    restored.add(food);
                }
            }
        }
        currentRecommendations = restored;

        if (!state.getBoolean(STATE_RESULTS_VISIBLE, false)) {
            binding.buzzHint.setText(state.getString(
                    STATE_HINT, getString(R.string.buzz_pick_mood)));
            return;
        }

        binding.buzzResultSection.setVisibility(View.VISIBLE);
        binding.buzzHint.setText(state.getString(
                STATE_HINT, getString(R.string.buzz_selected_hint)));
        binding.recommendationsTitle.setText(state.getString(
                STATE_RESULT_TITLE, getString(R.string.buzz_recommendations_title)));
        binding.recommendationsSubtitle.setText(state.getString(
                STATE_RESULT_SUBTITLE, getString(R.string.buzz_recommendations_sub)));

        if (state.getBoolean(STATE_LOADING, false)) {
            if (surpriseActive || selectedMood == null) {
                binding.buzzScroll.post(this::surpriseMe);
            } else {
                binding.buzzScroll.post(() -> selectMood(selectedMood));
            }
            return;
        }

        if (!restored.isEmpty()) {
            recommendationsAdapter.setFoods(restored);
            binding.buzzLoading.setVisibility(View.GONE);
            binding.recommendationsRecycler.setAlpha(1f);
            binding.recommendationsRecycler.setTranslationY(0f);
            binding.recommendationsRecycler.setVisibility(View.VISIBLE);
            binding.buzzAgainButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupMoodGrid() {
        moodAdapter = new MoodAdapter(FoodData.getMoods(), this::selectMood);
        binding.moodRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        binding.moodRecycler.setAdapter(moodAdapter);
    }

    private void setupRecommendations() {
        recommendationsAdapter = new FoodMiniAdapter(this, new ArrayList<>(), this::openFood);
        binding.recommendationsRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recommendationsRecycler.setAdapter(recommendationsAdapter);
        binding.buzzAgainButton.setVisibility(View.GONE);
    }

    private void setupActions() {
        binding.buzzBackButton.setOnClickListener(v -> finish());
        binding.surpriseButton.setOnClickListener(v -> surpriseMe());
        binding.buzzAgainButton.setOnClickListener(v -> buzzAgain());

        View cartButton = binding.getRoot().findViewById(R.id.buzzCartButton);
        if (cartButton != null) {
            cartButton.setOnClickListener(v -> startActivity(
                    MainActivity.createIntent(this, MainActivity.TAB_CART)));
        }
    }

    private void selectMood(Mood mood) {
        selectedMood = mood;
        surpriseActive = false;
        moodAdapter.setSelectedMoodId(mood.id);
        binding.buzzHint.setText(R.string.buzz_selected_hint);
        animateBee(1f);

        List<FoodItem> pool = FoodData.getFoodsForMood(mood);
        currentPool = pool;
        beginLoading(() -> finishRecommendation(
                pickThree(pool, currentRecommendations),
                getString(R.string.buzz_recommendations_title),
                getString(R.string.buzz_recommendations_sub),
                getString(R.string.buzz_bee_ready)));
    }

    private void surpriseMe() {
        selectedMood = null;
        surpriseActive = true;
        moodAdapter.setSelectedMoodId(null);
        binding.buzzHint.setText(R.string.buzz_selected_hint);
        animateBee(1.15f);

        currentPool = FoodData.getFoods();
        beginLoading(() -> finishRecommendation(
                pickThree(currentPool, currentRecommendations),
                getString(R.string.surprise_title),
                getString(R.string.surprise_sub),
                getString(R.string.buzz_bee_surprise)));
    }

    private void buzzAgain() {
        if (currentPool == null || currentPool.isEmpty()) {
            surpriseMe();
            return;
        }
        binding.buzzHint.setText(R.string.buzz_selected_hint);
        animateBee(1.1f);
        beginLoading(() -> finishRecommendation(
                pickThree(currentPool, currentRecommendations),
                selectedMood == null
                        ? getString(R.string.surprise_title)
                        : getString(R.string.buzz_recommendations_title),
                selectedMood == null
                        ? getString(R.string.surprise_sub)
                        : getString(R.string.buzz_recommendations_sub),
                getString(R.string.buzz_bee_again)));
    }

    private void beginLoading(Runnable onReady) {
        if (pendingRecommendation != null) {
            handler.removeCallbacks(pendingRecommendation);
        }
        binding.buzzResultSection.setVisibility(View.VISIBLE);
        binding.buzzLoading.setVisibility(View.VISIBLE);
        binding.recommendationsRecycler.setVisibility(View.GONE);
        binding.buzzAgainButton.setVisibility(View.GONE);
        binding.buzzScroll.post(() -> binding.buzzScroll.smoothScrollTo(
                0, binding.buzzResultSection.getTop()));

        pendingRecommendation = () -> {
            binding.buzzLoading.setVisibility(View.GONE);
            onReady.run();
            pendingRecommendation = null;
        };
        handler.postDelayed(pendingRecommendation, 850L);
    }

    private void finishRecommendation(List<FoodItem> foods,
                                      String title,
                                      String subtitle,
                                      String beeMessage) {
        currentRecommendations = new ArrayList<>(foods);
        recommendationsAdapter.setFoods(foods);
        binding.recommendationsTitle.setText(title);
        binding.recommendationsSubtitle.setText(subtitle);
        binding.buzzHint.setText(beeMessage);
        binding.recommendationsRecycler.setAlpha(0f);
        binding.recommendationsRecycler.setTranslationY(18f);
        binding.recommendationsRecycler.setVisibility(View.VISIBLE);
        binding.recommendationsRecycler.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(320)
                .start();
        binding.buzzAgainButton.setVisibility(View.VISIBLE);
        animateBee(1f);
    }

    private List<FoodItem> pickThree(List<FoodItem> pool, List<FoodItem> previous) {
        if (pool == null || pool.isEmpty()) {
            return new ArrayList<>();
        }
        List<FoodItem> result = new ArrayList<>();
        for (int attempt = 0; attempt < 12; attempt++) {
            List<FoodItem> shuffled = new ArrayList<>(pool);
            Collections.shuffle(shuffled);
            result = new ArrayList<>(shuffled.subList(0, Math.min(3, shuffled.size())));
            if (previous == null || !sameFoods(result, previous)) {
                break;
            }
        }
        return result;
    }

    private boolean sameFoods(List<FoodItem> first, List<FoodItem> second) {
        if (first.size() != second.size()) {
            return false;
        }
        Set<String> firstIds = new HashSet<>();
        for (FoodItem food : first) {
            firstIds.add(food.id);
        }
        for (FoodItem food : second) {
            if (!firstIds.contains(food.id)) {
                return false;
            }
        }
        return true;
    }

    private void animateBee(float scale) {
        binding.buzzBee.animate()
                .rotation(-18f)
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(160)
                .withEndAction(() -> binding.buzzBee.animate()
                        .rotation(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(220)
                        .start())
                .start();
    }

    private void openFood(FoodItem food) {
        startActivity(FoodDetailsActivity.createIntent(this, food.id));
    }

    private void updateCartBadge() {
        if (cartBadge == null) {
            return;
        }
        int count = repository.getCartCount();
        cartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        cartBadge.setText(String.valueOf(count));
        cartBadge.setBackgroundResource(R.drawable.bg_badge);
    }

    @Override
    protected void onStart() {
        super.onStart();
        repository.addListener(this);
        updateCartBadge();
    }

    @Override
    protected void onStop() {
        repository.removeListener(this);
        super.onStop();
    }

    @Override
    public void onDataChanged() {
        updateCartBadge();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SURPRISE, surpriseActive);
        outState.putString(STATE_MOOD_ID, selectedMood == null ? null : selectedMood.id);
        outState.putBoolean(STATE_RESULTS_VISIBLE,
                binding.buzzResultSection.getVisibility() == View.VISIBLE);
        outState.putBoolean(STATE_LOADING,
                binding.buzzLoading.getVisibility() == View.VISIBLE);
        outState.putString(STATE_RESULT_TITLE,
                binding.recommendationsTitle.getText().toString());
        outState.putString(STATE_RESULT_SUBTITLE,
                binding.recommendationsSubtitle.getText().toString());
        outState.putString(STATE_HINT, binding.buzzHint.getText().toString());

        ArrayList<String> recommendationIds = new ArrayList<>();
        for (FoodItem food : currentRecommendations) {
            recommendationIds.add(food.id);
        }
        outState.putStringArrayList(STATE_RECOMMENDATION_IDS, recommendationIds);
    }

    @Override
    protected void onDestroy() {
        if (pendingRecommendation != null) {
            handler.removeCallbacks(pendingRecommendation);
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
