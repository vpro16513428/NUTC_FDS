package com.nutccsie.nutc_fds;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new getjson().execute();//下這一行getjson才會做動作
    }
    protected class getjson extends AsyncTask<Void, Void, Object[]> {
        @Override
        protected Object[] doInBackground(Void... params) {
            try {
                //region 從thinkspeak獲取JSON資料(純String)存成JSONArray的型態在jsonArray
                URL url = new URL("http://api.thingspeak.com/channels/96545/fields/1.json?api_key=5NE777ZJOKKJ1GJT");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String jsonString= reader.readLine();
                reader.close();
                JSONArray jsonArray =new  JSONObject(jsonString).getJSONArray("feeds");//取得feeds的陣列

                int max=0;//最大
                float percent;//百分比

                //掃過所有feed取得最大值 和最大值除以最後一個feed值的百分比
                for (int i = 0; i < jsonArray.length();i++){
                    if (Integer.parseInt(jsonArray.getJSONObject(i).getString("field1")) > max) {
                        max=Integer.parseInt(jsonArray.getJSONObject(i).getString("field1"));
                    }
                }
                percent=(float) Integer.parseInt(jsonArray.getJSONObject(jsonArray.length()-1).getString("field1"))/max;
                //endregion

                //Log.d("Tag_in:",jsonArray.getJSONObject(1).getString("field1"));
                Object[] res = new Object[2];
                res[0]=max;
                res[1]=percent;
                return res;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object[] res) {
            super.onPostExecute(res);
            //res[0] 是最大值 ->Integer型態
            //res[1] 是最後一個feed值和最大值得相除 ->float型態
            Log.d("max=",res[0].toString());
            Log.d("percent=",res[1].toString());
            //可以在這裡使用res[0]和res[1]去改變UI的值
            
        }
    }

}


