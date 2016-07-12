package com.example.jura.myapplication;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.List;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by Jura on 11.06.2016.
 */
public class AsyncTasks {
    final static int SERVER_CODE_LIST = 1;
    final static int SERVER_CODE_DURATION = 2;
    final static int SERVER_CODE_POSITION = 3;
    final static int SERVER_CODE_PLAYBACK = 4;
    final static int SERVER_CODE_PLAYBACK_STOP = 5;

    static class HttpGetResult {
        String message;
        int code;
        HttpGetResult(int xcode, String xmessage) {
            code = xcode;
            message = xmessage;
        }
    }

    private static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public static class HttpGetCookie extends AsyncTask<Object, String, List<String>> {
        JuraActivity mainClass;

        @Override
        protected List<String> doInBackground(Object... params) {
            String url = (String) params[0];
            mainClass = (JuraActivity) params[1];

            URL net_url;
            HttpURLConnection urlConnection;
            List<String> cookies;
            String cookie;
            List<String> res = new ArrayList<String>();

            try {
                net_url = new URL(url);
            } catch (MalformedURLException e) {
                res.add("exception");
                res.add(e.toString()+url);
                return res;
            }

            try {
                urlConnection = (HttpURLConnection) net_url.openConnection();
                urlConnection.setConnectTimeout(10*1000);
                urlConnection.connect();
                cookies = urlConnection.getHeaderFields().get("Set-Cookie");
                urlConnection.disconnect();

                if (cookies == null) {
                    res.add("exception");
                    res.add("cookies are null"+url);
                    return res;
                }

                if (cookies.isEmpty()) {
                    res.add("exception");
                    res.add("cookies emty"+url);
                    return res;
                }
                cookie = cookies.get(0).split(";")[0];
                if ((cookie.length() < 5) && !cookie.substring(0,3).equals("user")) {
                    res.add("exception");
                    res.add("malformed cookie"+url);
                    return res;
                }
            } catch (IOException e) {
                res.add("exception");
                res.add(e.toString()+url);
                return res;
            }

            res.add(url);
            res.add(cookie);
            return res;
        }

        protected void onPostExecute(List<String> result) {
            mainClass.setWorkStatus(false);
            if (result.isEmpty())
                return;
            if (result.get(0).equals("exception")) {
                //mainClass.setDebugView(result.get(1));
                return;
            }
            mainClass.setTextView("got cookie"+result.get(1));
            mainClass.setParams(result);
            //got cookie, now get root dir
            mainClass.startAsyncTask("app");
        }
    }

    public static class HttpGet extends AsyncTask<Object, String, HttpGetResult> {
        JuraActivity mainClass;

        @Override
        protected HttpGetResult doInBackground(Object... params) {
            String url = (String)params[0];
            String appCookie = (String)params[1];
            mainClass = (JuraActivity) params[2];

            URL net_url;
            HttpURLConnection urlConnection;
            String s = "didn't trigger";
            int code = 0;

            try {
                net_url = new URL(url);
            } catch (MalformedURLException e) {
                return new HttpGetResult(1, e.toString());
            }

            try {
                urlConnection = (HttpURLConnection) net_url.openConnection();
                urlConnection.addRequestProperty("Cookie", appCookie);
                urlConnection.connect();
                code = urlConnection.getResponseCode();
                if (code == 401) {
                    urlConnection.disconnect();
                    return new HttpGetResult(code, "");
                }
            } catch (IOException e) {
                return new HttpGetResult(1, e.toString());
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                s = readStream(in);
                publishProgress(s);
                urlConnection.disconnect();
            } catch (IOException e) {
                urlConnection.disconnect();
                return new HttpGetResult(1, e.toString());
            }

            return new HttpGetResult(code, s);
        }
        protected void onProgressUpdate(String... progress) {
            if (progress[0].length() == 0){}
                //mainClass.setDebugView("empty string!");
            //mainClass.setDebugView(progress[0]);
        }

        protected void onPostExecute(HttpGetResult result) {
            mainClass.setWorkStatus(false);
            if (result.code == 401) {
                //cookie wasn't found on the server (restarted), get new cookie
                mainClass.startAsyncTaskCookie();
            }
            mainClass.setDebugView(result.message);
            if (result.message.isEmpty()) {
                mainClass.setDebugView("Empty!");
                return;
            }
            try {
                JSONObject reader = new JSONObject(result.message);
                //mainClass.setDebugView(reader.getString("code"));
                int code = reader.getInt("code");
                mainClass.setDebugView(String.valueOf(code));
                switch (code) {
                    case SERVER_CODE_DURATION:
                        mainClass.setDuration(reader.getInt("duration"));
                        mainClass.setDebugView(Integer.toString(reader.getInt("duration")));
                        break;
                    case SERVER_CODE_LIST:
                        mainClass.buildTable(reader.getString("list"));
                        break;
                    case SERVER_CODE_POSITION:
                        mainClass.setSeekBarProgress(reader.getInt("position"));
                        break;
                    case SERVER_CODE_PLAYBACK:
                        mainClass.setSeekBarProgress();
                        mainClass.startAsyncTaskDuration();
                        mainClass.startSyncPosition();
                        break;
                    case SERVER_CODE_PLAYBACK_STOP:
                        mainClass.playbackStopped()
                        break;
                }
            } catch (JSONException e) {
                mainClass.setDebugView(e.toString());
                return;
            }



            /*else if (result.code == 201) {
                mainClass.buildTable(result.message);
            }*/

        }
    }

    public static class GetDuration extends AsyncTask<Object, Void, Void> {
        JuraActivity mainClass;

        @Override
        protected Void doInBackground(Object... params) {
            mainClass = (JuraActivity) params[0];
            SystemClock.sleep(1000);
            return null;
        }

        protected void onPostExecute(Void result) {
            mainClass.setWorkStatus(false);
            mainClass.startAsyncTask("duration");
        }
    }
}
