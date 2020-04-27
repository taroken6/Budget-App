package com.example.budgetappattempt2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        CalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            /**
             * Returns array of selected date as {Month, Day, Year}
             * @param view
             * @param year
             * @param month
             * @param dayOfMonth
             */
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                int[] date = {month, dayOfMonth, year};
                Intent intent = new Intent();
                intent.putExtra("date", date);
                setResult(Activity.RESULT_OK, intent);
                Log.d("Calendar Activity", month + "-" + dayOfMonth + "-" + year);
                finish();
            }
        });
    }
}
