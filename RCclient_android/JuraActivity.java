package com.example.jura.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.util.TypedValue;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.TableLayout;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.net.URLEncoder;

public class JuraActivity extends AppCompatActivity {

    private TextView debugView;
    private TextView textView;
    private TableLayout tableLayout;
    private SeekBar seekBar;
    private SeekBarListener seekBarListener;


    private String serverIp = "";
    private String appCookie = "";
    private boolean inWork = false;
    
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> syncPosHandle;
    private boolean playbackInProgress = false;

    private int duration = 0;

    //public interaction
    public void setParams(List<String> params) {
        serverIp = params.get(0);
        appCookie = params.get(1);
    }
    public String getServerIp() { return serverIp; }
    public void setDuration(int seconds) { duration = seconds; }
    public void setWorkStatus(boolean status) {
        inWork = status;
        if(status)
            textView.setText("in work");
        else
            textView.setText("not in work");

    }
    public void setDebugView(String s) {
        debugView.setText(s);
    }
    public void setTextView(String s) {
        //textView.setText(textView.getText().toString() + '\t'+ s);
    }
    public void resetSeekBarProgress() {
        seekBar.setProgress(0);
    }
    public void setSeekBarProgress(int pos) {
        percent = (pos / duration) * seekBar.getMax()
        seekBar.setProgress(percent);
    }
    public void playbackStopped() {
        resetSeekBarProgress();
        duration = 0;
        
        if (!syncPosHandle.isDone()) {
            syncPosHandle.cancel(false);
        }
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jura);

        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        tableLayout.setColumnStretchable(0, true);
        //buildTable("");

        seekBar =  (SeekBar) findViewById(R.id.seekBar);
        seekBarListener = new SeekBarListener();
        seekBarListener.init(this);
        seekBar.setOnSeekBarChangeListener(seekBarListener);

        //debug
        debugView = (TextView) findViewById(R.id.textView2);
        textView = (TextView) findViewById(R.id.textView);
        textView.setOnClickListener(debug);
        textView.setClickable(true);
        //

        findServer();
    }
    
    @Override 
    protected void onStop() {
        super.onStop();
        
        if (!syncPosHandle.isDone()) {
            playbackInProgress = true;
            syncPosHandle.cancel(false);
        }
    }
    
    @Override 
    protected void onRestart() {
        super.onRestart();
        
        if (playbackInProgress)
            startSyncPosition();
    }
    
    private void findServer() {
        serverIp = "http://192.168.199.14:5000";
        startAsyncTaskCookie();

        /*
        StringBuilder address = new StringBuilder("http://192.168.199.10:5000");
        for (int i = 0; i < 10; i++) {
            address.setCharAt(20, (char)(0x30+i));
            AsyncTasks.HttpGetCookie httpGetCookie = new AsyncTasks.HttpGetCookie();
            httpGetCookie.execute(address.toString(), this);
        }*/
    }

    private View.OnClickListener fsClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            String s = tv.getText().toString();
            String arg;

            try {
                arg = URLEncoder.encode(s, "utf-8");
                startAsyncTask("app?arg="+arg);
            } catch(UnsupportedEncodingException e) {

            }
        }
    };

    View.OnClickListener debug = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startAsyncTask("duration");
        }
    };

    void buildTable(String serverResponse) {
        tableLayout.removeAllViews();

        String[] entities = serverResponse.split("\n");
        for (int i = 0; i < entities.length+1; i++) {
            TableRow row = new TableRow(this);
            row.setMinimumHeight(90);
            TextView tv = new TextView(this);
            tv.setTextSize(22);
            if (i == 0)
                tv.setText("..");
            else
                tv.setText(entities[i-1]);
            tv.setOnClickListener(fsClick);

            //ripple effect
            TypedValue outValue = new TypedValue();
            tv.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            tv.setBackgroundResource(outValue.resourceId);

            row.addView(tv);
            tableLayout.addView(row,i);
        }
    }

    /////////// API /////////////

    public void startAsyncTask(String url) {
        if (inWork)
            return;
        setWorkStatus(true);
        AsyncTasks.HttpGet httpGet = new AsyncTasks.HttpGet();
        httpGet.execute(serverIp+"/"+url, appCookie, this);
    }
    public void startAsyncTaskCookie() {
        if (inWork)
            return;
        setWorkStatus(true);
        AsyncTasks.HttpGetCookie httpGetCookie = new AsyncTasks.HttpGetCookie();
        httpGetCookie.execute(serverIp, this);
    }
    public void startAsyncTaskDuration() {
        if (inWork)
            return;
        setWorkStatus(true);
        AsyncTasks.GetDuration getDuration = new AsyncTasks.GetDuration();
        getDuration.execute(this);
    }
    public void serverKillAll(View v) {
        startAsyncTask("killall");
        duration = 0;
    }
    public void serverPause(View v) {
        startAsyncTask("pause");
    }
    public void setPosition(int pos) {
        if (duration == 0)
            return;
        int newPos = duration * pos;
        startAsyncTask("setposition?arg="+Integer.toString(newPos)+"0000");
    }
    public void startSyncPosition() {
        final Runnable syncPosition = new Runnable() {
            public void run() { startAsyncTask("syncPosition"); }
        };
        syncPosHandle =
            scheduler.scheduleWithFixedDelay(syncPosition, 1, 1, SECONDS);
    }
    public void Sync(View v) {
        findServer();
    }
}
