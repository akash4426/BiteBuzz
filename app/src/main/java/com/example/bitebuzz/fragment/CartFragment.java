package com.example.bitebuzz.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bitebuzz.CheckoutActivity;
import com.example.bitebuzz.MainActivity;
import com.example.bitebuzz.R;
import com.example.bitebuzz.adapter.CartAdapter;
import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.databinding.FragmentCartBinding;
import com.example.bitebuzz.model.CartItem;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Full cart with quantity controls, coupon validation and live totals.
 */
public class CartFragment extends Fragment implements AppRepository.DataListener {

    private FragmentCartBinding binding;
    private AppRepository repository;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = AppRepository.getInstance(requireContext());

        adapter = new CartAdapter(requireContext(), new ArrayList<>());
        binding.cartRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cartRecycler.setAdapter(adapter);

        binding.browseFromCart.setOnClickListener(v -> openTab(MainActivity.TAB_EXPLORE));
        binding.continueShoppingButton.setOnClickListener(v -> openTab(MainActivity.TAB_HOME));
        binding.placeOrderButton.setOnClickListener(v -> {
            if (repository.isCartEmpty()) {
                UiUtils.showSnackbar(binding.getRoot(),
                        getString(R.string.cart_empty_error));
            } else {
                startActivity(new Intent(requireContext(), CheckoutActivity.class));
            }
        });

        binding.applyCouponButton.setOnClickListener(v -> applyCoupon());
        binding.removeCouponButton.setOnClickListener(v -> {
            repository.removeCoupon();
            binding.couponInput.setText("");
            binding.couponErrorText.setVisibility(View.GONE);
            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.coupon_removed));
        });
        binding.couponInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                binding.couponErrorText.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        render();
    }

    private void applyCoupon() {
        String code = binding.couponInput.getText() == null
                ? ""
                : binding.couponInput.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            showCouponError(getString(R.string.coupon_empty));
            return;
        }
        if (!repository.isValidCoupon(code)) {
            showCouponError(getString(R.string.coupon_invalid));
            return;
        }
        repository.applyCoupon(code);
        binding.couponInput.setText("");
        binding.couponErrorText.setVisibility(View.GONE);
        UiUtils.hideKeyboard(requireContext(), binding.couponInput);
        UiUtils.showSnackbar(binding.getRoot(), getString(R.string.coupon_applied_success));
    }

    private void showCouponError(String message) {
        binding.couponErrorText.setText(message);
        binding.couponErrorText.setVisibility(View.VISIBLE);
    }

    private void render() {
        if (binding == null) {
            return;
        }
        List<CartItem> items = repository.getCartItems();
        adapter.setItems(items);
        int count = repository.getCartCount();

        binding.cartCountLabel.setText(getResources().getQuantityString(
                R.plurals.items_ready, count, count));

        boolean empty = items.isEmpty();
        binding.cartRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.cartEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.cartSummaryCard.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {
            return;
        }

        AppRepository.PriceSummary totals = repository.calculateTotals();
        binding.subtotalValue.setText(PriceUtils.format(totals.subtotal));

        if (totals.isFreeDelivery()) {
            binding.deliveryFeeValue.setText(R.string.free);
            binding.deliveryFeeValue.setTextColor(ContextCompat.getColor(
                    requireContext(), R.color.success));
        } else {
            binding.deliveryFeeValue.setText(PriceUtils.format(totals.deliveryFee));
            binding.deliveryFeeValue.setTextColor(ContextCompat.getColor(
                    requireContext(), R.color.text_primary));
        }

        if (totals.hasDiscount()) {
            binding.discountRow.setVisibility(View.VISIBLE);
            binding.discountValue.setText(PriceUtils.formatWithMinus(totals.discount));
        } else {
            binding.discountRow.setVisibility(View.GONE);
        }

        binding.totalValue.setText(PriceUtils.format(totals.total));

        if (totals.isFreeDelivery()) {
            binding.freeDeliveryHint.setText(R.string.free_delivery_unlocked);
            binding.freeDeliveryHint.setTextColor(ContextCompat.getColor(
                    requireContext(), R.color.success));
        } else {
            binding.freeDeliveryHint.setText(getString(
                    R.string.free_delivery_nudge,
                    PriceUtils.format(totals.getAmountForFreeDelivery())));
            binding.freeDeliveryHint.setTextColor(ContextCompat.getColor(
                    requireContext(), R.color.primary));
        }

        if (!totals.couponCode.isEmpty()) {
            binding.couponAppliedRow.setVisibility(View.VISIBLE);
            if (AppRepository.COUPON_BUZZ10.equals(totals.couponCode)) {
                binding.couponAppliedText.setText(R.string.coupon_buzz10_applied);
            } else {
                binding.couponAppliedText.setText(R.string.coupon_firstbite_applied);
            }
        } else {
            binding.couponAppliedRow.setVisibility(View.GONE);
        }
    }

    private void openTab(int tab) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).selectTab(tab);
        } else {
            startActivity(MainActivity.createIntent(requireContext(), tab));
        }
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
