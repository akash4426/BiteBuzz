package com.example.bitebuzz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.bitebuzz.databinding.ActivitySplashBinding;
import com.example.bitebuzz.util.UiUtils;

/**
 * A short animated welcome before the main app appears.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 1900L;

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable launchRunnable = () -> {
        if (isFinishing()) {
            return;
        }
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setLightSystemBars(this, false);

        playIntroAnimation();
        handler.postDelayed(launchRunnable, SPLASH_DURATION);
    }

    private void playIntroAnimation() {
        binding.splashLogo.setAlpha(0f);
        binding.splashLogo.setScaleX(0.7f);
        binding.splashLogo.setScaleY(0.7f);
        binding.splashLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(650)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();

        binding.splashBrand.setAlpha(0f);
        binding.splashBrand.setTranslationY(26f);
        binding.splashBrand.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(180)
                .setDuration(500)
                .start();

        binding.splashTagline.setAlpha(0f);
        binding.splashTagline.animate()
                .alpha(1f)
                .setStartDelay(360)
                .setDuration(500)
                .start();

        binding.splashBee.animate()
                .translationY(-10f)
                .setStartDelay(500)
                .setDuration(700)
                .withEndAction(() -> binding.splashBee.animate()
                        .translationY(0f)
                        .setDuration(700)
                        .withEndAction(() -> {
                            if (!isFinishing()) {
                                binding.splashBee.animate()
                                        .translationY(-10f)
                                        .setDuration(700)
                                        .withEndAction(null)
                                        .start();
                            }
                        })
                        .start())
                .start();

        binding.splashLoadingGroup.setAlpha(0f);
        binding.splashLoadingGroup.animate()
                .alpha(1f)
                .setStartDelay(700)
                .setDuration(400)
                .start();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(launchRunnable);
        super.onDestroy();
    }
}
