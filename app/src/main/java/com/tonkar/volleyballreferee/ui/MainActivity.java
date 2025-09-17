package com.tonkar.volleyballreferee.ui;

import android.widget.FrameLayout;
import android.view.ViewGroup;

import com.tonkar.volleyballreferee.ui.rotation.RotationOverlayView;
import com.tonkar.volleyballreferee.engine.rotation.RotationQrSupport;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.tonkar.volleyballreferee.ui.rotation.RotationQrScanActivity;

import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.*;

import androidx.annotation.IdRes;
import androidx.appcompat.app.*;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationBarView;
import com.tonkar.volleyballreferee.*;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.worker.SyncWorker;
import com.tonkar.volleyballreferee.ui.onboarding.MainOnboardingActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    private RotationOverlayView rotationOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SyncWorker.enqueue(getApplicationContext());

        showOnboarding();

        super.onCreate(savedInstanceState);

        Log.i(Tags.MAIN_UI, "Create main activity");
        setContentView(R.layout.activity_main);
        FrameLayout root = (FrameLayout) findViewById(android.R.id.content);
        rotationOverlay = new RotationOverlayView(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rotationOverlay.setLayoutParams(lp);
        root.addView(rotationOverlay);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        NavigationBarView navigationView = findViewById(R.id.main_navigation_view);
        navigationView.setOnItemSelectedListener(item -> {
            navigateToFragment(item.getItemId());
            return true;
        });

        getNavigationController().ifPresent(
                navController -> navController.addOnDestinationChangedListener((argNavController, navDestination, bundle) -> {
                    if (List
                            .of(R.id.home_fragment, R.id.scheduled_games_list_fragment, R.id.user_fragment)
                            .contains(navDestination.getId())) {
                        MenuItem menuItem = navigationView.getMenu().findItem(navDestination.getId());
                        menuItem.setChecked(true);
                    } else {
                        MenuItem menuItem = navigationView.getMenu().findItem(R.id.navigation_fragment);
                        menuItem.setChecked(true);
                    }
                }));

        Intent intent = getIntent();
        @IdRes int initFragmentId = intent.getIntExtra(UiUtils.INIT_FRAGMENT_ID, 0);

        if (initFragmentId != 0) {
            navigateToFragment(initFragmentId);
        }

        showReleaseNotes();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan_rotation) {
            startActivity(new Intent(this, RotationQrScanActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onResume() {
       super.onResume();
        java.util.List<String> home = RotationQrSupport.getHome(this);
        java.util.List<String> away = RotationQrSupport.getAway(this);
        if (rotationOverlay != null) rotationOverlay.setData(home, away);
    }

    private void navigateToFragment(@IdRes int fragmentId) {
        getNavigationController().ifPresent(navigationController -> navigationController.navigate(fragmentId));
    }

    private Optional<NavController> getNavigationController() {
        NavHostFragment navigationHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_container_view);
        return Optional.ofNullable(navigationHostFragment).map(NavHostFragment::getNavController);
    }

    private void showReleaseNotes() {
        String releaseNotesKey = String.format("rn_%s", BuildConfig.VERSION_CODE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.getBoolean(releaseNotesKey, false)) {
            try {
                int resourceId = getResources().getIdentifier(releaseNotesKey, "string", getPackageName());

                if (resourceId > 0) {
                    String releaseNotes = getString(resourceId);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                            .setTitle(String.format("Welcome to Volleyball Referee %s", BuildConfig.VERSION_NAME))
                            .setMessage(releaseNotes)
                            .setPositiveButton(android.R.string.yes,
                                               (dialog, which) -> sharedPreferences.edit().putBoolean(releaseNotesKey, true).apply());
                    AlertDialog alertDialog = builder.show();
                    UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
                }
            } catch (Resources.NotFoundException e) {
                Log.i(Tags.MAIN_UI, String.format("There is no release note %s", releaseNotesKey));
            }
        }
    }

    private void showOnboarding() {
        if (PrefUtils.showOnboarding(this, PrefUtils.PREF_ONBOARDING_MAIN)) {
            Intent intent = new Intent(this, MainOnboardingActivity.class);
            startActivity(intent);
        }
    }
}
