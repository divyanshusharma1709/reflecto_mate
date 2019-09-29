package com.sgih.dialogactivity;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class UploadAsyncTask extends AsyncTask<String, Void, String> {
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    String serverResponseMessage = "";
    String diary;


    UploadAsyncTask(String s) {
        diary = s;
    }
//    private String uploadWeights() throws MalformedURLException {
//        URL url;
//        HttpsURLConnection conn;
//
//        url = new URL("https://sgihserver.herokuapp.com/upload");
//        try {
//            File file = new File(getAc().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), name);
//            if (!file.exists()) {
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    Log.i("Error: FILE", "File not Created!");
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return serverResponseMessage;
//    }
//    String lineEnd = "\r\n";
//    String twoHyphens = "--";
//    String boundary = "*****";
//    String serverResponseMessage = "";
//
//    private void uploadWeight(HttpURLConnection conn) {
//        try {
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Connection", "Keep-Alive");
//            conn.setRequestProperty("ENCTYPE",
//                    "UTF-8");
//            conn.setRequestProperty("Content-Type",
//                    "application/json;boundary=" + boundary);
//            conn.setRequestProperty("file", "weights");
//
//            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
//            dos.writeBytes(twoHyphens + boundary + lineEnd);
//            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
//                    + "file" + "\"" + lineEnd);
//
//            dos.writeBytes(lineEnd);
//            //Write File
//            dos.write(diary.getBytes(StandardCharsets.UTF_8));
//
//            dos.writeBytes(lineEnd);
//            dos.writeBytes(twoHyphens + boundary + twoHyphens
//                    + lineEnd);
//            int serverResponseCode = conn.getResponseCode();
//            serverResponseMessage = conn.getResponseMessage();
//            Log.i("Response Message: ", serverResponseMessage);
//            Log.i("Response Code: ", String.valueOf(serverResponseCode));
//            dos.flush();
//            dos.close();
//        } catch (ProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected String doInBackground(String... strings) {
        String urlString = "https://sgihserver.herokuapp.com/upload"; // URL to call
        String data = diary; //data to post
        OutputStream out = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.setRequestProperty("ENCTYPE",
                    "multipart/form-data");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("file", "weights");
            out = new BufferedOutputStream(conn.getOutputStream());
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                    + "weights.bin" + "\"" + lineEnd);

            dos.writeBytes(lineEnd);
            //Write File
            dos.write(diary.getBytes(StandardCharsets.UTF_8));

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);
            out.close();
            conn.connect();
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            conn.disconnect();
            Log.i("Response: ", serverResponseMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "Success";
    }
}
