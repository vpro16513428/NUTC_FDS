package com.nutccsie.nutc_fds;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.v4.widget.SimpleCursorAdapter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    int channel_total=0;
    int[][] channel_info;// [0] ID ,  [1] APIKEY , [2] MAX , [3] LAST , [4] Percent
    String User_APIKEY="";
    String Channel_name="";
    String Channel_ID="";
    String Channel_APIKEY="";
    String Channel_percent="";

    private Cursor myCursor;
    private EditText inputText;
    private EditText inputTEXT2;
    private ListView listinput;
    private ArrayAdapter<String> adapter;
    private testadp testadp_test;
    private ArrayList<String> item;
    int count = 0 , y = 0 , red_warn = 20 , yellow_warn = 50;

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
        //TSW.percentChannel("96545");
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

    private void openadd(){
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.add_activity, null);
        final EditText inputText = (EditText)v.findViewById(R.id.edit);
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
                                    item.add(inputText.getText().toString());
                                    inputText.setText("");
                                    listinput.setAdapter(testadp_test);
                                    count++;
                                    //ThingSpeakWork TSW = new ThingSpeakWork();
                                    //TSW.newChannel(inputText.getText().toString());
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
                                                            testadp_test.insert(inputText2.getText().toString(), y);
                                                            ThingSpeakWork TSW = new ThingSpeakWork();
                                                            TSW.editChannel(Channel_ID,inputText2.getText().toString());
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
                                ThingSpeakWork TSW = new ThingSpeakWork();
                                TSW.deleteChannel(Channel_ID);
                            }
                        })
                .show();
    }//用AlertDialog的方式指定刪除listview

    private void opensetting(){
        final LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View v = inflater.inflate(R.layout.setting_activity, null);
        final TextView text = (TextView) v.findViewById(R.id.textView);
        final SeekBar seekbar = (SeekBar)v.findViewById(R.id.seekBar);
        final TextView text3 = (TextView) v.findViewById(R.id.textView3);
        final TextView text2 = (TextView) v.findViewById(R.id.textView2);
        final SeekBar seekbar2 = (SeekBar)v.findViewById(R.id.seekBar2);
        final TextView text4 = (TextView) v.findViewById(R.id.textView4);
        final EditText edittext = (EditText)v.findViewById(R.id.edit);
        final Button scn_btn = (Button) v.findViewById(R.id.Scan_sensor_button);
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
        scn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View scn_v = inflater.inflate(R.layout.esptouch_demo_activity, null);

                final String TAG = "EsptouchDemoActivity";
                final TextView mTvApSsid;
                final EditText mEdtApPassword;
                final Button mBtnConfirm;
                final Switch mSwitchIsSsidHidden;
                final EspWifiAdminSimple mWifiAdmin;
                final Spinner mSpinnerTaskCount;

                mWifiAdmin = new EspWifiAdminSimple(MainActivity.this);
                mTvApSsid = (TextView) scn_v.findViewById(R.id.tvApSssidConnected);
                mEdtApPassword = (EditText) scn_v.findViewById(R.id.edtApPassword);
                mBtnConfirm = (Button) scn_v.findViewById(R.id.btnConfirm);
                mSwitchIsSsidHidden = (Switch) scn_v.findViewById(R.id.switchIsSsidHidden);

                String apSsid = mWifiAdmin.getWifiConnectedSsid();
                if (apSsid != null) {
                    mTvApSsid.setText(apSsid);
                } else {
                    mTvApSsid.setText("");
                }

                mSpinnerTaskCount = (Spinner) scn_v.findViewById(R.id.spinnerTaskResultCount);
                int[] spinnerItemsInt = getResources().getIntArray(R.array.taskResultCount);
                int length = spinnerItemsInt.length;
                Integer[] spinnerItemsInteger = new Integer[length];
                for(int i=0;i<length;i++)
                {
                    spinnerItemsInteger[i] = spinnerItemsInt[i];
                }
                ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(MainActivity.this, android.R.layout.simple_list_item_1, spinnerItemsInteger);
                mSpinnerTaskCount.setAdapter(adapter);
                mSpinnerTaskCount.setSelection(1);

                mBtnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v == mBtnConfirm) {
                            String apSsid = mTvApSsid.getText().toString();
                            String apPassword = mEdtApPassword.getText().toString();
                            String apBssid = mWifiAdmin.getWifiConnectedBssid();
                            Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked();
                            String isSsidHiddenStr = "NO";
                            String taskResultCountStr = Integer.toString(mSpinnerTaskCount.getSelectedItemPosition());
                            if (isSsidHidden)
                            {
                                isSsidHiddenStr = "YES";
                            }
                            if (__IEsptouchTask.DEBUG) {
                                Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid + ", " + " mEdtApPassword = " + apPassword);
                            }
                            new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword, isSsidHiddenStr, taskResultCountStr);
                        }
                    }
                });




                final AlertDialog.Builder scn = new AlertDialog.Builder(MainActivity.this);
                scn.setTitle("ScanSensor")
                        .setView(scn_v)
                        .setPositiveButton("離開",
                                new  DialogInterface.OnClickListener(){
                                    @Override
                                    public void onClick(DialogInterface dialog,int which){
                                        dialog.cancel();
                                    }
                                });

                scn.create().show();
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

        void percentChannel(String ID){
            Channel_ID=ID;
            this.execute(4);
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
                    map.put("public_flag","true");

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
                case 4://percent
                    map = null;

                    HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels/" + Channel_ID + "/feeds.json", map);
                    JSONstr = HUCW.sendHttpURLConnectionRequest("GET");

                    int num=0;
                    float max=0,last=0;
                    try {
                        result = new JSONObject(JSONstr);
                        num=result.getJSONArray("feeds").length();
                        for (int i=0;i<num;i++){
                            if (max<result.getJSONArray("feeds").getJSONObject(i).getInt("field1")){
                                max=result.getJSONArray("feeds").getJSONObject(i).getInt("field1");
                            }
                        }
                        last=result.getJSONArray("feeds").getJSONObject(num-1).getInt("field1");
                        Channel_percent= String.valueOf(last/max*100);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

    private class EsptouchAsyncTask2 extends AsyncTask<String, Void, IEsptouchResult> {

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
        protected IEsptouchResult doInBackground(String... params) {
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, MainActivity.this);
            }
            IEsptouchResult result = mEsptouchTask.executeForResult();
            return result;
        }

        @Override
        protected void onPostExecute(IEsptouchResult result) {
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(true);
            mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                    "Confirm");
            // it is unnecessary at the moment, add here just to show how to use isCancelled()
            if (!result.isCancelled()) {
                if (result.isSuc()) {
                    mProgressDialog.setMessage("Esptouch success, bssid = "
                            + result.getBssid() + ",InetAddress = "
                            + result.getInetAddress().getHostAddress());
                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                }
            }
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
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, MainActivity.this);
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
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage(sb.toString());
                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                }
            }
        }
    }
}

