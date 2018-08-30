package com.example.eric.isrs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class About extends AppCompatActivity {

    private NavigationView mNavationgationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;

    private TextView mHeaderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //設定側邊欄
        mDrawerLayout = (DrawerLayout)findViewById(R.id.about_drawerLayout);
        mNavationgationView = (NavigationView)findViewById(R.id.about_navigation_view);

        mToolbar = (Toolbar)findViewById(R.id.about_toolbar);

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

                mDrawerLayout.closeDrawer(GravityCompat.START);

                int id = item.getItemId();

                if(id == R.id.action_home){
                    Toast.makeText(About.this, "首頁", Toast.LENGTH_LONG).show();
                    return true;
                }else if(id == R.id.action_about) {
                    Toast.makeText(About.this, "關於", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(About.this, About.class);
                    startActivity(intent);

                    return true;
                }else if(id == R.id.action_help){
                    Toast.makeText(About.this, "幫忙", Toast.LENGTH_LONG).show();
                    return true;
                }else if(id == R.id.action_logout){
                    Toast.makeText(About.this, "登出", Toast.LENGTH_LONG).show();

                    SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);
                    SharedPreferences.Editor ed = settings.edit();

                    ed.clear().commit();

                    Intent intent;
                    intent = new Intent(About.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }

                return false;
            }
        });
    }
}
