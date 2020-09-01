package com.example.emotechs;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UploadTask extends AsyncTask<String,Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        try{
            JSONObject js = new JSONObject();
            js.put("idtoken", strings[0]);
            URL url = new URL("http://af4943e5c2c4.ngrok.io");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            String message = js.toString();
            String data = URLEncoder.encode("postData,", "UTF-8") + "=" + URLEncoder.encode(message, "UTF-8");


            OutputStreamWriter outr = new OutputStreamWriter(urlConnection.getOutputStream());
            outr.write(data);
            outr.flush();

            int resCode = urlConnection.getResponseCode();
            Log.d("result_code", Integer.toString(resCode));

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);

            String line;
            while((line = reader.readLine()) != null) {
                Log.i("recieved ...", line);
            }
            urlConnection.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "Done";
    }
}