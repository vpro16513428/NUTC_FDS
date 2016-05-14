package com.nutccsie.nutc_fds;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.Set;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class MainActivity extends AppCompatActivity {

    int channel_total=0;
    int[][] channel_info;// [0] ID ,  [1] APIKEY , [2] MAX , [3] LAST , [4] Percent
    String User_APIKEY="RO28U1DJSWQB8Q3L";
    String Channel_name="";
    String Channel_ID="";
    String Channel_APIKEY="";

    private EditText inputText;
    private ListView listinput;
    private ArrayAdapter<String> adapter;
    private testadp testadp_test;
    private ArrayList<String> item;
    int count = 0 , y = 0 , red_warn = 20 , yellow_warn = 50;

    SQLiteDatabase db;
    //資料庫名
    public String db_name = "SQLite";

    //表名
    public String table_name = "ListChannel";

    //輔助類名
    MyDBHelper SQLite = new MyDBHelper(MainActivity.this, db_name);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listinput = (ListView)findViewById(R.id.listView);
        item = new ArrayList<>();
        //adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,item);
        testadp_test = new testadp(this,item);
        listinput.setAdapter(testadp_test);
        //AlertDialog
        ThingSpeakWork TSW = new ThingSpeakWork();
        //TSW.newChannel("My New Channel");
        //TSW.editChannel("116139","Updated Channel");
        //TSW.resetChannel("116139");
        //TSW.deleteChannel("116139");
        db = SQLite.getReadableDatabase();

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

    private  void refresharraylist(){
        for (int i=0 ; i<count ; i++){
            if (channel_info[i][3]<red_warn){
            }
            if (channel_info[i][3]<yellow_warn){
            }
        }
    }

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
                                if (!inputText.getText().toString().equals("") && !inputText2.getText().toString().equals("")){
                                    char ch[] = inputText2.getText().toString().toCharArray();
                                    for (int i=0 ; i < inputText2.getText().length(); i++){
                                        if (!Character.isDigit(ch[i])){
                                            break;
                                        }else if ((Integer.parseInt(inputText2.getText().toString())>4)){
                                            item.add(inputText.getText().toString() + "        " + channel_info[0][3] + "%");
                                        }else {
                                            item.add(inputText.getText().toString() + "        " + channel_info[Integer.parseInt(inputText2.getText().toString())][3] + "%");
                                        }
                                        inputText.setText("");
                                        listinput.setAdapter(testadp_test);
                                        ++count;
                                    }

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
                                testadp_test.notifyDataSetChanged();
                                count--;
                            }
                        })
                .show();

    }//用AlertDialog的方式以EditText新增到listview

    private void opensetting(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.setting_activity, null);
        final TextView text = (TextView) v.findViewById(R.id.textView);
        final SeekBar seekbar = (SeekBar)v.findViewById(R.id.seekBar);
        final TextView text3 = (TextView) v.findViewById(R.id.textView3);
        final TextView text2 = (TextView) v.findViewById(R.id.textView2);
        final SeekBar seekbar2 = (SeekBar)v.findViewById(R.id.seekBar2);
        final TextView text4 = (TextView) v.findViewById(R.id.textView4);
        seekbar.setProgress(yellow_warn);
        seekbar2.setProgress(red_warn);
        text3.setText(yellow_warn+"%");
        text4.setText(red_warn+"%");
        final AlertDialog.Builder setting = new AlertDialog.Builder(this);
        setting.setTitle("設定")
                .setView(v)
                .setPositiveButton("離開",
                        new  DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("確定",
                        new  DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                dialog.cancel();
                            }
                        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            public void onProgressChanged(SeekBar seekBar, int progressV, boolean fromUser) {
                progress = progressV;
                text3.setText((progress) + "%");
                yellow_warn = progress;
                testadp_test.setYellowWarnValue(yellow_warn);
                testadp_test.notifyDataSetChanged();
            }
            public void onStartTrackingTouch(SeekBar arg0) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekbar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            public void onProgressChanged(SeekBar seekBar2, int progressV, boolean fromUser) {
                progress = progressV;
                text4.setText((progress)+"%");
                red_warn = progress;
                testadp_test.setRedWarnValue(red_warn);
                testadp_test.notifyDataSetChanged();
            }
            public void onStartTrackingTouch(SeekBar arg0) {
            }
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        setting.create().show();
    }//set使用AlertDialog的方式顯示SeekBar

    public class ThingSpeakWork extends AsyncTask<Integer, Integer, Void> {
        private ProgressDialog progressBar;
        HttpURLConnection_WORK HUCW=null;
        String JSONstr=null;
        JSONObject result=null;
        HashMap<String,String> map=null;

        void newChannel(String name){
            Channel_name=name;
            this.execute(0);
        }

        void editChannel(String ID,String name){
            Channel_ID=ID;
            Channel_name=name;
            this.execute(1);
        }

        void resetChannel(String ID){
            Channel_ID=ID;
            this.execute(2);
        }

        void deleteChannel(String ID){
            Channel_ID=ID;
            this.execute(3);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(MainActivity.this);
            progressBar.setMessage("Loading...");
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.show();
            //初始化進度條並設定樣式及顯示的資訊。
        }

        @Override
        protected Void doInBackground(Integer... params) {
            publishProgress(0);
            switch (params[0]) {
                case 0://new
                    map = new HashMap<>();
                    //new
                    map.put("name", Channel_name);
                    map.put("api_key", User_APIKEY);

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels.json", map);
                    JSONstr = HUCW.sendHttpURLConnectionRequest("POST");

                    try {
                        result = new JSONObject(JSONstr);
                        Channel_ID = String.valueOf(result.getInt("id"));
                        Channel_APIKEY = result.getJSONArray("api_keys").getJSONObject(0).getString("api_key");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case 1://edit
                    map = new HashMap<>();
                    //edit
                    map.put("name", Channel_name);
                    map.put("api_key", User_APIKEY);

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels/" + Channel_ID + ".json", map);
                    HUCW.sendHttpURLConnectionRequest("PUT");

                    break;
                case 2://reset
                    map = new HashMap<>();
                    //edit
                    map.put("api_key", User_APIKEY);

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels/" + Channel_ID + "/feeds.json", map);
                    HUCW.sendHttpURLConnectionRequest("DELETE");

                    break;
                case 3://delete
                    map = new HashMap<>();
                    //edit
                    map.put("api_key", User_APIKEY);

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels/" + Channel_ID + ".json", map);
                    HUCW.sendHttpURLConnectionRequest("DELETE");

                    break;
            }
            publishProgress(100);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.dismiss();
            HUCW = null;
            JSONstr = null;
            result = null;
            map = null;//設null防止佔用過多記憶體
        }
    }
}

