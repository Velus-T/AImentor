package com.example.aimentor.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.aimentor.R;
import com.example.aimentor.adapters.ViewPagerAdapter;
import com.example.aimentor.science.presentation.ScienceActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

public class MenuActivity extends com.example.aimentor.ui.InsetActivity implements NavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ViewPager2 viewPager;
    NavigationView navigationView;
    Menu menu;
    MenuItem menuItemLogout;
    SharedPreferences sharedPrf;
    Intent dataIntent;
    Bundle dataBundle;
    private String account = "";
    private int    userId = 0;

    private void activeMenu(Intent intent, Bundle bundle){
        intent = getIntent();
        bundle = intent.getExtras();
        if (bundle != null) {
            String menuName = bundle.getString("MENU_TAB", "").toLowerCase();
            if (menuName.equals("category")){
                viewPager.setCurrentItem(1);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // check user login.
        if (userId < 0 || TextUtils.isEmpty(account)){
            Intent checkLogin = new Intent(MenuActivity.this, LoginActivity.class);
            startActivity(checkLogin);
            finish();
        }
        activeMenu(dataIntent, dataBundle);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        navigationView = findViewById(R.id.drawerNavigation);
        menu = navigationView.getMenu();
        menuItemLogout = menu.findItem(R.id.logout_menu);
        dataIntent = getIntent();
        dataBundle = dataIntent.getExtras();
        sharedPrf = getSharedPreferences("USER_INFO", MODE_PRIVATE);
        account = sharedPrf.getString("USERNAME_USER", "");
        userId  = sharedPrf.getInt("ID_USER", 0);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AI Mentor");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        // click Item drawer menu
        navigationView.setNavigationItemSelectedListener(this);
        // call view pager
        setupViewPager();
        // click tab
        clickTabNavigation();
        // logout
        Logout();
        // hien thi thong tin dang nhap
        MenuItem itemAccount = menu.findItem(R.id.account_menu);
        if (account != null) {
            itemAccount.setTitle(account);
        }
    }
    private void Logout(){
        menuItemLogout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawer(GravityCompat.START); // close
                // xoa du lieu Bundle
                if (dataBundle != null){
                    dataIntent.removeExtra("ID_ACCOUNT");
                    dataIntent.removeExtra("USER_ACCOUNT");
                    dataIntent.removeExtra("EMAIL_ACCOUNT");
                }
                // xoa du lieu sharePrf
                SharedPreferences.Editor editor = sharedPrf.edit();
                editor.clear();
                editor.apply();

                Intent login = new Intent(MenuActivity.this, LoginActivity.class);
                startActivity(login);
                finish();// khong cho back lai
                return false;
            }
        });
    }
    private void clickTabNavigation(){
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.home_menu) {
                    viewPager.setCurrentItem(0); // Home - position : 0
                } else if (menuItem.getItemId() == R.id.Category_menu) {
                    viewPager.setCurrentItem(1); // Category
                } else if (menuItem.getItemId() == R.id.Quiz_menu) {
                    viewPager.setCurrentItem(2); // Quiz
                } else if (menuItem.getItemId() == R.id.science_menu) {
            Intent intent = new Intent(MenuActivity.this, ScienceActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.Settings_menu) {
                    viewPager.setCurrentItem(3); // Settings
                }
                return true;
            }
        });
    }
    private void setupViewPager(){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0){
                    bottomNavigationView.getMenu().findItem(R.id.home_menu).setChecked(true);
                } else if (position == 1) {
                    bottomNavigationView.getMenu().findItem(R.id.Category_menu).setChecked(true);
                } else if (position == 2) {
                    bottomNavigationView.getMenu().findItem(R.id.Quiz_menu).setChecked(true);
                } else if (position == 3) {
                    bottomNavigationView.getMenu().findItem(R.id.Settings_menu).setChecked(true);
                } else {
                    bottomNavigationView.getMenu().findItem(R.id.home_menu).setChecked(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.home_menu) {
            viewPager.setCurrentItem(0); // Home - position : 0
        } else if (menuItem.getItemId() == R.id.Category_menu) {
            viewPager.setCurrentItem(1); // Category
        } else if (menuItem.getItemId() == R.id.Quiz_menu) {
            viewPager.setCurrentItem(2); // Quiz
        } else if (menuItem.getItemId() == R.id.science_menu) {
            Intent intent = new Intent(MenuActivity.this, ScienceActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.Settings_menu) {
            viewPager.setCurrentItem(3); // Settings
        } else {
            viewPager.setCurrentItem(0); // Home - position : 0
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
