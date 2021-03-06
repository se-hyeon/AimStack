package com.example.sehyeon.aimstack;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddNewAimPage extends AppCompatActivity {

    EditText aimNameEditText;
    EditText aimHourEditText;
    EditText aimMinuteEditText;
    Button startDayButton;
    Button endDayButton;
    Button saveAimButton;

    String aimName;
    String aimHour;
    String aimMinute;

    int startYear;
    int startMonth;
    int startDay;

    int endYear;
    int endMonth;
    int endDay;

    long startDayMillisec;
    long endDayMillisec;

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date selectedDate;

    final int START_DAY_FLAG = 1;
    final int END_DAY_FLAG = 2;
    int DATE_SETTING_BUTTON_FLAG;
    int ERROR_FLAG;

    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_aim_page);

        aimNameEditText = (EditText) findViewById(R.id.aimEditText);
        aimHourEditText = (EditText) findViewById(R.id.hourEditText);
        aimMinuteEditText = (EditText)findViewById(R.id.minuteEditText);

        startDayButton = (Button) findViewById(R.id.startDayButton);
        startDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DATE_SETTING_BUTTON_FLAG = START_DAY_FLAG;
                showDialog(START_DAY_FLAG);
            }
        });

        endDayButton = (Button) findViewById(R.id.endDayButton);
        endDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DATE_SETTING_BUTTON_FLAG = END_DAY_FLAG;
                showDialog(END_DAY_FLAG);
            }
        });

        saveAimButton = (Button) findViewById(R.id.saveNewAimButton);
        saveAimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aimName = aimNameEditText.getText().toString();
                aimHour = aimHourEditText.getText().toString();
                aimMinute = aimMinuteEditText.getText().toString();

                db = openOrCreateDatabase("myDatabase", MODE_WORLD_READABLE, null);

                if (!aimName.equals("") && !aimHour.equals("") && !aimMinute.equals("")) {
                    if (startDayMillisec <= endDayMillisec) {

                        Log.d("NewAim", Long.parseLong(aimHour) * 3600 + Long.parseLong(aimMinute) * 60 + "");
                        long aimSec = Long.parseLong(aimHour)*3600+Long.parseLong(aimMinute)*60;
 //                       insertRecord(db, aimName, aimSec+"", startDayMillisec/1000+"",endDayMillisec/1000+"",0+"");
                        insertRecord(db, aimName, aimSec, startDayMillisec/1000,endDayMillisec/1000,0l);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "시간을 거슬러 갈 수는 없어요!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "항목을 모두 기입해 주세요!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        String dateString;
        switch (id) {
            case START_DAY_FLAG:
                dateString = startDayButton.getText().toString();
                break;
            case END_DAY_FLAG:
                dateString = endDayButton.getText().toString();
                break;
            default:
                dateString = null;
                break;
        }
        Calendar calendar = Calendar.getInstance();
        Date currentDate = new Date();
        try {
            currentDate = dateFormat.parse(dateString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        calendar.setTime(currentDate);


        switch (id) {
            case START_DAY_FLAG:
                startYear = calendar.get(Calendar.YEAR);
                startMonth = calendar.get(Calendar.MONTH) + 1;
                startDay = calendar.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, dateSetListener, startYear, startMonth-1, startDay);
            case END_DAY_FLAG:
                endYear = calendar.get(Calendar.YEAR);
                endMonth = calendar.get(Calendar.MONTH) + 1;
                endDay = calendar.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, dateSetListener, endYear, endMonth-1, endDay);
            default:
                return null;
        }
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, monthOfYear);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedCalendar.set(Calendar.HOUR, 0);
            selectedCalendar.set(Calendar.MINUTE, 0);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);

            Log.d("date", selectedCalendar.getTimeInMillis() + "");

            switch (DATE_SETTING_BUTTON_FLAG) {
                case START_DAY_FLAG:
                    startDayMillisec = selectedCalendar.getTimeInMillis();
                    break;
                case END_DAY_FLAG:
                    endDayMillisec = selectedCalendar.getTimeInMillis();
                    break;
            }
            Date curDate = selectedCalendar.getTime();
            setSelectedDate(curDate);


        }
    };

    private void setSelectedDate(Date curDate) {
        selectedDate = curDate;
        String selectedDateStr = dateFormat.format(curDate);
        switch (DATE_SETTING_BUTTON_FLAG) {
            case START_DAY_FLAG:
                startDayButton.setText(selectedDateStr);
                break;
            case END_DAY_FLAG:
                endDayButton.setText(selectedDateStr);
                break;
        }
    }

    private void insertRecord(SQLiteDatabase _db, String title, String time,  String  startDayMillisec, String endDayMillisec, String doingSec) {
        try {
            _db.execSQL("insert into " + "myTable"
                    + "(TITLE, TIME, START_SECOND, END_SECOND, DOING_SEC) values ('" + title + "', '" + time + "', '"   + startDayMillisec + "','" + endDayMillisec + "', '" + doingSec + "');");
            Log.d("OH!!!!", "insert!!!");
        } catch (Exception ex) {
            Log.e("fail to insert!", "Exception in executing insert SQL.", ex);
        }
        _db.close();
    }
    private void insertRecord(SQLiteDatabase _db, String title, Long time,  Long  startDayMillisec, Long endDayMillisec, Long doingSec) {
        try {
            _db.execSQL("insert into " + "myTable"
                    + "(TITLE, TIME, START_SECOND, END_SECOND, DOING_SEC) values ('" + title + "', '" + time + "', '"   + startDayMillisec + "','" + endDayMillisec + "', '" + doingSec + "');");
            Log.d("OH!!!!", "insert!!!");
        } catch (Exception ex) {
            Log.e("fail to insert!", "Exception in executing insert SQL.", ex);
        }
        _db.close();
    }

}
