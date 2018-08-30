package com.example.eric.isrs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

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

        /**
         * Runs on the UI thread after {@link #publishProgress} is invoked.
         * The specified values are the values passed to {@link #publishProgress}.
         *
         * @param values The values indicating progress.
         * @see #publishProgress
         * @see #doInBackground
         */
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
