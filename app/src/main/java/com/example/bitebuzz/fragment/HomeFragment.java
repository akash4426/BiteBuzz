package com.example.bitebuzz.fragment;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitebuzz.BuzzModeActivity;
import com.example.bitebuzz.FoodDetailsActivity;
import com.example.bitebuzz.MainActivity;
import com.example.bitebuzz.ProfileActivity;
import com.example.bitebuzz.R;
import com.example.bitebuzz.RestaurantActivity;
import com.example.bitebuzz.adapter.CategoryAdapter;
import com.example.bitebuzz.adapter.FoodListAdapter;
import com.example.bitebuzz.adapter.RestaurantAdapter;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.FragmentHomeBinding;
import com.example.bitebuzz.model.Category;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Mood;
import com.example.bitebuzz.model.Restaurant;
import com.example.bitebuzz.util.DialogUtils;
import com.example.bitebuzz.util.UiUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * The main landing screen with search, categories, Buzz Mode shortcuts,
 * popular restaurants and quick bites.
 */
public class HomeFragment extends Fragment implements AppRepository.DataListener {

    private static final String STATE_CATEGORY = "state_category";
    private static final String STATE_QUERY = "state_query";

    private FragmentHomeBinding binding;
    private AppRepository repository;
    private CategoryAdapter categoryAdapter;
    private FoodListAdapter searchAdapter;
    private FoodListAdapter quickBitesAdapter;
    private RestaurantAdapter restaurantAdapter;
    private String selectedCategory = "All";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());

        setupCategories();
        setupSearch();
        setupRestaurants();
        setupQuickBites();
        setupActions();
        populateMoodShortcuts();

        if (savedInstanceState != null) {
            selectedCategory = savedInstanceState.getString(STATE_CATEGORY, "All");
            currentQuery = savedInstanceState.getString(STATE_QUERY, "");
            categoryAdapter.setSelectedPosition(categoryPosition(selectedCategory));
            if (!currentQuery.isEmpty()) {
                binding.searchInput.setText(currentQuery);
                binding.searchInput.setSelection(currentQuery.length());
            }
        }

        renderHeader();
        renderQuickBites();
    }

    private int categoryPosition(String name) {
        List<Category> categories = FoodData.getCategories();
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).name.equalsIgnoreCase(name)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_CATEGORY, selectedCategory);
        outState.putString(STATE_QUERY, currentQuery);
        super.onSaveInstanceState(outState);
    }

    private void setupCategories() {
        categoryAdapter = new CategoryAdapter(FoodData.getCategories(), (category, position) -> {
            selectedCategory = category.name;
            categoryAdapter.setSelectedPosition(position);
            renderQuickBites();
        });
        binding.categoryRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRecycler.setAdapter(categoryAdapter);
    }

    private void setupSearch() {
        searchAdapter = new FoodListAdapter(requireContext(), new ArrayList<>(), this::openFood);
        binding.searchResultsRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.searchResultsRecycler.setAdapter(searchAdapter);

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                applySearch(text == null ? "" : text.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        binding.searchInput.setOnEditorActionListener((textView, actionId, event) -> {
            UiUtils.hideKeyboard(requireContext(), binding.searchInput);
            return true;
        });
        binding.clearSearchButton.setOnClickListener(v -> binding.searchInput.setText(""));
    }

    private void setupRestaurants() {
        restaurantAdapter = new RestaurantAdapter(FoodData.getRestaurants(), this::openRestaurant);
        binding.popularRecycler.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.popularRecycler.setAdapter(restaurantAdapter);
    }

    private void setupQuickBites() {
        quickBitesAdapter = new FoodListAdapter(requireContext(), new ArrayList<>(), this::openFood);
        binding.quickBitesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.quickBitesRecycler.setAdapter(quickBitesAdapter);
    }

    private void setupActions() {
        binding.profileButton.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), ProfileActivity.class)));

        binding.addressRow.setOnClickListener(v -> DialogUtils.showInputDialog(
                requireActivity(),
                R.string.address_dialog_title,
                R.string.address_hint,
                repository.getAddress(),
                R.string.address_empty_error,
                value -> {
                    repository.setAddress(value);
                    UiUtils.showSnackbar(binding.getRoot(),
                            getString(R.string.address_updated));
                }));

        binding.promoBanner.setOnClickListener(v -> openTab(MainActivity.TAB_EXPLORE));
        binding.viewAllFoodsButton.setOnClickListener(v -> openTab(MainActivity.TAB_EXPLORE));
        binding.allRestaurantsButton.setOnClickListener(v -> showRestaurantPicker());
        binding.buzzFindButton.setOnClickListener(v -> openBuzz(null, false));
        binding.buzzSurpriseButton.setOnClickListener(v -> openBuzz(null, true));
        binding.buzzCard.setOnClickListener(v -> openBuzz(null, false));
    }

    private void populateMoodShortcuts() {
        binding.homeMoodRow.removeAllViews();
        for (Mood mood : FoodData.getMoods()) {
            TextView chip = new TextView(requireContext());
            chip.setText(getString(R.string.mood_chip_label, mood.emoji, mood.shortLabel));
            chip.setTextSize(10.5f);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
            chip.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            chip.setGravity(android.view.Gravity.CENTER);
            int horizontal = dp(11);
            int vertical = dp(7);
            chip.setPadding(horizontal, vertical, horizontal, vertical);

            GradientDrawable background = new GradientDrawable();
            background.setColor(ContextCompat.getColor(requireContext(), R.color.card_alt));
            background.setCornerRadius(dp(50));
            background.setStroke(dp(1), ContextCompat.getColor(requireContext(), R.color.stroke));
            chip.setBackground(background);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(dp(7));
            chip.setLayoutParams(params);
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v -> openBuzz(mood.id, false));
            binding.homeMoodRow.addView(chip);
        }
    }

    private void renderHeader() {
        binding.greetingText.setText(getString(R.string.home_greeting, repository.getFirstName()));
        binding.addressText.setText(repository.getAddress());
    }

    private void applySearch(String rawQuery) {
        currentQuery = rawQuery == null ? "" : rawQuery.trim();
        boolean searching = !currentQuery.isEmpty();
        binding.clearSearchButton.setVisibility(searching ? View.VISIBLE : View.GONE);
        binding.searchResultsSection.setVisibility(searching ? View.VISIBLE : View.GONE);
        binding.homeMainContent.setVisibility(searching ? View.GONE : View.VISIBLE);
        if (!searching) {
            UiUtils.hideKeyboard(requireContext(), binding.searchInput);
            return;
        }

        List<FoodItem> results = FoodData.searchFoods(currentQuery);
        searchAdapter.setFoods(results);
        binding.searchResultsTitle.setText(
                getString(R.string.search_results_for, currentQuery));
        binding.searchResultsCount.setText(getResources().getQuantityString(
                R.plurals.search_results_count, results.size(), results.size()));
        binding.searchResultsRecycler.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
        binding.searchEmptyText.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void renderQuickBites() {
        List<FoodItem> foods = FoodData.getFoodsByCategory(selectedCategory);
        if ("All".equalsIgnoreCase(selectedCategory) && foods.size() > 6) {
            foods = new ArrayList<>(foods.subList(0, 6));
        }
        quickBitesAdapter.setFoods(foods);

        if ("All".equalsIgnoreCase(selectedCategory)) {
            binding.quickBitesTitle.setText(R.string.quick_bites_title);
            binding.quickBitesSubtitle.setText(R.string.quick_bites_subtitle);
        } else {
            binding.quickBitesTitle.setText(R.string.quick_bites_title);
            binding.quickBitesSubtitle.setText(
                    getString(R.string.quick_bites_filtered, selectedCategory));
        }

        boolean empty = foods.isEmpty();
        binding.quickBitesRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.quickBitesEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showRestaurantPicker() {
        List<Restaurant> restaurants = FoodData.getRestaurants();
        String[] names = new String[restaurants.size()];
        for (int i = 0; i < restaurants.size(); i++) {
            names[i] = restaurants.get(i).name + "  ·  " + restaurants.get(i).cuisine;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restaurant_picker_title)
                .setItems(names, (dialog, which) -> openRestaurant(restaurants.get(which)))
                .setNegativeButton(R.string.close, null)
                .show();
    }

    private void openFood(FoodItem food) {
        startActivity(FoodDetailsActivity.createIntent(requireContext(), food.id));
    }

    private void openRestaurant(Restaurant restaurant) {
        startActivity(RestaurantActivity.createIntent(requireContext(), restaurant.id));
    }

    private void openBuzz(String moodId, boolean surprise) {
        Intent intent;
        if (surprise) {
            intent = BuzzModeActivity.surpriseIntent(requireContext());
        } else if (moodId != null) {
            intent = BuzzModeActivity.createIntent(requireContext(), moodId);
        } else {
            intent = new Intent(requireContext(), BuzzModeActivity.class);
        }
        startActivity(intent);
    }

    private void openTab(int tab) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).selectTab(tab);
        } else {
            startActivity(MainActivity.createIntent(requireContext(), tab));
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (repository != null) {
            repository.addListener(this);
        }
        if (binding != null) {
            renderHeader();
            quickBitesAdapter.notifyDataSetChanged();
            searchAdapter.notifyDataSetChanged();
            if (!currentQuery.isEmpty()) {
                applySearch(currentQuery);
            }
        }
    }

    @Override
    public void onStop() {
        if (repository != null) {
            repository.removeListener(this);
        }
        super.onStop();
    }

    @Override
    public void onDataChanged() {
        if (binding == null) {
            return;
        }
        renderHeader();
        quickBitesAdapter.notifyDataSetChanged();
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
