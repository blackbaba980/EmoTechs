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

public class ServerCommunicationHandler extends AsyncTask<String,Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        try{
            JSONObject js = new JSONObject();
            js.put("VideoURL", strings[0]);
            URL url = new URL("http://ab5976420850.ngrok.io/evaluation");
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
                Log.e("recieved ...", line);
                if(line.indexOf("Counter=") == 0){
                    String countNumber = line.split("Counter=")[1];
                    Log.e("recieved ...", "Counter = " + countNumber);
                }

            }
            urlConnection.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "Done";
    }
}
