package com.example.asm_final.activities;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.asm_final.R;
import com.example.asm_final.activities.database.DatabaseHelper;
import com.example.asm_final.activities.fragments.CategoryFragment;
import com.example.asm_final.activities.fragments.HomeFragment;
import com.example.asm_final.activities.fragments.ProfileFragment;
import com.example.asm_final.activities.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/*
 * Role: Activity for App Shell, responsible for the screen lifecycle, view binding, and primary user interactions.
 * Purpose: The Activity connects the UI with supporting classes while keeping responsibilities separated instead of concentrating all logic in one place.
 */
public class HomeActivity extends AppCompatActivity 
        implements NavigationView.OnNavigationItemSelectedListener, ProfileFragment.OnProfileUpdatedListener {

    private DrawerLayout drawerLayout;
    private String username;
    private DatabaseHelper dbHelper;
    private NavigationView navigationView;

    /*
     * Responsibility: Initializes the Android component, inflates and binds the layout, and prepares initial data and listeners.
     * Purpose: This is the screen entry point, so the initial state must be prepared before user interaction begins.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        updateHeader();
        updateMenuTitles();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(android.graphics.Color.WHITE);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, createHomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = createHomeFragment();
            } else if (id == R.id.nav_category) {
                selectedFragment = createCategoryFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("USERNAME", username);
                selectedFragment.setArguments(bundle);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }


    /*
     * Responsibility: Creates the object or data structure for `create home fragment` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private Fragment createHomeFragment() {
        HomeFragment fragment = new HomeFragment();
        Bundle arguments = new Bundle();
        arguments.putString("USERNAME", username);
        fragment.setArguments(arguments);
        return fragment;
    }

    /*
     * Responsibility: Creates the object or data structure for `create category fragment` using the class's consistent creation rules.
     * Purpose: Encapsulating creation rules prevents callers from repeating the same initialization steps.
     */
    private Fragment createCategoryFragment() {
        CategoryFragment fragment = new CategoryFragment();
        Bundle arguments = new Bundle();
        arguments.putString("USERNAME", username);
        fragment.setArguments(arguments);
        return fragment;
    }

    /*
     * Responsibility: Updates or synchronizes `update menu titles` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateMenuTitles() {
        // Cập nhật text cho menu khi đổi ngôn ngữ
        navigationView.getMenu().findItem(R.id.nav_home).setTitle(R.string.menu_home);
        navigationView.getMenu().findItem(R.id.nav_category).setTitle(R.string.menu_category);
        navigationView.getMenu().findItem(R.id.nav_profile).setTitle(R.string.menu_profile);
        navigationView.getMenu().findItem(R.id.nav_settings).setTitle(R.string.menu_settings);
        navigationView.getMenu().findItem(R.id.nav_logout).setTitle(R.string.menu_logout);
    }

    /*
     * Responsibility: Updates or synchronizes `update header` with the latest data or state.
     * Purpose: Keeps the UI and data synchronized after user actions.
     */
    private void updateHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView txtUserHeader = headerView.findViewById(R.id.txtUserHeader);
        ImageView imgUserHeader = headerView.findViewById(R.id.imgUserHeader);

        Cursor cursor = dbHelper.getUserData(username);
        if (cursor != null && cursor.moveToFirst()) {
            int fullNameIdx = cursor.getColumnIndex(DatabaseHelper.COL_FULLNAME);
            int avatarIdx = cursor.getColumnIndex(DatabaseHelper.COL_AVATAR);

            String displayName = username;
            if (fullNameIdx != -1) {
                String fullName = cursor.getString(fullNameIdx);
                if (fullName != null && !fullName.isEmpty()) {
                    displayName = fullName;
                }
            }
            
            if (txtUserHeader != null) {
                txtUserHeader.setText(displayName);
            }

            if (imgUserHeader != null && avatarIdx != -1) {
                String avatarUri = cursor.getString(avatarIdx);
                if (avatarUri != null && !avatarUri.isEmpty()) {
                    try {
                        imgUserHeader.setImageURI(Uri.parse(avatarUri));
                    } catch (Exception e) {
                        imgUserHeader.setImageResource(R.mipmap.ic_launcher_round);
                    }
                }
            }
            cursor.close();
        }
    }

    /*
     * Responsibility: Handles the Android callback or event `onProfileUpdated` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onProfileUpdated() {
        updateHeader();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            selectedFragment = createHomeFragment();
        } else if (id == R.id.nav_category) {
            selectedFragment = createCategoryFragment();
        } else if (id == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putString("USERNAME", username);
            selectedFragment.setArguments(bundle);
        } else if (id == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("USERNAME", username);
            selectedFragment.setArguments(bundle);
        } else if (id == R.id.nav_logout) {
            finish();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
     * Responsibility: Handles the Android callback or event `onBackPressed` and updates the related state.
     * Purpose: Keeping callbacks separate makes event flow clearer and follows the Android lifecycle cleanly.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}