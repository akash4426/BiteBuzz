package com.example.bitebuzz;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitebuzz.adapter.OrderAdapter;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.databinding.ActivityProfileBinding;
import com.example.bitebuzz.model.Order;
import com.example.bitebuzz.util.DialogUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.List;

/**
 * Editable profile, quick stats and order history.
 */
public class ProfileActivity extends AppCompatActivity implements AppRepository.DataListener {

    private ActivityProfileBinding binding;
    private AppRepository repository;
    private OrderAdapter orderAdapter;
    private TextView cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.applySystemInsets(binding.profileRoot);

        repository = AppRepository.getInstance(this);
        cartBadge = binding.getRoot().findViewById(R.id.cartCountBadge);

        orderAdapter = new OrderAdapter(repository.getOrders(), order ->
                startActivity(OrderTrackingActivity.createIntent(this, order.id)));
        binding.profileOrdersRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.profileOrdersRecycler.setAdapter(orderAdapter);

        setupActions();
        render();
    }

    private void setupActions() {
        binding.profileBackButton.setOnClickListener(v -> finish());

        binding.editProfileName.setOnClickListener(v -> DialogUtils.showInputDialog(
                this,
                R.string.name_dialog_title,
                R.string.name_hint,
                repository.getName(),
                R.string.name_empty_error,
                value -> {
                    repository.setName(value);
                    UiUtils.showSnackbar(binding.getRoot(),
                            getString(R.string.profile_updated));
                }));

        View.OnClickListener editAddress = v -> DialogUtils.showInputDialog(
                this,
                R.string.address_dialog_title,
                R.string.address_hint,
                repository.getAddress(),
                R.string.address_empty_error,
                value -> {
                    repository.setAddress(value);
                    UiUtils.showSnackbar(binding.getRoot(),
                            getString(R.string.address_updated));
                });
        binding.editProfileAddress.setOnClickListener(editAddress);

        View.OnClickListener openOrders = v -> binding.profileScroll.smoothScrollTo(
                0, binding.orderHistoryHeader.getTop());
        binding.menuOrders.setOnClickListener(openOrders);
        binding.profileOrdersStatCard.setOnClickListener(openOrders);

        View.OnClickListener openFavorites = v -> startActivity(
                MainActivity.createIntent(this, MainActivity.TAB_FAVORITES));
        binding.menuFavorites.setOnClickListener(openFavorites);
        binding.profileFavoritesStatCard.setOnClickListener(openFavorites);

        binding.menuAbout.setOnClickListener(v -> DialogUtils.showAboutDialog(this));
        binding.menuVersion.setOnClickListener(v -> Toast.makeText(
                this, R.string.app_version_value, Toast.LENGTH_SHORT).show());

        binding.startOrderButton.setOnClickListener(v -> startActivity(
                MainActivity.createIntent(this, MainActivity.TAB_EXPLORE)));

        View cartButton = binding.getRoot().findViewById(R.id.profileCartButton);
        if (cartButton != null) {
            cartButton.setOnClickListener(v -> startActivity(
                    MainActivity.createIntent(this, MainActivity.TAB_CART)));
        }
    }

    private void render() {
        binding.profileName.setText(repository.getName());
        binding.profileInitials.setText(UiUtils.initialsOf(repository.getName()));
        binding.profileAddress.setText(repository.getAddress());

        List<Order> orders = repository.getOrders();
        int favoriteCount = repository.getFavoriteCount();
        binding.profileOrdersCount.setText(String.valueOf(orders.size()));
        binding.profileFavoritesCount.setText(String.valueOf(favoriteCount));
        binding.profileOrdersLabel.setText(getResources().getQuantityString(
                R.plurals.orders_stat_label, orders.size()));
        binding.profileFavoritesLabel.setText(getResources().getQuantityString(
                R.plurals.favorites_stat_label, favoriteCount));
        String orderCount = getResources().getQuantityString(
                R.plurals.order_history_count, orders.size(), orders.size());
        binding.profileHistoryCount.setText(orderCount);

        orderAdapter.setOrders(orders);
        boolean empty = orders.isEmpty();
        binding.profileOrdersRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.profileOrdersEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);

        updateCartBadge();
    }

    private void updateCartBadge() {
        if (cartBadge == null) {
            return;
        }
        int count = repository.getCartCount();
        cartBadge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        cartBadge.setText(String.valueOf(count));
    }

    @Override
    protected void onStart() {
        super.onStart();
        repository.addListener(this);
        render();
    }

    @Override
    protected void onStop() {
        repository.removeListener(this);
        super.onStop();
    }

    @Override
    public void onDataChanged() {
        render();
    }
}
