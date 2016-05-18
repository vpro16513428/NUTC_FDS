package com.nutccsie.nutc_fds;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.StringDef;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.nutccsie.nutc_fds.task.__IEsptouchTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    int channel_total = 0;
    String[][] Channel_Info;// [0] IP ,  [1] ID , [2] name , [3] APIKEY , [4] Percent
    String User_APIKEY = "";
    String Channel_name = "";
    String Channel_ID = "";
    String Channel_APIKEY = "";
    String Channel_percent = "";
    String Sensor_IP = "";
    Socket socket = null;
    TextView textResponse;

    private EditText inputText;
    private ListView listinput;
    private ArrayAdapter<String> adapter;
    private testadp testadp_test;
    private ArrayList<String> item;
    int count = 0 , y = 0 , red_warn = 20 , yellow_warn = 50;

    public void writeData() {
        try {
            FileOutputStream fos = openFileOutput("settings.dat", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            JSONObject data = new JSONObject();
            //User_APIKEY
            data.put("User_APIKEY", User_APIKEY);
            /*
            //Channel_Info
            JSONArray temp = new JSONArray();
            for (int i = 0; i < channel_total; i++) {
                JSONObject temp2 = new JSONObject();
                for (int j = 0; j < 5; j++) {
                    switch (j) {
                        case 0: //IP
                            temp2.put("IP", Channel_Info[i][0]);
                            break;
                        case 1: //ID
                            temp2.put("ID", Channel_Info[i][1]);
                            break;
                        case 2: //name
                            temp2.put("name", Channel_Info[i][2]);
                            break;
                        case 3: //APIKEY
                            temp2.put("APIKEY", Channel_Info[i][3]);
                            break;
                        case 4: //Percent
                            temp2.put("Percent", Channel_Info[i][4]);
                            break;
                    }
                }
                temp.put(i, temp2);
            }

            data.put("Channel_Info", temp);
*/
            osw.write(data.toString());
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readSavedData() {
        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = openFileInput("settings.dat");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }

            isr.close();
            Log.d("datax",datax.toString());
            JSONObject data = null;
            data = new JSONObject(datax.toString());
            User_APIKEY = data.getString("User_APIKEY");
            /*
            if(data.getJSONArray("Channel_Info").length()!=0){
                channel_total = data.getJSONArray("Channel_Info").length()-1;
            }
            Channel_Info=new String[channel_total+1][5];
            for (int i = 0; i < channel_total; i++) {
                //IP
                Channel_Info[i][0] = data.getJSONArray("Channel_Info").getJSONObject(i).getString("IP");
                //ID
                Channel_Info[i][1] = data.getJSONArray("Channel_Info").getJSONObject(i).getString("ID");
                //name
                Channel_Info[i][2] = data.getJSONArray("Channel_Info").getJSONObject(i).getString("name");
                //APIKEY
                Channel_Info[i][3] = data.getJSONArray("Channel_Info").getJSONObject(i).getString("APIKEY");
                //Percent
                Channel_Info[i][4] = data.getJSONArray("Channel_Info").getJSONObject(i).getString("Percent");
            }*/
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        writeData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readSavedData();
        listinput = (ListView)findViewById(R.id.listView);
        item = new ArrayList<>();
        testadp_test = new testadp(this, item,yellow_warn,red_warn);
        listinput.setAdapter(testadp_test);
        Channel_Info=new String[1][5];
        Channel_Info[0][0]="192.168.1.101";
        Channel_Info[0][1]="";
        Channel_Info[0][2]="";
        Channel_Info[0][3]="Y50WL6TXOL5JY42N";
        Channel_Info[0][4]="";
        ThingSpeakWork TSW = new ThingSpeakWork();
        TSW.refresh();
        Log.d("test","test");

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }// ActionBar

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                openadd();
                return true;
            case R.id.action_edit:
                openedit();
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

    private void openadd() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.add_activity, null);
        final EditText inputText = (EditText)v.findViewById(R.id.edit);
            new AlertDialog.Builder(MainActivity.this)
                .setTitle("新增")
                .setView(v)
                .setPositiveButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (!inputText.getText().toString().equals("")) {
                                    ThingSpeakWork TSW = new ThingSpeakWork();
                                    TSW.newChannel(inputText.getText().toString());
                                    item.add(inputText.getText().toString() + "        " + Channel_Info[channel_total][4] + "%");
                                    inputText.setText("");
                                    listinput.setAdapter(testadp_test);
                                    count++;
                                }
                            }
                        })
                .create().show();
    }//用AlertDialog的方式以EditText新增到listview

    public EditText getInputText() {
        return inputText;
    }

    private void openedit(){
        final String[] str = new String[count];
        for(int i=0; i<count; i++){
            str[i] = item.get(i);
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("修改")
                .setSingleChoiceItems(str, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        y = which;
                    }
                })
                .setPositiveButton("取消",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("確定",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                                final View v = inflater.inflate(R.layout.edit_activity, null);
                                final EditText inputText2 = (EditText)v.findViewById(R.id.edit2);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("修改")
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
                                                        if (!inputText2.getText().toString().equals("")){
                                                            item.remove(y);
                                                            testadp_test.insert(inputText2.getText().toString() + "        " + Channel_Info[y][4] + "%", y);
                                                            ThingSpeakWork TSW = new ThingSpeakWork();
                                                            TSW.editChannel(Channel_Info[y+1][1],inputText2.getText().toString());
                                                        }
                                                    }
                                                })
                                        .show();
                            }
                        })
                .show();
    }

    private void opendelete(){
        final String[] str = new String[count];
        for (int i = 0; i < count; i++) {
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
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                ThingSpeakWork TSW = new ThingSpeakWork();
                                TSW.deleteChannel(Channel_ID);
                            }
                        })
                .show();
    }//用AlertDialog的方式指定刪除listview

    private void opensetting() {
        final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.setting_activity, null);
        final TextView text = (TextView) v.findViewById(R.id.textView);
        final SeekBar seekbar = (SeekBar) v.findViewById(R.id.seekBar);
        final TextView text3 = (TextView) v.findViewById(R.id.textView3);
        final TextView text2 = (TextView) v.findViewById(R.id.textView2);
        final SeekBar seekbar2 = (SeekBar) v.findViewById(R.id.seekBar2);
        final TextView text4 = (TextView) v.findViewById(R.id.textView4);
        final EditText edittext = (EditText)v.findViewById(R.id.edit);
        final Button scn_btn = (Button) v.findViewById(R.id.Scan_sensor_button);
        final Button set_btn = (Button) v.findViewById(R.id.Set_sensor_button);
        seekbar.setProgress(yellow_warn);
        seekbar2.setProgress(red_warn);
        text3.setText(yellow_warn + "%");
        text4.setText(red_warn + "%");
        final AlertDialog.Builder setting = new AlertDialog.Builder(this);
        setting.setTitle("設定")
                .setView(v)
                .setPositiveButton("離開",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which){
                                User_APIKEY = edittext.getText().toString();
                                Log.d("text",User_APIKEY);
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
                text4.setText((progress) + "%");
                red_warn = progress;
                testadp_test.setRedWarnValue(red_warn);
                testadp_test.notifyDataSetChanged();
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        scn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View scn_v = inflater.inflate(R.layout.esptouch_demo_activity, null);

                final String TAG = "EsptouchDemoActivity";
                final TextView mTvApSsid;
                final EditText mEdtApPassword;
                final Button mBtnConfirm;
                final EspWifiAdminSimple mWifiAdmin;


                mWifiAdmin = new EspWifiAdminSimple(MainActivity.this);
                mTvApSsid = (TextView) scn_v.findViewById(R.id.tvApSssidConnected);
                mEdtApPassword = (EditText) scn_v.findViewById(R.id.edtApPassword);
                mBtnConfirm = (Button) scn_v.findViewById(R.id.btnConfirm);


                String apSsid = mWifiAdmin.getWifiConnectedSsid();
                if (apSsid != null) {
                    mTvApSsid.setText(apSsid);
                } else {
                    mTvApSsid.setText("");
                }

                mBtnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v == mBtnConfirm) {
                            String apSsid = mTvApSsid.getText().toString();
                            String apPassword = mEdtApPassword.getText().toString();
                            String apBssid = mWifiAdmin.getWifiConnectedBssid();
                            Boolean isSsidHidden = false;
                            String isSsidHiddenStr = "NO";
                            String taskResultCountStr = "1";
                            if (isSsidHidden) {
                                isSsidHiddenStr = "YES";
                            }
                            if (__IEsptouchTask.DEBUG) {
                                Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid + ", " + " mEdtApPassword = " + apPassword);
                            }
                            EsptouchAsyncTask3 temp = new EsptouchAsyncTask3();
                            temp.execute(apSsid, apBssid, apPassword, isSsidHiddenStr, taskResultCountStr);
                        }
                    }
                });


                final AlertDialog.Builder scn = new AlertDialog.Builder(MainActivity.this);
                scn.setTitle("ScanSensor")
                        .setView(scn_v)
                        .setPositiveButton("離開",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                scn.create().show();
            }
        });

        set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View set_v = inflater.inflate(R.layout.socket_activity, null);

                final TextView txtSend;
                final EditText editTextAddress, editTextPort;
                final Button buttonConnect, buttonSend;

                editTextAddress = (EditText) set_v.findViewById(R.id.address);
                editTextAddress.setText(Sensor_IP);
                editTextPort = (EditText) set_v.findViewById(R.id.port);
                buttonConnect = (Button) set_v.findViewById(R.id.connect);
                textResponse = (TextView) set_v.findViewById(R.id.response);
                buttonSend = (Button) set_v.findViewById(R.id.send_btn);
                txtSend = (TextView) set_v.findViewById(R.id.txtSend);

                buttonConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v == buttonConnect) {
                            Log.d("IP:PORT", editTextAddress.getText().toString() + ":" + editTextPort.getText().toString());
                            MyClient_connect_Task myClientTask = new MyClient_connect_Task(editTextAddress.getText().toString(), Integer.parseInt(editTextPort.getText().toString()));
                            myClientTask.execute();
                        }
                    }
                });
                buttonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v == buttonSend) {

                            MyClient_send_Task myClient_send_task = new MyClient_send_Task();
                            myClient_send_task.execute(txtSend.getText().toString());
                        }
                    }
                });

                final AlertDialog.Builder set = new AlertDialog.Builder(MainActivity.this);
                set.setTitle("Set Sensor")
                        .setView(set_v)
                        .setPositiveButton("離開",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                set.create().show();
            }
        });

        setting.create().show();
    }//set使用AlertDialog的方式顯示SeekBar

    public class ThingSpeakWork extends AsyncTask<Integer, Integer, Integer> {
        private ProgressDialog progressBar;
        HttpURLConnection_WORK HUCW = null;
        String JSONstr = null;
        JSONObject result = null;
        HashMap<String, String> map = null;

        void newChannel(String name) {
            Channel_name = name;
            this.execute(0);
        }

        void editChannel(String ID, String name) {
            Channel_ID = ID;
            Channel_name = name;
            this.execute(1);
        }

        void resetChannel(String ID) {
            Channel_ID = ID;
            this.execute(2);
        }

        void deleteChannel(String ID) {
            Channel_ID = ID;
            this.execute(3);
        }

        void percentChannel(String ID) {
            Channel_ID = ID;
            this.execute(4);
        }

        void refresh() {
            this.execute(5);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(MainActivity.this);
            progressBar.setMessage("Loading...");
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
            //初始化進度條並設定樣式及顯示的資訊。
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            publishProgress(0);
            switch (params[0]) {
                case 0://new
                    map = new HashMap<>();
                    //new
                    map.put("name", Channel_name);
                    map.put("api_key", User_APIKEY);
                    map.put("public_flag", "true");

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels.json", map);
                    JSONstr = HUCW.sendHttpURLConnectionRequest("POST");

                    try {
                        Log.d("JSONstr",JSONstr);
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

                case 4://percent
                    map = null;

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels/" + Channel_ID + "/feeds.json", map);
                    JSONstr = HUCW.sendHttpURLConnectionRequest("GET");

                    int num = 0;
                    float max = 0, last = 0,percent=0;
                    try {
                        result = new JSONObject(JSONstr);
                        num = result.getJSONArray("feeds").length();
                        if(num!=0){
                            for (int i = 0; i < num; i++) {
                                if (max < result.getJSONArray("feeds").getJSONObject(i).getInt("field1")) {
                                    max = result.getJSONArray("feeds").getJSONObject(i).getInt("field1");
                                }
                            }
                            last = result.getJSONArray("feeds").getJSONObject(num - 1).getInt("field1");

                            percent = last / max * 100;
                            Channel_percent=String.format("%.1f", percent) ;
                        }else {
                            Channel_percent = "0.0";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case 5://refresh

                    map = null;

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/users/vpro16513428/channels.json", map);
                    JSONstr = HUCW.sendHttpURLConnectionRequest("GET");


                    try {
                        result = new JSONObject(JSONstr);
                        if(result.getJSONArray("channels").length()!=0){
                            channel_total=result.getJSONArray("channels").length()-1;
                        }

                        String[][] temp =new String[channel_total+1][5];
                        for (int i = 0;i<channel_total;i++){
                            for(int j = 0;j<5;j++){
                                temp[i][j]=Channel_Info[i][j];
                            }
                        }
                        Channel_Info=temp;

                        for(int i = 0;i<=channel_total;i++){
                            Channel_Info[i][1]=String.valueOf(result.getJSONArray("channels").getJSONObject(i).getInt("id"));
                            Channel_Info[i][2]=result.getJSONArray("channels").getJSONObject(i).getString("name");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

            }
            publishProgress(100);
            return params[0];
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Integer method) {
            super.onPostExecute(method);
            switch (method) {
                case 0: //new

                    channel_total+=1;

                    String[][] temp =new String[channel_total+1][5];
                    for (int i = 0;i<channel_total;i++){
                        for(int j = 0;j<5;j++){
                            temp[i][j]=Channel_Info[i][j];
                        }
                    }
                    //IP
                    temp[channel_total][0]="";
                    //ID
                    temp[channel_total][1]=Channel_ID;
                    //name
                    temp[channel_total][2]=Channel_name;
                    //APIKEY
                    temp[channel_total][3]=Channel_APIKEY;
                    //Percent
                    temp[channel_total][4]="0";

                    Channel_Info=temp;

                    item.add(Channel_Info[channel_total][2] + "        " + Channel_Info[channel_total][4] + "%");

                    break;
                case 1: //edit

                    break;
                case 2: //reset

                    break;
                case 3: //delete

                    break;
                case 4: //percent

                    break;
                case 5: //refresh

                    for(int i=0;i<=channel_total;i++){

                        ThingSpeakWork mTSW = new ThingSpeakWork();
                        mTSW.percentChannel(Channel_Info[i][1]);
                        try {
                            mTSW.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        Channel_Info[i][4]=Channel_percent;
                        item.add(Channel_Info[i][2] + "        " + Channel_Info[i][4] + "%");
                    }
                    testadp_test.notifyDataSetChanged();

                    break;
            }
            progressBar.dismiss();
            HUCW = null;
            JSONstr = null;
            result = null;
            map = null;//設null防止佔用過多記憶體
        }
    }


    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(MainActivity.this, text,
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog
                    .setMessage("Esptouch is configuring, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i("test", "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    "Waiting...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            mProgressDialog.show();
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(false);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, MainActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    "Confirm");
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                        Sensor_IP = resultInList.getInetAddress().toString();
                        Toast.makeText(MainActivity.this, Sensor_IP, Toast.LENGTH_LONG).show();
                        sb.append("Esptouch success, bssid = " + resultInList.getBssid() + ",InetAddress = " + resultInList.getInetAddress().getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count) + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage(sb.toString());
                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                }
            }
        }
    }

    public class MyClient_connect_Task extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        MyClient_connect_Task(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Log.d("connectTask", "connectTask");
                socket = null;
                socket = new Socket(dstAddress, dstPort);

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }


    public class MyClient_send_Task extends AsyncTask<String, Void, Void> {
        String response = "";

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                Log.d("sendTask", "sendTask");
                byte[] data = arg0[0].getBytes("UTF-8");
                OutputStream os = socket.getOutputStream();
                os.write(data);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

    /*
     * notice:
     * inputStream.read() will block if no data return
     */
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                }

            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            textResponse.setText(response);
            super.onPostExecute(result);
        }

    }
}

