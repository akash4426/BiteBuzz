package com.example.bitebuzz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitebuzz.adapter.FoodListAdapter;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.ActivityRestaurantBinding;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Restaurant;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;

/**
 * Restaurant header plus its full local menu.
 */
public class RestaurantActivity extends AppCompatActivity implements AppRepository.DataListener {

    private static final String EXTRA_RESTAURANT_ID = "extra_restaurant_id";

    private ActivityRestaurantBinding binding;
    private AppRepository repository;
    private FoodListAdapter menuAdapter;

    public static Intent createIntent(Context context, String restaurantId) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_ID, restaurantId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityRestaurantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.applySystemInsets(binding.restaurantRoot);

        repository = AppRepository.getInstance(this);

        Restaurant restaurant = FoodData.getRestaurantById(
                getIntent().getStringExtra(EXTRA_RESTAURANT_ID));
        if (restaurant == null) {
            Toast.makeText(this, R.string.restaurant_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.restaurantBanner.setImageResource(restaurant.imageRes);
        binding.restaurantBanner.setContentDescription(restaurant.name);
        binding.restaurantDetailName.setText(restaurant.name);
        binding.restaurantDetailCuisine.setText(restaurant.cuisine);
        binding.restaurantDetailRating.setText(PriceUtils.formatRating(restaurant.rating));
        binding.restaurantDetailTime.setText(restaurant.getDeliveryTime());
        binding.restaurantDetailPrice.setText(
                getString(R.string.price_for_two, restaurant.priceForTwo));
        binding.restaurantDetailOffer.setText(restaurant.offer);
        binding.restaurantBackButton.setOnClickListener(v -> finish());

        ArrayList<FoodItem> menu = new ArrayList<>(
                FoodData.getFoodsByRestaurant(restaurant.id));
        menuAdapter = new FoodListAdapter(
                this, menu, this::openFood);
        binding.restaurantMenuRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.restaurantMenuRecycler.setAdapter(menuAdapter);
        binding.restaurantDishCount.setText(getResources().getQuantityString(
                R.plurals.restaurant_dishes_count, menu.size(), menu.size()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        repository.addListener(this);
        if (menuAdapter != null) {
            menuAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        repository.removeListener(this);
        super.onStop();
    }

    @Override
    public void onDataChanged() {
        if (menuAdapter != null) {
            menuAdapter.notifyDataSetChanged();
        }
    }

    private void openFood(FoodItem food) {
        startActivity(FoodDetailsActivity.createIntent(this, food.id));
    }
}
