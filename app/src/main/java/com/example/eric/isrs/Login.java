package com.example.eric.isrs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private EditText mUsername;
    private EditText mPassword;
    private Button mLogin;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (EditText)findViewById(R.id.login_user);
        mPassword = (EditText)findViewById(R.id.login_password);
        mLogin = (Button)findViewById(R.id.login_btn);
        mProgressBar = (ProgressBar)findViewById(R.id.login_ProgressBar);


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                validate(mUsername.getText().toString(), mPassword.getText().toString());
            }
        });
    }


    private void validate(final String userName, final String userPassword){
        //connect to server to validate login correct
        Map<String, String> params = new HashMap<String, String>();

        params.put("username", userName);
        params.put("password", userPassword);

        LoginHttp http = new LoginHttp(userName, userPassword, this);
        http.execute(params);
    }

    public class LoginHttp extends AsyncHttp{

        private String userName;
        private String userPassword;
        private Context context;

        public LoginHttp(String un, String up, Context ct){
            this.userName = un;
            this.userPassword = up;
            this.context = ct;
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {
            return super.validateAccount(maps);
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressBar.setVisibility(View.GONE);

            if(s.equals("login_success")){
                SharedPreferences settings = getSharedPreferences("setting", MODE_PRIVATE);

                settings.edit()
                        .putString("USERNAME", userName)
                        .putString("PASSWORD", userPassword)
                        .commit();

                Toast.makeText(Login.this, "登入成功", Toast.LENGTH_LONG).show();

                Intent intent;
                intent = new Intent(context, Home.class);
                startActivity(intent);

            }else{
                Toast.makeText(Login.this, "登入失敗", Toast.LENGTH_LONG).show();
            }

        }
    }
}
