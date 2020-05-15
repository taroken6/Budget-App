package com.example.budgetapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class EditEarningsActivity extends AppCompatActivity {
    private final String TAG = "Edit Earnings Activity";
    private ImageButton calendarButton;
    private TextView dateTextView;
    private EditText amountEditText;
    private Spinner typeSpinner;
    private EditText notesEditText;
    private Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_earnings);

        //region Initialize
        calendarButton = findViewById(R.id.calendarImageButton);
        dateTextView = findViewById(R.id.dateTextView);
        amountEditText = findViewById(R.id.amountTextBox);
        typeSpinner = findViewById(R.id.typeSpinner);
        notesEditText = findViewById(R.id.notesTextBox);
        okButton = findViewById(R.id.okButton);
        //region Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.earnings_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        //endregion
        //endregion

        Bundle data = getIntent().getExtras();
        MoneyInput input = (MoneyInput) data.getParcelable("input");
        double money = input.getMoney();
        String type = input.getType();
        String description = input.getDescription();
        String date = input.getDate();
        Log.d(TAG, String.valueOf(money) + " " + type + " " + description + " " + date);

        dateTextView.setText(date);
        amountEditText.setText(String.valueOf(money));
        notesEditText.setText(description);
        int typeSpinnerPosition = adapter.getPosition(type);
        typeSpinner.setSelection(typeSpinnerPosition);
        //endregion

        //region Listeners
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditEarningsActivity.this,
                        CalendarActivity.class);
                startActivityForResult(intent, 2);
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double money = convertMoneyToDouble(amountEditText.getText().toString());
                String type = typeSpinner.getSelectedItem().toString();
                String notes = notesEditText.getText().toString();
                String date = dateTextView.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("edit_earnings", new MoneyInput(money, type, notes, date));
                setResult(Activity.RESULT_OK, intent);
                Log.i(TAG, type);
                finish();
            }
        });
        //endregion
    }

    public double convertMoneyToDouble(String input){
        String money = input.replaceAll("[^\\d.]", "");
        double returnValue;
        try{
            returnValue = Double.parseDouble(money);
        } catch(NumberFormatException e){
            returnValue = 0;
        }
        return returnValue;
    }

    //region Date methods
    private void displayDate(int[] date) {
        String dateString = getDateString(date);
        dateTextView.setText(dateString);
    }

    private String getDateString(int[] dateArray){
        int month = dateArray[0] + 1;
        int day = dateArray[1];
        int year = dateArray[2];
        Calendar date;
        String dateString = month + "/" + day + "/" + year;
        return dateString;
    }
    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 2) { // Retrieve from Calendar Activity
            if(resultCode == Activity.RESULT_OK){
                int[] date = intent.getIntArrayExtra("date");
                displayDate(date);
            }
        }
    }
}
