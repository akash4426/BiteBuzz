package com.example.bitebuzz.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bitebuzz.FoodDetailsActivity;
import com.example.bitebuzz.R;
import com.example.bitebuzz.adapter.CategoryAdapter;
import com.example.bitebuzz.adapter.FoodGridAdapter;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.FragmentExploreBinding;
import com.example.bitebuzz.model.Category;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.util.UiUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Searchable, filterable catalogue of every dish.
 */
public class ExploreFragment extends Fragment implements AppRepository.DataListener {

    private static final int SORT_RECOMMENDED = 0;
    private static final int SORT_PRICE_LOW = 1;
    private static final int SORT_PRICE_HIGH = 2;
    private static final int SORT_RATING = 3;
    private static final String STATE_CATEGORY = "state_category";
    private static final String STATE_QUERY = "state_query";
    private static final String STATE_VEG_ONLY = "state_veg_only";
    private static final String STATE_SORT = "state_sort";

    private FragmentExploreBinding binding;
    private AppRepository repository;
    private FoodGridAdapter foodAdapter;
    private CategoryAdapter categoryAdapter;
    private String selectedCategory = "All";
    private String currentQuery = "";
    private boolean vegOnly;
    private int sortMode = SORT_RECOMMENDED;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());

        foodAdapter = new FoodGridAdapter(requireContext(), new ArrayList<>(), this::openFood);
        binding.exploreRecycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.exploreRecycler.setAdapter(foodAdapter);

        categoryAdapter = new CategoryAdapter(FoodData.getCategories(), (category, position) -> {
            selectedCategory = category.name;
            categoryAdapter.setSelectedPosition(position);
            applyFilters();
        });
        binding.exploreCategoryRecycler.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(
                        requireContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.exploreCategoryRecycler.setAdapter(categoryAdapter);

        setupSearch();
        setupFilters();

        if (savedInstanceState != null) {
            selectedCategory = savedInstanceState.getString(STATE_CATEGORY, "All");
            currentQuery = savedInstanceState.getString(STATE_QUERY, "");
            vegOnly = savedInstanceState.getBoolean(STATE_VEG_ONLY, false);
            sortMode = savedInstanceState.getInt(STATE_SORT, SORT_RECOMMENDED);
            categoryAdapter.setSelectedPosition(categoryPosition(selectedCategory));
            binding.vegFilterChip.setChecked(vegOnly);
            if (!currentQuery.isEmpty()) {
                binding.exploreSearchInput.setText(currentQuery);
                binding.exploreSearchInput.setSelection(currentQuery.length());
            }
        }

        applyFilters();
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
        outState.putBoolean(STATE_VEG_ONLY, vegOnly);
        outState.putInt(STATE_SORT, sortMode);
        super.onSaveInstanceState(outState);
    }

    private void setupSearch() {
        binding.exploreSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                currentQuery = text == null ? "" : text.toString().trim();
                binding.clearExploreSearch.setVisibility(
                        currentQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        binding.exploreSearchInput.setOnEditorActionListener((textView, actionId, event) -> {
            UiUtils.hideKeyboard(requireContext(), binding.exploreSearchInput);
            return true;
        });
        binding.clearExploreSearch.setOnClickListener(v ->
                binding.exploreSearchInput.setText(""));
    }

    private void setupFilters() {
        binding.vegFilterChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            vegOnly = isChecked;
            applyFilters();
        });

        binding.sortButton.setOnClickListener(v -> {
            String[] options = {
                    getString(R.string.sort_recommended),
                    getString(R.string.sort_price_low),
                    getString(R.string.sort_price_high),
                    getString(R.string.sort_rating)
            };
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.sort_by)
                    .setSingleChoiceItems(options, sortMode, (dialog, which) -> {
                        sortMode = which;
                        dialog.dismiss();
                        applyFilters();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.clearFiltersButton.setOnClickListener(v -> clearFilters());
        binding.emptyClearFiltersButton.setOnClickListener(v -> clearFilters());
    }

    private void clearFilters() {
        currentQuery = "";
        selectedCategory = "All";
        vegOnly = false;
        sortMode = SORT_RECOMMENDED;
        binding.exploreSearchInput.setText("");
        binding.vegFilterChip.setChecked(false);
        if (categoryAdapter != null) {
            categoryAdapter.setSelectedPosition(0);
        }
        applyFilters();
        UiUtils.hideKeyboard(requireContext(), binding.exploreSearchInput);
    }

    private void applyFilters() {
        if (binding == null) {
            return;
        }
        List<FoodItem> results = FoodData.searchFoods(currentQuery);

        if (!"All".equalsIgnoreCase(selectedCategory)) {
            List<FoodItem> categoryMatches = new ArrayList<>();
            for (FoodItem food : results) {
                if (food.category.equalsIgnoreCase(selectedCategory)) {
                    categoryMatches.add(food);
                }
            }
            results = categoryMatches;
        }

        if (vegOnly) {
            List<FoodItem> vegMatches = new ArrayList<>();
            for (FoodItem food : results) {
                if (food.vegetarian) {
                    vegMatches.add(food);
                }
            }
            results = vegMatches;
        }

        if (sortMode == SORT_PRICE_LOW) {
            Collections.sort(results, Comparator.comparingInt((FoodItem food) -> food.price));
        } else if (sortMode == SORT_PRICE_HIGH) {
            Collections.sort(results, (first, second) -> Integer.compare(second.price, first.price));
        } else if (sortMode == SORT_RATING) {
            Collections.sort(results, (first, second) -> Float.compare(second.rating, first.rating));
        }

        foodAdapter.setFoods(results);
        binding.resultsCount.setText(getResources().getQuantityString(
                R.plurals.restaurant_dishes_count, results.size(), results.size()));

        boolean empty = results.isEmpty();
        binding.exploreRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.exploreEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);

        boolean filtersActive = !currentQuery.isEmpty()
                || vegOnly
                || !"All".equalsIgnoreCase(selectedCategory)
                || sortMode != SORT_RECOMMENDED;
        binding.clearFiltersButton.setVisibility(filtersActive ? View.VISIBLE : View.GONE);
    }

    private void openFood(FoodItem food) {
        startActivity(FoodDetailsActivity.createIntent(requireContext(), food.id));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (repository != null) {
            repository.addListener(this);
        }
        if (binding != null) {
            foodAdapter.notifyDataSetChanged();
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
        if (foodAdapter != null) {
            foodAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
