package com.example.eric.isrs;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private Button mButtonSend;
    private Button mButtonSignup;

    private TextView mWelcomeText;

    private String mUserName;
    private String mPassWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar)findViewById(R.id.main_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        mButtonSend = (Button)findViewById(R.id.main_startbtn);
        mButtonSignup = (Button)findViewById(R.id.main_signbtn);

        mWelcomeText = (TextView)findViewById(R.id.main_weltext);
        mWelcomeText.setGravity(Gravity.CENTER_HORIZONTAL);

        SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);
        mUserName = settings.getString("USERNAME", "failed");
        mPassWord = settings.getString("PASSWORD", "failed");



        Map<String, String> params = new HashMap<String, String>();
        params.put("username", mUserName);
        params.put("password", mPassWord);

        Mainhttp http = new Mainhttp(mButtonSend, mButtonSignup, mProgressBar, mWelcomeText, mUserName);
        http.execute(params);
    }

    public void logIn(View view){
        Intent intent;
        intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void start(View view){
        Intent intent;
        intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    public class Mainhttp extends AsyncHttp{

        private Button mButtonSend;
        private Button mButtonSignup;
        private ProgressBar mProgressBar;
        private TextView mWelcomeText;
        private String mUserName;


        public Mainhttp(Button btn, Button btn1,ProgressBar pb, TextView tv, String un){
            this.mProgressBar = pb;
            this.mButtonSend = btn;
            this.mButtonSignup = btn1;
            this.mWelcomeText = tv;
            this.mUserName = un;
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            String re =  super.validateAccount(maps);
            return re;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String post_result) {
            super.onPostExecute(post_result);
            mProgressBar.setVisibility(View.GONE);

            if(post_result.equals("login_success")){

                mButtonSignup.setText("Logout");
                mButtonSignup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);
                        SharedPreferences.Editor ed = settings.edit();

                        ed.clear().commit();

                        //從開
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

                mButtonSend.setText("Start");
                mButtonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        start(v);
                    }
                });

                mWelcomeText.setText("Welcome, "+mUserName);
            }else{
                mButtonSend.setText("Login");
                mButtonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logIn(v);
                    }
                });

                mWelcomeText.setText("Welcome, please log in");
            }
        }
    }

}
