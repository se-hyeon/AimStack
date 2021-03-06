package com.example.sehyeon.aimstack;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class OngoingPage extends AppCompatActivity {

    String TAG = "ongoing page";
    private SQLiteDatabase db;
    private OneAim aim;
    private Integer position;

    private Timer timer;
    private TimerTask timerTask;
    private int hour, minute, sec;
    private String time;
    private int totalSec;

    private TextView timerTextView;
    private TextView ongoingTitle;
    private Button stopButton;

    private boolean notiFlag = false;

    // custom view
    com.example.sehyeon.aimstack.ProgressCircle progressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_ongoing_page);

        Bundle bundle = getIntent().getExtras();

        aim = new OneAim();
        db = openOrCreateDatabase("myDatabase", MODE_WORLD_READABLE, null);
        position = (Integer) bundle.get("thisAim");

        loadItem();

        timer = new Timer();
        timerTextView = (TextView) findViewById(R.id.timer);

        ongoingTitle = (TextView) findViewById(R.id.ongoingTitle);
        ongoingTitle.setText(aim.getTitle());

        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                updateItem();
            }
        });


        progressCircle = (com.example.sehyeon.aimstack.ProgressCircle) findViewById(R.id.progressView);
        progressCircle.setDoingSec(Integer.parseInt(aim.getDoingSec()));
        progressCircle.setTotalSec(Integer.parseInt(aim.getAimSec()));

        Log.d("check", getToday() + " / " + aim.getEndDaySecond());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        updateItem();
    }

    @Override
    protected void onPause(){
        notiFlag=true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notiFlag=false;
        if (getToday() <= Long.parseLong(aim.getEndDaySecond())) {
            setTimer();
        } else
            timerTextView.setText("기간 끝");
    }

    private void loadItem() {

        int recordCount;
        db = null;
        db = openOrCreateDatabase("myDatabase", MODE_WORLD_READABLE, null);
        Cursor cursor = db.rawQuery("select * from myTable ", null);

        recordCount = cursor.getCount();
        //    Log.d("-----", "count of all items : " + recordCount);

        if (recordCount < 1) {
            return;
        }

        for (int i = 0; i < position + 1; i++)
            cursor.moveToNext();

        aim.setTitle(cursor.getString(0));
        aim.setTime(cursor.getString(1));
        aim.setStartDaySecond(cursor.getString(2));
        aim.setEndDaySecond(cursor.getString(3));
        aim.setDoingSec(cursor.getString(4));

        Log.d("--", "title : " + cursor.getString(0) + ", " + cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3) + ", " + cursor.getString(4));

        cursor.close();
    }
    private void updateItem() {
        aim.setDoingSec(totalSec + "");

        String sql = "update myTable set doing_sec=" + totalSec
                + " WHERE title='" + aim.getTitle()
                + "' and time='" + aim.getAimSec()
                + "' and start_second='" + aim.getStartDaySecond()
                + "' and end_second='" + aim.getEndDaySecond()
                + "';";
        db.execSQL(sql);
        loadItem();
    }
    private void setTimer() {
        timer.cancel();
        timer = null;
        timer = new Timer();
        Log.i(TAG, "onResume");
        totalSec = Integer.parseInt(aim.getDoingSec());

        timerTask = new TimerTask() {
            @Override
            public void run() {

                hour = totalSec / 3600;
                minute = totalSec % 3600 / 60;
                sec = totalSec % 3600 % 60;

                time = hour + ":" + minute + ":" + sec;

                totalSec++;
                aim.setDoingSec(totalSec + "");

                progressCircle.setDoingSec(Integer.parseInt(aim.getDoingSec()));
                progressCircle.setTotalSec(Integer.parseInt(aim.getAimSec()));

                if(Integer.parseInt(aim.getDoingSec())*100/Integer.parseInt(aim.getAimSec())==100){
                    if(notiFlag==true)
                        notification();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerTextView.setText(time);
                        progressCircle.invalidate();
                        //      Log.d("timer", time);
                    }
                });

            }
        };
        timer.schedule(timerTask, 100, 1000);

    }
    public long getToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis() / 1000;
        Log.d("getToday", "" + today);
        return today;
    }
    public void notification() {

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Resources res = getResources();

        Intent notificationIntent = new Intent(this, OngoingPage.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("AimStack")
                .setContentText(aim.getTitle()+" 완료!")
                .setTicker(aim.getTitle()+" 완료!")
                .setSmallIcon(R.mipmap.aim_stack)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.aim_stack))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

        Notification  n = builder.build();
        nm.notify(1234, n);
    }

}
