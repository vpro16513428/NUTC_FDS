package com.nutccsie.nutc_fds;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    int channel_total=0;
    int[][] channel_info;// [0] ID , [1] MAX , [2] LAST , [3] Percent

    private EditText inputText;
    private ListView listinput;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> item;
    int count = 0 , y=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                openadd();
                return true;
            case R.id.action_delete:
                opendelete();
                return true;
            case R.id.action_settings:
                opensetting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }//讓ActionBar按鈕有動作

    private void openadd(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.add_activity, null);
        final EditText inputText = (EditText)v.findViewById(R.id.edit);
        final EditText inputText2 = (EditText)v.findViewById(R.id.edit2);
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("新增")
                .setView(v)
                .setPositiveButton("取消",
                        new  DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                dialog.cancel();
                            }
                        })
                    .setNegativeButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!inputText.getText().toString().equals("")){
                                    item.add(inputText.getText().toString()+"        "+channel_info[Integer.parseInt(inputText2.getText().toString())][3] + "%");
                                    inputText.setText("");
                                    listinput.setAdapter(adapter);
                                    ++count;
                                }
                            }
                        })
                    .create().show();
    }//用AlertDialog的方式以EditText新增到listview

    public EditText getInputText() {
        return inputText;
    }

    private void opendelete(){
        final String[] str = new String[count];
        for(int i=0; i<count; i++) {
            str[i] = item.get(i);
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("刪除")
                .setSingleChoiceItems(str, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        y = which;
                    }
                })
                .setPositiveButton("取消",
                        new  DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                item.remove(y);
                                adapter.notifyDataSetChanged();
                                count--;
                            }
                        })
                .show();

    }//用AlertDialog的方式以EditText新增到listview

    private void opensetting(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.setting_activity, null);
        final TextView text = (TextView) v.findViewById(R.id.textView);
        final SeekBar sweekbar = (SeekBar)v.findViewById(R.id.seekBar);
        final TextView text2 = (TextView) v.findViewById(R.id.textView2);
        final SeekBar sweekbar2 = (SeekBar)v.findViewById(R.id.seekBar2);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("設定")
                .setView(v)
                .setPositiveButton("取消",
                        new  DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                        })
                .create().show();
    }//set使用AlertDialog的方式顯示SeekBar



    protected class getjson extends AsyncTask<Void, Void, Object[]> {
        @Override
        protected Object[] doInBackground(Void... params) {
            try {

                URL url = new URL("https://api.thingspeak.com/users/vpro16513428/channels.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String jsonString= reader.readLine();
                reader.close();
                conn.disconnect();

                JSONArray channels_jsonArray =new JSONObject(jsonString).getJSONArray("channels");//取得 channels 列表的陣列

                channel_total=channels_jsonArray.length();//取得 channel 數量
                channel_info = new int[channel_total][4];

                for (int i =0;i<channel_total;i++){
                    channel_info[i][0]=channels_jsonArray.getJSONObject(i).getInt("id");//取得第 i 個 channel 的 ID

                    URL tmp_url = new URL("https://api.thingspeak.com/channels/"+channel_info[i][0]+"/feeds.json");//取得第 i 個 channel 的 feed 的列表的網址
                    conn= (HttpURLConnection) tmp_url.openConnection();
                    conn.connect();
                    reader= new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
                    jsonString=reader.readLine();
                    reader.close();
                    conn.disconnect();

                    JSONArray channel_feeds_jsonArray =new JSONObject(jsonString).getJSONArray("feeds");//取得 channel_feeds 列表的陣列

                    for (int j = 0;j<channel_feeds_jsonArray.length();j++){
                        if (channel_info[i][1]<channel_feeds_jsonArray.getJSONObject(j).getInt("field1")){
                            channel_info[i][1]=channel_feeds_jsonArray.getJSONObject(j).getInt("field1");
                        }
                    }
                    channel_info[i][2]=channel_feeds_jsonArray.getJSONObject(channel_feeds_jsonArray.length()-1).getInt("field1");
                    channel_info[i][3]=(int)(((float) channel_info[i][2]/channel_info[i][1])*100);
                    Log.d("channel_ID", i+"_"+channel_info[i][0]);
                    Log.d("channel_max", i+"_"+channel_info[i][1]);
                    Log.d("channel_last", i+"_"+channel_info[i][2]);
                    Log.d("channel_percent", i+"_"+channel_info[i][3]);
                }
                //Log.d("channel_num", String.valueOf(channels_jsonArray.length()));
                Object[] res = new Object[2];

                return res;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object[] res) {
            super.onPostExecute(res);
        }
    }

}

