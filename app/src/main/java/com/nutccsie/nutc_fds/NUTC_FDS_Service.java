package com.nutccsie.nutc_fds;

/**
 * Created by user on 2016/5/20.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

//繼承android.app.Service
public class NUTC_FDS_Service extends Service {
    private Handler handler = new Handler();
    String[][] Channel_Info=null;
    String User_APIKEY="";
    String Channel_ID="";
    String Channel_percent="";
    int red_warn = 0;
    int channel_total=-1;


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        writeData();
        super.onTaskRemoved(rootIntent);
    }

    public void writeData() {
        try {
            FileOutputStream fos = openFileOutput("service.dat", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            JSONObject data = new JSONObject();
            //User_APIKEY
            data.put("User_APIKEY", User_APIKEY);
            data.put("red_warn", red_warn);

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
            FileInputStream fIn = openFileInput("service.dat");
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }

            isr.close();
            Log.d("datax",datax.toString());
            if(datax.toString()!=""){
                JSONObject data = null;
                data = new JSONObject(datax.toString());
                User_APIKEY = data.getString("User_APIKEY");
                red_warn = data.getInt("red_warn");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        readSavedData();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onStart(Intent intent, int startId) {

        if(intent!=null){
            Log.d("intent","not null");
            User_APIKEY=intent.getStringExtra("User_APIKEY").toString();
            red_warn=intent.getIntExtra("red_warn", red_warn);
            Log.d("User_APIKEY",User_APIKEY);
        }else{
            Log.d("intent","null");
        }

        handler.postDelayed(showTime, 1000);

        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        writeData();
        handler.removeCallbacks(showTime);
        super.onDestroy();
    }

    private Runnable showTime = new Runnable() {
        public void run() {
            Log.d("run","123");
            for (int i=0 ;i<=channel_total; i++){

                if (Float.parseFloat(Channel_Info[i][4])<red_warn && Channel_Info[i][5].equals("0") /*&& !Channel_Info[i][4].equals("0.0")*/){
                    final int notifyID =Integer.valueOf(Channel_Info[i][1]); // 通知的識別號碼
                    // 建立震動效果，陣列中元素依序為停止、震動的時間，單位是毫秒
                    long[] vibrate_effect = {500, 500, 500, 500};
                    final Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // 通知音效的URI，在這裡使用系統內建的通知音效
                    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
                    final Notification notification = new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("液難忘_FDS")
                            .setVibrate(vibrate_effect)
                            .setSound(soundUri)
                            .setContentText(Channel_Info[i][2]+"只剩下"+Channel_Info[i][4]+"%囉!!").build(); // 建立通知
                    //notification.defaults=Notification.DEFAULT_ALL;
                    //notification.flags = Notification.FLAG_AUTO_CANCEL;
                    notificationManager.notify(notifyID, notification); // 發送通知
                    Channel_Info[i][5] = String.valueOf(Integer.parseInt(Channel_Info[i][5])+1);
                }
            }
            ThingSpeakWork TSW= new ThingSpeakWork();
            TSW.refresh();
            try {
                TSW.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, 1000);
        }
    };

    public class ThingSpeakWork extends AsyncTask<Integer, Integer, Integer> {

        HttpURLConnection_WORK HUCW = null;
        String JSONstr = null;
        JSONObject result = null;
        HashMap<String, String> map = null;




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
        }

        @Override
        protected Integer doInBackground(Integer... params) {

            switch (params[0]) {

                case 4://percent
                    map = null;
                    int num = 0;
                    float max = 0, last = 0,percent=0;
                    try {
                        HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels/" + Channel_ID + "/feeds.json", map);
                        JSONstr = HUCW.sendHttpURLConnectionRequest("GET");
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

                    try {
                        HUCW = new HttpURLConnection_WORK("https://api.thingspeak.com/channels.json?api_key="+User_APIKEY, map);
                        JSONstr = HUCW.sendHttpURLConnectionRequest("GET");
                        JSONArray temp = new JSONArray(JSONstr);
                        if(!JSONstr.equals("")){
                            channel_total=temp.length()-1;
                            String[][] temp2 =new String[channel_total+1][6];

                            for (int i = 0;i<=channel_total;i++){
                                for(int j = 0;j<6;j++){
                                    switch (j){
                                        case 0:
                                            temp2[i][0]="";
                                            break;
                                        case 1:
                                            temp2[i][1]= String.valueOf(temp.getJSONObject(i).getInt("id"));
                                            break;
                                        case 2:
                                            temp2[i][2]= temp.getJSONObject(i).getString("name");
                                            break;
                                        case 3:
                                            temp2[i][3]= temp.getJSONObject(i).getJSONArray("api_keys").getJSONObject(0).getString("api_key");
                                            break;
                                        case 4:
                                            temp2[i][4]= "0.0";
                                            break;
                                        case 5:
                                            if (Channel_Info!=null){
                                                temp2[i][5]= Channel_Info[i][5];
                                            }
                                            break;

                                    }
                                }
                            }
                            Channel_Info=temp2;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return params[0];
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer method) {
            super.onPostExecute(method);
            switch (method) {
                case 4: //percent

                    break;
                case 5: //refresh

                    if(channel_total>-1){
                        for(int i=0;i<=channel_total;i++){
                            ThingSpeakWork mTSW = new ThingSpeakWork();
                            mTSW.percentChannel(Channel_Info[i][1]);
                            try {
                                mTSW.get();
                                Channel_Info[i][4]=Channel_percent;
                                if(Channel_Info[i][4].equals("0.0")||Channel_Info[i][5]==null){
                                    Channel_Info[i][5]="0";
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    break;
            }

            HUCW = null;
            JSONstr = null;
            result = null;
            map = null;//設null防止佔用過多記憶體
        }
    }

    public class HttpURLConnection_WORK {
        private URL url;                    //儲存網路php路徑
        private Map<String, String> map;    //儲存要送的值
        private String encode="utf-8";        //儲存編碼
        //建構子(網路路徑,要送的值)
        public HttpURLConnection_WORK(String path,Map<String, String> map){
            try {
                this.url=new URL(path);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            this.map=map;
        }
        //發送HttpURLConnectionRequest(網址,資料內容,編碼方式)
        public String sendHttpURLConnectionRequest(String method) {
            String temp="";
            try {
                //打開服務器
                HttpURLConnection hucn=(HttpURLConnection) url.openConnection();
                hucn.setReadTimeout(5000);            //設置讀取超時為5秒
                hucn.setConnectTimeout(10000);        //設置連接網路超時為10秒

                //設置輸出入流串
                hucn.setDoInput(true);                //可從伺服器取得資料
                if(method=="GET"){
                    //設置請求的方法
                    hucn.setRequestMethod(method);
                }else {
                    hucn.setDoOutput(true);                //可寫入資料至伺服器
                    //設置請求的方法
                    hucn.setRequestMethod(method);
                    //POST方法不能緩存數據,需手動設置使用緩存的值為false
                    hucn.setUseCaches(false);
                    //寫入參數
                    OutputStream os=hucn.getOutputStream();            //設置輸出流串
                    DataOutputStream dos=new DataOutputStream(os);    //封裝寫給伺服器的資料,需存進這裡
                    if (map != null && !map.isEmpty()) {            //判斷map是否非null或有初始化
                        String str=null;                //用來存傳送參數
                        //entrySet()會得到map內的key-value成對的集合,並回傳
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            if(str==null)    //判斷是否為第一次調用
                                str=entry.getKey()+"="+ URLEncoder.encode(entry.getValue(),encode);
                            else
                                str=str+"&"+entry.getKey()+"="+URLEncoder.encode(entry.getValue(),encode);
                            //Key值不變,Value轉成UTF-8編碼可使用中文
                            //Map.Entry內提供了getKey()、getValue()、setValue(),雖然增加一行卻省略了很多對Map不必要的get調用
                        }
                        dos.writeBytes(str);    //將設置好的請求參數寫進dos
                        //顯示時必須進行解碼,否則看到的中文會變成亂碼
                        Log.i("text","HttpURLConnection_POST.dos傳送資料="+java.net.URLDecoder.decode(str,encode));
                    }
                    //輸出完關閉輸出流
                    dos.flush();
                    dos.close();
                }

                //判斷是否請求成功,為200時表示成功,其他均有問題
                if(hucn.getResponseCode() == 200){
                    //取得回傳的inputStream (輸入流串)
                    InputStream inputStream = hucn.getInputStream();
                    temp= changeInputStream(inputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("text","HttpURLConnection_POST="+e.toString());
            }

            return temp;
        }
        public String changeInputStream(InputStream inputStream) {    //將輸入串流轉成字串回傳
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            //ByteArrayOutputStream型態可不斷寫入來增長緩存區,可使用toByteArray()、toString()獲取數據
            byte[] data = new byte[1024];
            int len;
            String result = "";
            if (inputStream != null) {        //判斷inputStream是否非空字串
                try {
                    while ((len = inputStream.read(data)) != -1) {    //將inputStream寫入data並回傳字數到len
                        outputStream.write(data, 0, len);            //將data寫入到輸出流中,參數二為起始位置,len是讀取長度
                    }
                    result = new String(outputStream.toByteArray(), encode);    //resilt取得outputStream的string並轉成encode邊碼
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("text", "Http_Client.changeInputStream.IOException="+e.toString());
                }
            }
            return result;                //回傳result
        }
    }
}
