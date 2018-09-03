package com.example.eric.isrs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

@SuppressLint("ValidFragment")
public class FragmentSheet extends Fragment {
    View v;
    private ProgressBar mProgressBar;
    private String mUserName;

    @SuppressLint("ValidFragment")
    public FragmentSheet(String un){
        this.mUserName = un;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar)getView().findViewById(R.id.home_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);

        LinearLayout layout = (LinearLayout)getView().findViewById(R.id.home_linearlayout);


        Homehttp http = new Homehttp(mUserName, layout, mProgressBar);
        http.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_sheet, container, false);

        return v;
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
                final JSONArray ids = jsonInfo.getJSONArray("ids");
                final JSONArray titles = jsonInfo.getJSONArray("titles");

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

                    ll[i] = new LinearLayout(getActivity());

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

                    final int finalI = i;
                    ll[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                recognition(v, ids.getInt(finalI), titles.getString(finalI));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
                    TextView topic = new TextView(getActivity());

                    topic.setText("\n題目："+titles.getString(i));
                    topic.setTextSize(25);
                    topic.setGravity(Gravity.CENTER_VERTICAL);

                    TextView num = new TextView(getActivity());
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

    public void recognition(View view, int sheetID, String sheetTitle){
        //snapshot
        Intent intent;
        intent = new Intent(getActivity(), Recognition.class);
        intent.putExtra("SHEET_ID", sheetID);
        intent.putExtra("SHEET_TITLES", sheetTitle);
        startActivity(intent);
    }
}
