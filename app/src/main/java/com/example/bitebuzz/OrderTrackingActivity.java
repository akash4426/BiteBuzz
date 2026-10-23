package com.example.bitebuzz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.databinding.ActivityOrderTrackingBinding;
import com.example.bitebuzz.databinding.ItemTrackStepBinding;
import com.example.bitebuzz.model.Order;
import com.example.bitebuzz.util.UiUtils;

/**
 * Four-stage demo tracking timeline with a next-step simulation button.
 */
public class OrderTrackingActivity extends AppCompatActivity {
    private static final String EXTRA_ORDER_ID = "extra_order_id";
    private static final int STEP_COUNT = 4;

    private ActivityOrderTrackingBinding binding;
    private AppRepository repository;
    private Order order;

    public static Intent createIntent(Context context, String orderId) {
        Intent intent = new Intent(context, OrderTrackingActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.applySystemInsets(binding.trackingRoot);

        repository = AppRepository.getInstance(this);
        order = repository.getOrderById(getIntent().getStringExtra(EXTRA_ORDER_ID));
        if (order == null) {
            Toast.makeText(this, R.string.order_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.trackingBackButton.setOnClickListener(v -> finish());
        binding.simulateNextButton.setOnClickListener(v -> handleNextStep());
        binding.trackingOrderLabel.setText(
                getString(R.string.order_tracking_label, order.id));
        render();
    }

    private void render() {
        binding.trackingStatusTitle.setText(statusTitle(order.status));
        binding.trackingStatusBody.setText(statusBody(order.status));
        binding.trackingEta.setText(etaFor(order.status));
        binding.trackingStepCount.setText(
                getString(R.string.step_count, Math.min(order.status + 1, STEP_COUNT)));

        binding.trackingStepsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < STEP_COUNT; i++) {
            ItemTrackStepBinding step = ItemTrackStepBinding.inflate(
                    inflater, binding.trackingStepsContainer, false);
            step.stepTitle.setText(stepTitle(i));
            step.stepDescription.setText(stepDescription(i));

            boolean done = order.status == Order.STATUS_DELIVERED || i < order.status;
            boolean current = i == order.status && order.status != Order.STATUS_DELIVERED;

            if (done) {
                step.stepCircle.setBackgroundResource(R.drawable.bg_step_done);
                step.stepIcon.setVisibility(View.VISIBLE);
                step.stepNumber.setVisibility(View.GONE);
                step.stepTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                step.stepDescription.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                step.stepLine.setBackgroundResource(R.drawable.bg_track_line_done);
            } else if (current) {
                step.stepCircle.setBackgroundResource(R.drawable.bg_step_current);
                step.stepIcon.setVisibility(View.GONE);
                step.stepNumber.setVisibility(View.VISIBLE);
                step.stepNumber.setTextColor(ContextCompat.getColor(this, R.color.white));
                step.stepTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                step.stepDescription.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                step.stepLine.setBackgroundResource(R.drawable.bg_track_line_idle);
            } else {
                step.stepCircle.setBackgroundResource(R.drawable.bg_step_todo);
                step.stepIcon.setVisibility(View.GONE);
                step.stepNumber.setVisibility(View.VISIBLE);
                step.stepNumber.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
                step.stepTitle.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
                step.stepDescription.setTextColor(ContextCompat.getColor(this, R.color.text_hint));
                step.stepLine.setBackgroundResource(R.drawable.bg_track_line_idle);
            }

            step.stepNumber.setText(String.valueOf(i + 1));
            step.stepLine.setVisibility(i == STEP_COUNT - 1 ? View.GONE : View.VISIBLE);
            binding.trackingStepsContainer.addView(step.getRoot());
        }

        if (order.status == Order.STATUS_DELIVERED) {
            binding.simulateNextButton.setText(R.string.order_again);
        } else {
            binding.simulateNextButton.setText(R.string.simulate_next_step);
        }
    }

    private void handleNextStep() {
        if (order.status == Order.STATUS_DELIVERED) {
            repository.addOrderItemsToCart(order);
            startActivity(MainActivity.createIntent(this, MainActivity.TAB_CART));
            finish();
            return;
        }

        int nextStatus = order.status + 1;
        repository.updateOrderStatus(order.id, nextStatus);
        render();
        Toast.makeText(this, statusBody(order.status), Toast.LENGTH_SHORT).show();
    }

    private String statusTitle(int status) {
        switch (status) {
            case Order.STATUS_PREPARING:
                return getString(R.string.status_preparing);
            case Order.STATUS_OUT_FOR_DELIVERY:
                return getString(R.string.status_out_for_delivery);
            case Order.STATUS_DELIVERED:
                return getString(R.string.status_delivered);
            default:
                return getString(R.string.status_order_confirmed);
        }
    }

    private String statusBody(int status) {
        switch (status) {
            case Order.STATUS_PREPARING:
                return getString(R.string.tracking_status_preparing);
            case Order.STATUS_OUT_FOR_DELIVERY:
                return getString(R.string.tracking_status_out);
            case Order.STATUS_DELIVERED:
                return getString(R.string.tracking_status_delivered);
            default:
                return getString(R.string.tracking_status_confirmed);
        }
    }

    private String etaFor(int status) {
        switch (status) {
            case Order.STATUS_PREPARING:
                return getString(R.string.eta_preparing, 20, 30);
            case Order.STATUS_OUT_FOR_DELIVERY:
                return getString(R.string.eta_out, 10, 20);
            case Order.STATUS_DELIVERED:
                return getString(R.string.eta_delivered);
            default:
                return getString(R.string.eta_confirmed, 25, 35);
        }
    }

    private String stepTitle(int index) {
        switch (index) {
            case 1:
                return getString(R.string.status_preparing);
            case 2:
                return getString(R.string.status_out_for_delivery);
            case 3:
                return getString(R.string.status_delivered);
            default:
                return getString(R.string.status_order_confirmed);
        }
    }

    private String stepDescription(int index) {
        switch (index) {
            case 1:
                return getString(R.string.status_preparing_desc);
            case 2:
                return getString(R.string.status_out_desc);
            case 3:
                return getString(R.string.status_delivered_desc);
            default:
                return getString(R.string.status_confirmed_desc);
        }
    }
}
