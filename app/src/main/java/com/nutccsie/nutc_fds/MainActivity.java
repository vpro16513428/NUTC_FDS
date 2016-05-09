package com.nutccsie.nutc_fds;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button Button3 = (Button)findViewById(R.id.button3);
        final Button Button4 = (Button)findViewById(R.id.button4);
        final Button Button5 = (Button)findViewById(R.id.button5);
        Button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });
        Button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });
        Button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset();
            }
        });//AlertDialog

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                jumptoSet();
            }
        });

        new getjson().execute();//下這一行getjson才會做動作
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }// ActionBar

    private void Reset(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you want to reset？").setPositiveButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
               dialog.cancel();
            }}).setNegativeButton("Yes",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        AlertDialog reset_dialog = builder.create();
        reset_dialog.show();
    }//AlertDialog

    public void jumptoSet(){
        setContentView(R.layout.set);
        Button button6 = (Button)findViewById(R.id.button6);
        button6.setOnClickListener(new  Button.OnClickListener(){
            @Override
            public void onClick(View view){
                jumptoMain();
            }
        });
    }
    public void jumptoMain(){
        setContentView(R.layout.activity_main);
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view){
                jumptoSet();
            }
        });

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
                percent=(float) Integer.parseInt(jsonArray.getJSONObject(jsonArray.length()-1).getString("field1"))/max*100;
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
            Button Button3 = (Button)findViewById(R.id.button3);
            Button3.setText(Button3.getText().toString()+"     "+(res[1].toString().substring(0,res[1].toString().indexOf(".")+3))+"%");
            //Button3.setText(String.format("%s%s", Button3.getText().toString(), res[1].toString()));
        }
    }

}


