package com.example.bitebuzz.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitebuzz.FoodDetailsActivity;
import com.example.bitebuzz.MainActivity;
import com.example.bitebuzz.R;
import com.example.bitebuzz.adapter.FoodListAdapter;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.databinding.FragmentFavoritesBinding;
import com.example.bitebuzz.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays every dish the user has hearted.
 */
public class FavoritesFragment extends Fragment implements AppRepository.DataListener {

    private FragmentFavoritesBinding binding;
    private AppRepository repository;
    private FoodListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());
        adapter = new FoodListAdapter(requireContext(), new ArrayList<>(), this::openFood);
        binding.favoritesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.favoritesRecycler.setAdapter(adapter);
        binding.exploreFromFavorites.setOnClickListener(v ->
                startActivity(MainActivity.createIntent(requireContext(), MainActivity.TAB_EXPLORE)));
        render();
    }

    private void render() {
        if (binding == null) {
            return;
        }
        List<FoodItem> favorites = repository.getFavoriteFoods();
        adapter.setFoods(favorites);
        boolean empty = favorites.isEmpty();
        binding.favoritesRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.favoritesEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.favoritesCount.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            binding.favoritesCount.setText(getResources().getQuantityString(
                    R.plurals.saved_bites_count, favorites.size(), favorites.size()));
        }
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
        render();
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
        render();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
