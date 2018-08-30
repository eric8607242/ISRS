package com.example.eric.isrs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
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

    private ProgressBar mProgressBar;
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

        mProgressBar = (ProgressBar)findViewById(R.id.home_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        LinearLayout layout = (LinearLayout)findViewById(R.id.home_linearlayout);


        Homehttp http = new Homehttp("123", layout, mProgressBar);
        http.execute();


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

                mDrawerLayout.closeDrawer(GravityCompat.START);

                int id = item.getItemId();

                if(id == R.id.action_home){
                    Toast.makeText(Home.this, "首頁", Toast.LENGTH_LONG).show();
                    return true;
                }else if(id == R.id.action_about) {
                    Toast.makeText(Home.this, "關於", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Home.this, About.class);
                    startActivity(intent);

                    return true;
                }else if(id == R.id.action_help){
                    Toast.makeText(Home.this, "幫忙", Toast.LENGTH_LONG).show();
                    return true;
                }else if(id == R.id.action_logout){
                    Toast.makeText(Home.this, "登出", Toast.LENGTH_LONG).show();

                    SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);
                    SharedPreferences.Editor ed = settings.edit();

                    ed.clear().commit();

                    Intent intent;
                    intent = new Intent(Home.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }

                return false;
            }
        });

    }

    public void recognition(View view){
        //snapshot
        Intent intent;
        intent = new Intent(this, Recognition.class);
        startActivity(intent);
    }


    public class Homehttp extends AsyncHttp{

        private String username;
        private ProgressBar mProgressBar;
        private LinearLayout layout;

        public Homehttp(String un, LinearLayout ll, ProgressBar pb){
            this.username = un;
            this.layout = ll;
            this.mProgressBar = pb;
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            return super.getSheetInfo(username);
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param s The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            mProgressBar.setVisibility(View.GONE);

            try {
                JSONObject jsonInfo = new JSONObject(s);
                JSONArray ids = jsonInfo.getJSONArray("ids");
                JSONArray titles = jsonInfo.getJSONArray("titles");

                //set LinearLayout property
                LinearLayout.LayoutParams layoutParams;
                layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                layoutParams.setMargins(0,10,0,10);

                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                LinearLayout ll[] = new LinearLayout[ids.length()];

                for(int i = 0 ; i < ids.length() ; i ++){

                    ll[i] = new LinearLayout(Home.this);

                    ll[i].setGravity(Gravity.CENTER);
                    ll[i].setOrientation(LinearLayout.VERTICAL);
                    ll[i].setId(i);
                    layout.addView(ll[i], layoutParams);

                    //set Block shape
                    final GradientDrawable border = new GradientDrawable();
                    border.setColor(0xFF4F77A1); //white background
                    border.setStroke(1, Color.BLACK); //black border with full opacity
                    border.setAlpha(150);

                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        ll[i].setBackgroundDrawable(border);
                    } else {
                        ll[i].setBackground(border);
                    }

                    ll[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            recognition(v);
                        }
                    });

                    ll[i].setOnHoverListener(new View.OnHoverListener() {
                        @Override
                        public boolean onHover(View v, MotionEvent event) {
                            v.setBackgroundColor(Color.BLACK);
                            return false;
                        }
                    });

                    //add Textview in LinearLayout
                    TextView topic = new TextView(Home.this);

                    topic.setText("\n題目："+titles.getString(i));
                    topic.setTextSize(25);
                    topic.setGravity(Gravity.CENTER_VERTICAL);

                    TextView num = new TextView(Home.this);
                    num.setText("編號："+ String.valueOf(ids.getInt(i))+"\n");
                    num.setTextSize(20);
                    num.setGravity(Gravity.CENTER_VERTICAL);

                    ll[i].addView(topic, textParams);
                    ll[i].addView(num, textParams);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
