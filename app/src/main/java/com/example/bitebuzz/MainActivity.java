package com.example.bitebuzz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.bitebuzz.data.AppRepository;
import com.example.bitebuzz.databinding.ActivityMainBinding;
import com.example.bitebuzz.fragment.CartFragment;
import com.example.bitebuzz.fragment.ExploreFragment;
import com.example.bitebuzz.fragment.FavoritesFragment;
import com.example.bitebuzz.fragment.HomeFragment;
import com.example.bitebuzz.util.UiUtils;
import com.google.android.material.badge.BadgeDrawable;

/**
 * Hosts the four main tabs and keeps their state alive with show/hide.
 */
public class MainActivity extends AppCompatActivity implements AppRepository.DataListener {

    public static final String EXTRA_TAB = "extra_tab";
    public static final int TAB_HOME = 0;
    public static final int TAB_EXPLORE = 1;
    public static final int TAB_FAVORITES = 2;
    public static final int TAB_CART = 3;
    private static final String STATE_TAB = "state_tab";

    private static final String[] TAGS = {"home", "explore", "favorites", "cart"};

    private ActivityMainBinding binding;
    private final Fragment[] fragments = new Fragment[4];
    private int currentTab = TAB_HOME;
    private AppRepository repository;

    public static Intent createIntent(Context context, int tab) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_TAB, tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = AppRepository.getInstance(this);
        UiUtils.applyMainInsets(binding.mainRoot, binding.fragmentContainer, binding.bottomNav);

        int desiredTab = savedInstanceState == null
                ? getIntent().getIntExtra(EXTRA_TAB, TAB_HOME)
                : savedInstanceState.getInt(STATE_TAB, TAB_HOME);
        currentTab = clampTab(desiredTab);

        createFragments();
        showTab(currentTab);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int tab = tabForMenuId(item.getItemId());
            if (tab >= 0) {
                currentTab = tab;
                showTab(tab);
                return true;
            }
            return false;
        });
        binding.bottomNav.setSelectedItemId(menuIdFor(currentTab));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (currentTab != TAB_HOME) {
                    binding.bottomNav.setSelectedItemId(R.id.nav_home);
                } else {
                    finish();
                }
            }
        });
    }

    private void createFragments() {
        FragmentManager manager = getSupportFragmentManager();
        fragments[TAB_HOME] = manager.findFragmentByTag(TAGS[TAB_HOME]);
        fragments[TAB_EXPLORE] = manager.findFragmentByTag(TAGS[TAB_EXPLORE]);
        fragments[TAB_FAVORITES] = manager.findFragmentByTag(TAGS[TAB_FAVORITES]);
        fragments[TAB_CART] = manager.findFragmentByTag(TAGS[TAB_CART]);

        if (fragments[TAB_HOME] == null) {
            fragments[TAB_HOME] = new HomeFragment();
        }
        if (fragments[TAB_EXPLORE] == null) {
            fragments[TAB_EXPLORE] = new ExploreFragment();
        }
        if (fragments[TAB_FAVORITES] == null) {
            fragments[TAB_FAVORITES] = new FavoritesFragment();
        }
        if (fragments[TAB_CART] == null) {
            fragments[TAB_CART] = new CartFragment();
        }

        androidx.fragment.app.FragmentTransaction transaction = manager.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (!fragments[i].isAdded()) {
                transaction.add(R.id.fragmentContainer, fragments[i], TAGS[i]);
            }
        }
        transaction.commitNow();
    }

    private void showTab(int tab) {
        if (fragments[tab] == null) {
            return;
        }
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == tab) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        transaction.commit();
        currentTab = tab;
    }

    /**
     * Lets fragments switch tabs (used by Home shortcuts and empty-state buttons).
     */
    public void selectTab(int tab) {
        int safeTab = clampTab(tab);
        int menuId = menuIdFor(safeTab);
        if (binding.bottomNav.getSelectedItemId() == menuId) {
            showTab(safeTab);
        } else {
            binding.bottomNav.setSelectedItemId(menuId);
        }
    }

    private int clampTab(int tab) {
        return Math.max(TAB_HOME, Math.min(TAB_CART, tab));
    }

    private int menuIdFor(int tab) {
        switch (tab) {
            case TAB_EXPLORE:
                return R.id.nav_explore;
            case TAB_FAVORITES:
                return R.id.nav_favorites;
            case TAB_CART:
                return R.id.nav_cart;
            case TAB_HOME:
            default:
                return R.id.nav_home;
        }
    }

    private int tabForMenuId(int id) {
        if (id == R.id.nav_home) {
            return TAB_HOME;
        }
        if (id == R.id.nav_explore) {
            return TAB_EXPLORE;
        }
        if (id == R.id.nav_favorites) {
            return TAB_FAVORITES;
        }
        if (id == R.id.nav_cart) {
            return TAB_CART;
        }
        return -1;
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int tab = clampTab(intent.getIntExtra(EXTRA_TAB, TAB_HOME));
        binding.bottomNav.setSelectedItemId(menuIdFor(tab));
    }

    @Override
    protected void onStart() {
        super.onStart();
        repository.addListener(this);
        updateCartBadge();
    }

    @Override
    protected void onStop() {
        repository.removeListener(this);
        super.onStop();
    }

    @Override
    public void onDataChanged() {
        updateCartBadge();
    }

    private void updateCartBadge() {
        int count = repository.getCartCount();
        BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_cart);
        badge.setVisible(count > 0);
        badge.setNumber(count);
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
        badge.setMaxCharacterCount(3);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(STATE_TAB, currentTab);
        super.onSaveInstanceState(outState);
    }
}
