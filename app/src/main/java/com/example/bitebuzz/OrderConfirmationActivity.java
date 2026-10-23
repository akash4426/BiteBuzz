package com.example.bitebuzz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import android.view.animation.OvershootInterpolator;

import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.databinding.ActivityOrderConfirmationBinding;
import com.example.bitebuzz.model.CartItem;
import com.example.bitebuzz.model.Order;
import com.example.bitebuzz.util.PriceUtils;
import com.example.bitebuzz.util.UiUtils;

/**
 * Friendly "order confirmed" celebratory screen.
 */
public class OrderConfirmationActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER_ID = "extra_order_id";

    private ActivityOrderConfirmationBinding binding;
    private AppRepository repository;
    private Order order;

    public static Intent createIntent(Context context, String orderId) {
        Intent intent = new Intent(context, OrderConfirmationActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityOrderConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.applySystemInsets(binding.getRoot());

        repository = AppRepository.getInstance(this);
        order = repository.getOrderById(getIntent().getStringExtra(EXTRA_ORDER_ID));
        if (order == null) {
            Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindOrder();
        setupActions();
        playCelebration();
    }

    private void bindOrder() {
        binding.confirmationOrderId.setText(order.id);
        binding.confirmationEta.setText(
                getString(R.string.order_confirm_delivery_time, 25, 35));
        binding.confirmationAmount.setText(PriceUtils.format(order.total));
        binding.confirmationTotal.setText(PriceUtils.format(order.total));
        binding.confirmationAddress.setText(order.address);
        binding.confirmationPayment.setText(order.paymentMethod);

        binding.confirmationItemsContainer.removeAllViews();
        for (CartItem item : order.items) {
            addSummaryRow(
                    item.quantity + " × " + item.food.name,
                    PriceUtils.format(item.getLineTotal()));
        }
    }

    private void addSummaryRow(String label, String amount) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(4));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(11.5f);
        labelView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        labelView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_regular));
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView amountView = new TextView(this);
        amountView.setText(amount);
        amountView.setTextSize(11.5f);
        amountView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        amountView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins_semibold));

        row.addView(labelView);
        row.addView(amountView);
        binding.confirmationItemsContainer.addView(row);
    }

    private void setupActions() {
        binding.confirmationTrackButton.setOnClickListener(v ->
                startActivity(OrderTrackingActivity.createIntent(this, order.id)));
        binding.confirmationHomeButton.setOnClickListener(v -> {
            startActivity(MainActivity.createIntent(this, MainActivity.TAB_HOME));
            finish();
        });
    }

    private void playCelebration() {
        binding.confirmationHero.setScaleX(0.55f);
        binding.confirmationHero.setScaleY(0.55f);
        binding.confirmationHero.setAlpha(0f);
        binding.confirmationHero.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(550)
                .setInterpolator(new OvershootInterpolator(1.15f))
                .start();

        View[] confetti = {
                binding.confetti1, binding.confetti2, binding.confetti3,
                binding.confetti4, binding.confetti5, binding.confetti6
        };
        for (int i = 0; i < confetti.length; i++) {
            View view = confetti[i];
            view.setAlpha(0f);
            view.setTranslationY(-28f);
            view.setScaleX(0.7f);
            view.setScaleY(0.7f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(150L + (i * 70L))
                    .setDuration(450)
                    .start();
        }

        binding.confirmationTrackButton.setAlpha(0f);
        binding.confirmationTrackButton.animate()
                .alpha(1f)
                .setStartDelay(500)
                .setDuration(400)
                .start();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
