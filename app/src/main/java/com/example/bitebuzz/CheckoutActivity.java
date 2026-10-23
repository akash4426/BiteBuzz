package com.example.bitebuzz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;

import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.databinding.ActivityCheckoutBinding;
import com.example.bitebuzz.model.CartItem;
import com.example.bitebuzz.model.Order;
import com.example.bitebuzz.util.DialogUtils;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

import java.util.List;

/**
 * Simulated checkout: address, order summary, payment choice and order creation.
 */
public class CheckoutActivity extends AppCompatActivity {

    private static final String STATE_PLACING_ORDER = "state_placing_order";
    private static final String STATE_PENDING_ADDRESS = "state_pending_address";
    private static final String STATE_PENDING_PAYMENT = "state_pending_payment";
    private static final String STATE_PAYMENT_ID = "state_payment_id";

    private ActivityCheckoutBinding binding;
    private AppRepository repository;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean placingOrder;
    private String pendingAddress = "";
    private String pendingPaymentMethod = "";
    private int selectedPaymentId = R.id.paymentCod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.applySystemInsets(binding.checkoutRoot);

        repository = AppRepository.getInstance(this);
        if (repository.isCartEmpty()) {
            Toast.makeText(this, R.string.checkout_empty_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupAddress();
        setupPayment();
        binding.checkoutBackButton.setOnClickListener(v -> finish());
        binding.checkoutPlaceOrderButton.setOnClickListener(v -> placeOrder());
        render();

        if (savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_PLACING_ORDER, false)) {
            String address = savedInstanceState.getString(STATE_PENDING_ADDRESS);
            String payment = savedInstanceState.getString(STATE_PENDING_PAYMENT);
            if (address != null && !address.trim().isEmpty()
                    && payment != null && !payment.isEmpty()) {
                beginPlacingOrder(address, payment);
            }
        }
    }

    private void setupAddress() {
        binding.checkoutAddress.setText(repository.getAddress());
        View.OnClickListener edit = v -> DialogUtils.showInputDialog(
                CheckoutActivity.this,
                R.string.address_dialog_title,
                R.string.address_hint,
                repository.getAddress(),
                R.string.address_empty_error,
                value -> {
                    repository.setAddress(value);
                    binding.checkoutAddress.setText(value);
                    UiUtils.showSnackbar(binding.checkoutRoot, binding.checkoutBottomBar,
                            getString(R.string.address_updated));
                });
        binding.checkoutAddressCard.setOnClickListener(edit);
        binding.editCheckoutAddress.setOnClickListener(edit);
    }

    private void setupPayment() {
        binding.codRow.setOnClickListener(v -> selectPayment(R.id.paymentCod, true));
        binding.upiRow.setOnClickListener(v -> selectPayment(R.id.paymentUpi, true));
        binding.cardRow.setOnClickListener(v -> selectPayment(R.id.paymentCard, true));
        binding.paymentCod.setOnClickListener(v -> selectPayment(R.id.paymentCod, true));
        binding.paymentUpi.setOnClickListener(v -> selectPayment(R.id.paymentUpi, true));
        binding.paymentCard.setOnClickListener(v -> selectPayment(R.id.paymentCard, true));
    }

    /**
     * The radio buttons are nested inside clickable rows, so RadioGroup cannot
     * enforce a single selection on its own. Keep the three states in sync here.
     */
    private void selectPayment(int paymentId, boolean showFeedback) {
        boolean changed = selectedPaymentId != paymentId;
        selectedPaymentId = paymentId;
        binding.paymentCod.setChecked(paymentId == R.id.paymentCod);
        binding.paymentUpi.setChecked(paymentId == R.id.paymentUpi);
        binding.paymentCard.setChecked(paymentId == R.id.paymentCard);

        if (!changed || !showFeedback) {
            return;
        }
        if (paymentId == R.id.paymentUpi) {
            UiUtils.showSnackbar(binding.checkoutRoot, binding.checkoutBottomBar,
                    getString(R.string.payment_selected, getString(R.string.upi_demo)));
        } else if (paymentId == R.id.paymentCard) {
            UiUtils.showSnackbar(binding.checkoutRoot, binding.checkoutBottomBar,
                    getString(R.string.payment_selected, getString(R.string.card_demo)));
        }
    }

    private void render() {
        List<CartItem> items = repository.getCartItems();
        binding.checkoutItemCount.setText(
                getString(R.string.cart_item_count, repository.getCartCount()));
        binding.checkoutItemsContainer.removeAllViews();
        for (CartItem item : items) {
            String label = item.quantity + " × " + item.food.name;
            addSummaryRow(binding.checkoutItemsContainer, label,
                    PriceUtils.format(item.getLineTotal()), false);
            String extras = item.getExtrasLabel();
            if (!extras.isEmpty()) {
                TextView extrasView = new TextView(this);
                extrasView.setText(extras);
                extrasView.setTextSize(9.5f);
                extrasView.setTextColor(ContextCompat.getColor(this, R.color.primary));
                extrasView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_regular));
                extrasView.setPadding(0, 0, 0, dp(5));
                binding.checkoutItemsContainer.addView(extrasView);
            }
        }

        AppRepository.PriceSummary totals = repository.calculateTotals();
        binding.checkoutSubtotal.setText(PriceUtils.format(totals.subtotal));

        if (totals.isFreeDelivery()) {
            binding.checkoutDelivery.setText(R.string.free);
            binding.checkoutDelivery.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else {
            binding.checkoutDelivery.setText(PriceUtils.format(totals.deliveryFee));
            binding.checkoutDelivery.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }

        if (totals.hasDiscount()) {
            binding.checkoutDiscountRow.setVisibility(View.VISIBLE);
            binding.checkoutDiscount.setText(PriceUtils.formatWithMinus(totals.discount));
        } else {
            binding.checkoutDiscountRow.setVisibility(View.GONE);
        }

        if (!totals.couponCode.isEmpty()) {
            binding.checkoutCouponNote.setVisibility(View.VISIBLE);
            binding.checkoutCouponNote.setText(
                    getString(R.string.coupon_applied_note, totals.couponCode));
        } else {
            binding.checkoutCouponNote.setVisibility(View.GONE);
        }

        binding.checkoutTotal.setText(PriceUtils.format(totals.total));
        binding.checkoutBottomTotal.setText(PriceUtils.format(totals.total));
        binding.checkoutPlaceOrderButton.setText(
                getString(R.string.place_order_amount, PriceUtils.format(totals.total)));
    }

    private void addSummaryRow(LinearLayout container,
                               String label,
                               String amount,
                               boolean emphasized) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(5), 0, dp(1));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(11.5f);
        labelView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        labelView.setTypeface(ResourcesCompat.getFont(this,
                emphasized ? R.font.poppins_semibold : R.font.poppins_regular));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        labelView.setLayoutParams(labelParams);

        TextView amountView = new TextView(this);
        amountView.setText(amount);
        amountView.setTextSize(11.5f);
        amountView.setTextColor(ContextCompat.getColor(this,
                emphasized ? R.color.primary : R.color.text_primary));
        amountView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_semibold));

        row.addView(labelView);
        row.addView(amountView);
        container.addView(row);
    }

    private void placeOrder() {
        if (placingOrder) {
            return;
        }
        String address = repository.getAddress();
        if (address == null || address.trim().isEmpty()) {
            Toast.makeText(this, R.string.checkout_address_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = getString(R.string.cod);
        if (selectedPaymentId == R.id.paymentUpi) {
            paymentMethod = getString(R.string.upi_demo);
        } else if (selectedPaymentId == R.id.paymentCard) {
            paymentMethod = getString(R.string.card_demo);
        }

        beginPlacingOrder(address, paymentMethod);
    }

    private void beginPlacingOrder(String address, String paymentMethod) {
        if (placingOrder) {
            return;
        }
        placingOrder = true;
        pendingAddress = address;
        pendingPaymentMethod = paymentMethod;
        binding.checkoutPlaceOrderButton.setEnabled(false);
        binding.checkoutPlaceOrderButton.setText(R.string.placing_order);

        handler.postDelayed(() -> {
            Order order = repository.placeOrder(address, paymentMethod);
            if (order == null) {
                Toast.makeText(this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                placingOrder = false;
                binding.checkoutPlaceOrderButton.setEnabled(true);
                AppRepository.PriceSummary totals = repository.calculateTotals();
                binding.checkoutPlaceOrderButton.setText(getString(
                        R.string.place_order_amount, PriceUtils.format(totals.total)));
                return;
            }
            startActivity(OrderConfirmationActivity.createIntent(this, order.id));
            finish();
        }, 850L);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(STATE_PLACING_ORDER, placingOrder);
        outState.putString(STATE_PENDING_ADDRESS, pendingAddress);
        outState.putString(STATE_PENDING_PAYMENT, pendingPaymentMethod);
        outState.putInt(STATE_PAYMENT_ID, selectedPaymentId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedPaymentId = savedInstanceState.getInt(STATE_PAYMENT_ID, R.id.paymentCod);
        selectPayment(selectedPaymentId, false);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
