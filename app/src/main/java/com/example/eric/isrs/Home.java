package com.example.eric.isrs;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class Home extends AppCompatActivity {

    private NavigationView mNavationgationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;

    private View mHeader;

    private TextView mHeaderText;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //設定側邊欄
        mDrawerLayout = (DrawerLayout)findViewById(R.id.home_drawerLayout);
        mNavationgationView = (NavigationView)findViewById(R.id.home_navigation_view);

        mToolbar = (Toolbar)findViewById(R.id.home_toolbar);

        this.setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mHeaderText = mNavationgationView.getHeaderView(0).findViewById(R.id.home_txtHeader);
        SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);
        String mUserName = settings.getString("USERNAME", "failed");

        mHeaderText.setText(mUserName);


        mNavationgationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                displaySelectedScreen(id);
                return true;
            }
        });

        displaySelectedScreen(R.id.action_home);

    }


    private void displaySelectedScreen(int id){
        android.support.v4.app.Fragment fragment = null;

        switch (id){
            case R.id.action_about:
                fragment = new FragmentAbout();
                break;
            case R.id.action_help:
                fragment = new FragmentUse();
                break;
            case R.id.action_home:
                fragment = new FragmentSheet();
                break;
            case R.id.action_logout:
                SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);
                SharedPreferences.Editor ed = settings.edit();
                ed.clear().commit();

                Intent intent;
                intent = new Intent(Home.this, MainActivity.class);
                finish();
                startActivity(intent);
                break;
        }

        if(fragment != null){
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.home_content, fragment);
            ft.commit();
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

}
