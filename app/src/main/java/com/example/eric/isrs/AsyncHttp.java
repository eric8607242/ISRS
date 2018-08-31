package com.example.eric.isrs;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class AsyncHttp extends AsyncTask<Map<String, String>, Integer, String> {

    @Override
    protected String doInBackground(Map<String, String>... maps) {
        return null;
    }

    public static StringBuffer getRequestData(Map<String, String> params, String encode){
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for(Map.Entry<String, String> entry : params.entrySet()){
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public static String dealResponseResult(InputStream inputStream){
        String resultData = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;

        try{
            while((len = inputStream.read(data)) != -1){
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

    public String uploadImage(String filepath, String picNum, String userName) throws Exception{

        final String BOUNDARY = "==================================";
        final String HYPHENS = "--";
        final String CRLF = "\r\n";

        URL url = null;
        try {
//            url = new URL("http://10.0.2.2:8000/pic");
            url = new URL("http://18.218.154.134/mobile/recognition/"+userName+"/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        connection.setConnectTimeout(10 * 10000000);
        connection.setReadTimeout(10*10000000);

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");

        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary = " + BOUNDARY);

        String strContentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=\""+ picNum +".jpeg\"";
        String strContentType = "Content-Type:image/jpeg";

        connection.connect();

        DataOutputStream dataOS = new DataOutputStream(connection.getOutputStream());
        dataOS.writeBytes(HYPHENS + BOUNDARY + CRLF);
        dataOS.writeBytes(strContentDisposition + CRLF);
        dataOS.writeBytes(strContentType + CRLF);
        dataOS.writeBytes(CRLF);


        File file = new File(filepath);

        if(!file.exists()){
            return "file_failed";
        }

        Bitmap bm = BitmapFactory.decodeFile(filepath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, out);

        byte[] byteData = out.toByteArray();

        dataOS.write(byteData, 0 , byteData.length);

        dataOS.writeBytes(CRLF + HYPHENS + BOUNDARY + HYPHENS + CRLF);
        dataOS.flush();
        dataOS.close();

        int status = connection.getResponseCode();
        if(status == HttpURLConnection.HTTP_OK){
            InputStream inputStream = connection.getInputStream();

            connection.disconnect();
            inputStream.close();
            return dealResponseResult(inputStream);
        }else {
            connection.disconnect();
            throw new Exception("Non ok response returned");
        }
    }

    protected String validateAccount(Map<String, String>... maps) {
        Map<String, String> params = maps[0];

        byte[] data = getRequestData(params, "utf-8").toString().getBytes();

        URL url = null;
        try {
//            url = new URL("http://10.0.2.2:8000/account");
            url = new URL("http://18.218.154.134/mobile/check_login/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection httpURLConnection = null;

        try{
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);

            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            outputStream.write(data);
            outputStream.flush();

            int response = httpURLConnection.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK){
                InputStream inputStream = httpURLConnection.getInputStream();
                return dealResponseResult(inputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }


        return null;
    }

    public String getSheetInfo(String username){
        URL url = null;
        try {
            url = new URL("http://18.218.154.134/mobile/list/" + username + "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection httpURLConnection = null;

        try{
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            /* optional request header */
            httpURLConnection.setRequestProperty("Accept", "application/json");



            httpURLConnection.connect();

            int response = httpURLConnection.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK){
                InputStream inputStream = httpURLConnection.getInputStream();

                String s = dealResponseResult(inputStream);
                byte[] utf8 = s.getBytes("utf8");
                s = new String(utf8, "utf8");
                System.out.println(s);
                return s;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }
        return "null";
    }


}
