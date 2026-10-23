package com.example.bitebuzz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.data.FoodData;
import com.example.bitebuzz.databinding.ActivityFoodDetailsBinding;
import com.example.bitebuzz.model.AddOn;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Restaurant;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Rich dish screen with quantity, extras, cooking notes, favourite toggle
 * and the Add to Cart action.
 */
public class FoodDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_FOOD_ID = "extra_food_id";
    private static final String STATE_QUANTITY = "state_quantity";

    private ActivityFoodDetailsBinding binding;
    private AppRepository repository;
    private FoodItem food;
    private Restaurant restaurant;
    private int quantity = 1;

    public static Intent createIntent(Context context, String foodId) {
        Intent intent = new Intent(context, FoodDetailsActivity.class);
        intent.putExtra(EXTRA_FOOD_ID, foodId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityFoodDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.applySystemInsets(binding.foodDetailsRoot);

        repository = AppRepository.getInstance(this);
        food = FoodData.getFoodById(getIntent().getStringExtra(EXTRA_FOOD_ID));
        if (food == null) {
            Toast.makeText(this, R.string.food_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        restaurant = FoodData.getRestaurantById(food.restaurantId);

        bindFood();
        setupQuantity();
        setupExtras();
        setupActions();
        updateTotal();
    }

    private void bindFood() {
        binding.detailFoodImage.setImageResource(food.imageRes);
        binding.detailFoodImage.setContentDescription(food.name);
        binding.detailFoodName.setText(food.name);
        binding.detailRestaurantName.setText(restaurant == null ? "" : restaurant.name);
        binding.detailRestaurantLabel.setText(restaurant == null
                ? ""
                : getString(R.string.dish_by_restaurant, restaurant.name));
        binding.detailCategory.setText(food.category);
        binding.detailRating.setText(PriceUtils.formatRating(food.rating));
        binding.detailReviews.setText(getResources().getQuantityString(
                R.plurals.reviews_count, food.reviewCount, food.reviewCount));
        binding.detailPrepTime.setText(getString(R.string.preparation_time, food.prepMinutes));
        binding.detailPrice.setText(PriceUtils.format(food.price));
        binding.detailDescription.setText(food.description);
        bindAddOnPrices();

        if (food.vegetarian) {
            binding.vegBadge.setBackgroundResource(R.drawable.bg_veg_tag);
            binding.vegIcon.setImageResource(R.drawable.ic_veg);
            binding.vegLabel.setText(R.string.veg);
            binding.vegLabel.setTextColor(ContextCompat.getColor(this, R.color.veg));
        } else {
            binding.vegBadge.setBackgroundResource(R.drawable.bg_nonveg_tag);
            binding.vegIcon.setImageResource(R.drawable.ic_nonveg);
            binding.vegLabel.setText(R.string.non_veg);
            binding.vegLabel.setTextColor(ContextCompat.getColor(this, R.color.nonveg));
        }

        updateFavoriteIcon();
        binding.detailRestaurantName.setOnClickListener(v -> {
            if (restaurant != null) {
                startActivity(RestaurantActivity.createIntent(this, restaurant.id));
            }
        });
    }

    private void bindAddOnPrices() {
        AddOn cheese = FoodData.getAddOnById("cheese");
        if (cheese != null) {
            binding.extraCheesePrice.setText(getString(R.string.add_on_price, cheese.price));
        }
        AddOn portion = FoodData.getAddOnById("portion");
        if (portion != null) {
            binding.extraPortionPrice.setText(getString(R.string.add_on_price, portion.price));
        }
        AddOn drink = FoodData.getAddOnById("drink");
        if (drink != null) {
            binding.addDrinkPrice.setText(getString(R.string.add_on_price, drink.price));
        }
    }

    private void setupQuantity() {
        binding.detailPlusButton.setOnClickListener(v -> {
            if (quantity < 20) {
                quantity++;
                updateQuantityViews();
            }
        });
        binding.detailMinusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityViews();
            }
        });
        updateQuantityViews();
    }

    private void updateQuantityViews() {
        binding.detailQuantity.setText(String.valueOf(quantity));
        binding.detailMinusButton.setEnabled(quantity > 1);
        binding.detailMinusButton.setAlpha(quantity > 1 ? 1f : 0.35f);
        updateTotal();
    }

    private void setupExtras() {
        View.OnClickListener listener = v -> updateTotal();
        binding.extraCheeseCheckBox.setOnClickListener(listener);
        binding.extraPortionCheckBox.setOnClickListener(listener);
        binding.addDrinkCheckBox.setOnClickListener(listener);
    }

    private void setupActions() {
        binding.detailBackButton.setOnClickListener(v -> finish());
        binding.detailFavoriteButton.setOnClickListener(v -> {
            boolean favorite = repository.toggleFavorite(food.id);
            updateFavoriteIcon();
            UiUtils.showSnackbar(binding.getRoot(), binding.detailsBottomBar, getString(
                    favorite ? R.string.favorite_added : R.string.favorite_removed));
        });
        binding.addToCartDetailButton.setOnClickListener(v -> {
            repository.addToCart(food, quantity, selectedAddOns(), instructions());
            String message = getString(R.string.added_with_quantity, quantity, food.name);
            UiUtils.showSnackbarWithAction(
                    binding.getRoot(),
                    binding.detailsBottomBar,
                    message,
                    getString(R.string.view_cart),
                    action -> {
                        startActivity(MainActivity.createIntent(this, MainActivity.TAB_CART));
                        finish();
                    });
        });
    }

    private void updateFavoriteIcon() {
        boolean favorite = repository.isFavorite(food.id);
        binding.detailFavoriteButton.setImageResource(
                favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        binding.detailFavoriteButton.setColorFilter(ContextCompat.getColor(
                this, favorite ? R.color.primary : R.color.white));
    }

    private List<AddOn> selectedAddOns() {
        List<AddOn> selected = new ArrayList<>();
        if (binding.extraCheeseCheckBox.isChecked()) {
            addOn(selected, "cheese");
        }
        if (binding.extraPortionCheckBox.isChecked()) {
            addOn(selected, "portion");
        }
        if (binding.addDrinkCheckBox.isChecked()) {
            addOn(selected, "drink");
        }
        return selected;
    }

    private void addOn(List<AddOn> selected, String id) {
        AddOn addOn = FoodData.getAddOnById(id);
        if (addOn != null) {
            selected.add(addOn);
        }
    }

    private String instructions() {
        return binding.instructionsInput.getText() == null
                ? ""
                : binding.instructionsInput.getText().toString().trim();
    }

    private void updateTotal() {
        int unitPrice = food.price;
        for (AddOn addOn : selectedAddOns()) {
            unitPrice += addOn.price;
        }
        int total = unitPrice * quantity;
        binding.detailsTotalValue.setText(PriceUtils.format(total));
        binding.addToCartDetailButton.setText(
                getString(R.string.add_to_cart_total, PriceUtils.format(total)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_QUANTITY, quantity);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (food == null) {
            return;
        }
        quantity = Math.max(1, savedInstanceState.getInt(STATE_QUANTITY, 1));
        updateQuantityViews();
    }
}
