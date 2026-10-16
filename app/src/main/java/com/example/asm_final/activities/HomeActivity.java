package com.example.asm_final.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.asm_final.R;
import com.example.asm_final.activities.database.DatabaseHelper;
import com.example.asm_final.activities.fragments.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ProfileFragment.OnProfileUpdatedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNav;
    private String username;
    private int destination = R.id.nav_home;
    private boolean syncing;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        username = getIntent().getStringExtra("USERNAME");
        if(username == null || username.trim().isEmpty()) { logout(); return; }
        setContentView(R.layout.activity_home);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.nav_view);
        bottomNav=findViewById(R.id.bottom_navigation);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);toggle.syncState();toggle.getDrawerArrowDrawable().setColor(android.graphics.Color.WHITE);
        bottomNav.setOnItemSelectedListener(item -> { if(!syncing) navigateTo(item.getItemId(),false); return true; });
        getSupportFragmentManager().addOnBackStackChangedListener(this::syncNavigation);
        if(state==null) navigateTo(R.id.nav_home,false);
        else { destination=state.getInt("shell_destination",R.id.nav_home); syncNavigation(); }
        updateHeader();
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if(drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
                else if(getSupportFragmentManager().getBackStackEntryCount()>0) getSupportFragmentManager().popBackStack();
                else if(destination!=R.id.nav_home) navigateTo(R.id.nav_home,false);
                else finish();
            }
        });
    }
    public void navigateTo(int id, boolean remember) {
        Fragment fragment;
        if(id==R.id.nav_home) fragment=new HomeFragment();
        else if(id==R.id.nav_category) fragment=new CategoryFragment();
        else if(id==R.id.nav_profile) fragment=new ProfileHubFragment();
        else if(id==R.id.nav_edit_profile) fragment=new ProfileFragment();
        else if(id==R.id.nav_settings) fragment=new SettingsFragment();
        else return;
        Bundle args=new Bundle();args.putString("USERNAME",username);fragment.setArguments(args);
        FragmentManager manager=getSupportFragmentManager();
        if(!remember) manager.popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
        androidx.fragment.app.FragmentTransaction transaction=manager.beginTransaction().replace(R.id.fragment_container,fragment,"destination:"+id);
        if(remember) transaction.addToBackStack("destination:"+destination);
        transaction.commit();destination=id;syncNavigationSelection();
    }
    private void syncNavigation() {
        Fragment current=getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(current instanceof HomeFragment) destination=R.id.nav_home;
        else if(current instanceof CategoryFragment) destination=R.id.nav_category;
        else if(current instanceof ProfileHubFragment) destination=R.id.nav_profile;
        else if(current instanceof ProfileFragment) destination=R.id.nav_edit_profile;
        else if(current instanceof SettingsFragment) destination=R.id.nav_settings;
        syncNavigationSelection();
    }
    private void syncNavigationSelection() {
        syncing=true;
        navigationView.setCheckedItem(destination==R.id.nav_edit_profile?R.id.nav_profile:destination);
        // Profile is owned by Settings in bottom navigation, and remains selected correctly on back.
        bottomNav.getMenu().findItem(destination==R.id.nav_settings||destination==R.id.nav_edit_profile?R.id.nav_profile:destination).setChecked(true);
        syncing=false;
    }
    @Override protected void onSaveInstanceState(Bundle out) { out.putInt("shell_destination",destination);super.onSaveInstanceState(out); }
    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_logout) logout();else navigateTo(item.getItemId(),false);
        drawerLayout.closeDrawer(GravityCompat.START);return true;
    }
    public void logout() {
        com.example.mentor.data.ActiveUser.clear(this);
        startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
    @Override public void onProfileUpdated() { updateHeader(); }
    private void updateHeader() {
        View header=navigationView.getHeaderView(0);
        TextView name=header.findViewById(R.id.txtUserHeader);
        ImageView avatar=header.findViewById(R.id.imgUserHeader);
        if(name!=null) name.setText(username);
        if(avatar!=null) avatar.setImageResource(R.mipmap.ic_launcher_round);
        try(DatabaseHelper db=new DatabaseHelper(this);Cursor cursor=db.getUserData(username)) {
            if(cursor==null||!cursor.moveToFirst()) return;
            String fullName=cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FULLNAME));
            if(name!=null&&fullName!=null&&!fullName.isEmpty()) name.setText(fullName);
            String uri=cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AVATAR));
            if(avatar!=null&&uri!=null&&!uri.isEmpty()) {
                try { avatar.setImageURI(Uri.parse(uri)); } catch(RuntimeException ignored) { avatar.setImageResource(R.mipmap.ic_launcher_round); }
            }
        }
    }
}
