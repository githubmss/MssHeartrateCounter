package com.mss.heartrate.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.mss.heartrate.utils.AppPreferences;
import com.mss.heartrate.R;

public class ShowBPMActivity extends Activity {

    int a = 1;

    ImageView image;
    ArrayList<Integer> myBeat;

    public static enum TYPE {
        GREEN, RED
    }

    ;

    TextView txtBeat;

    private static TYPE currentType = TYPE.RED;

    public static TYPE getCurrent() {
        return currentType;
    }

    @SuppressWarnings("static-access")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showbpm);
//        getActionBar().hide();
        // new Abc(image).execute("");
        initUI();
    }

    private void initUI() {
        txtBeat = (TextView) findViewById(R.id.txt_avgbeat);
        image = (ImageView) findViewById(R.id.image);
        Typeface myTypeface = Typeface.createFromAsset(this.getAssets(),
                "digital.ttf");
        txtBeat.setTypeface(myTypeface);
        int sum = 0;
        @SuppressWarnings("static-access")
        int Beat = new AppPreferences(getApplicationContext())
                .getPreferenceRelaod("Beatavg", 0);
        txtBeat.setText("Value of Heartrate in bpm :-" + Beat);
        runonUIthread();
        myBeat = new AppPreferences(getApplicationContext()).getArray("Mylist");
        if (myBeat.size() > 0) {
            for (int i = 0; i < myBeat.size(); i++) {
                Log.e("Sizee", String.valueOf(myBeat.get(i)));
                sum = sum + myBeat.get(i);
                double average = sum / myBeat.size();
                Log.e("Sumaverage", String.valueOf(average));
            }
        }
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
        super.onBackPressed();
    }

    private void runonUIthread() {

        new Thread() {
            @Override
            public void run() {
                super.run();

                while (true) {
                    try {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (a == 1) {
                                    image.setBackgroundResource(R.drawable.bigred);
                                    a = 0;

                                } else if (a == 0) {

                                    image.setBackgroundResource(R.drawable.bigdarkred);
                                    a = 1;
                                }

                            }
                        });
                        Thread.sleep(300);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                }

            }

        }.start();

    }

}