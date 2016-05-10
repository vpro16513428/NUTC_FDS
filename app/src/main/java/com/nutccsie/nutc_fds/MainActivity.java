package com.nutccsie.nutc_fds;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText inputText;
    private ListView listinput;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputText =(EditText)findViewById(R.id.edit);
        listinput = (ListView)findViewById(R.id.listView);
        item = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,item);
        listinput.setAdapter(adapter);
        //AlertDialog

        new getjson().execute();//下這一行getjson才會做動作
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }// ActionBar

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_add:
                openOptionsDialog();
                return true;
            case R.id.action_delete:

                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }//讓ActionBar按鈕有動作

    private void openOptionsDialog(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.sec_activity, null);
        final EditText inputText = (EditText)v.findViewById(R.id.edit);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("請輸入名稱或代稱")
                .setView(v)
                .setPositiveButton("取消",
                        new  DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                dialog.cancel();
                            }
                        }).setNegativeButton("確定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!inputText.getText().toString().equals("")){
                            item.add(inputText.getText().toString());
                            inputText.setText("");
                            listinput.setAdapter(adapter);
                        }
                    }
                }).create().show();
    }//用AlertDialog的方式以EditText新增到Button

    public EditText getInputText() {
        return inputText;
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
        }
    }

}

