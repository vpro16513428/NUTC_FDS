package com.nutccsie.nutc_fds;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new getJson().execute();
    }

    private class getJson extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {

                URL url = new URL("http://api.thingspeak.com/channels/96545/fields/1.json?api_key=5NE777ZJOKKJ1GJT");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String jsonString= reader.readLine();
                reader.close();

                Log.d("Tag_in:",jsonString);
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}


