package com.nutccsie.nutc_fds;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by vpro16513428 on 2016/5/14.
 */

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
        try {
            //打開服務器
            HttpURLConnection hucn=(HttpURLConnection) url.openConnection();
            hucn.setReadTimeout(5000);            //設置讀取超時為5秒
            hucn.setConnectTimeout(10000);        //設置連接網路超時為10秒
            //設置輸出入流串
            if(method!="GET"){
                hucn.setDoOutput(true);                //可寫入資料至伺服器
                hucn.setDoInput(true);                //可從伺服器取得資料
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
            else{
                hucn.setDoInput(true);                //可從伺服器取得資料
            }

            //設置請求的方法為POST
            hucn.setRequestMethod(method);
            //POST方法不能緩存數據,需手動設置使用緩存的值為false
            hucn.setUseCaches(false);
            //連接資料庫
            //hucn.connect();    //如使用調用getResponseCode()判斷是否為200 就不必使用connect()
            //寫入參數


            //判斷是否請求成功,為200時表示成功,其他均有問題
            if(hucn.getResponseCode() == 200){
                //取得回傳的inputStream (輸入流串)
                InputStream inputStream = hucn.getInputStream();
                return changeInputStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("text","HttpURLConnection_POST="+e.toString());
        }
        return "";
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
