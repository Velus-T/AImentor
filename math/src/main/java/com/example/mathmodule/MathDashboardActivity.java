package com.example.mathmodule;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.activity.OnBackPressedCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.mathmodule.fragments.*;
public class MathDashboardActivity extends AppCompatActivity {
 private BottomNavigationView navigation;
 @Override protected void onCreate(Bundle state) {
  super.onCreate(state);setContentView(R.layout.activity_math_dashboard);navigation=findViewById(R.id.bottom_navigation);
  navigation.setOnItemSelectedListener(item->{show(item.getItemId());return true;});
  if(state==null)navigation.setSelectedItemId(R.id.nav_home);
  getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true) {
   public void handleOnBackPressed() {if(navigation.getSelectedItemId()!=R.id.nav_home)navigation.setSelectedItemId(R.id.nav_home);else finish();}
  });
 }
 private void show(int id) {
  Fragment page=id==R.id.nav_history?new MathHistoryFragment():id==R.id.nav_formula?new MathFormulaFragment():id==R.id.nav_quiz?new MathQuizFragment():new MathSolveFragment();
  getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out).replace(R.id.fragment_container,page).commit();
 }
}
