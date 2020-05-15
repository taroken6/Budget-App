package com.example.budgetapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class EditSpendingsActivity extends AppCompatActivity {
    private TextView dateTextView;
    private ImageButton calendarButton;
    private EditText amountTextBox;
    private Spinner typeSpinner;
    private Spinner categorySpinner;
    ArrayAdapter<CharSequence> categoryAdapter;
    private EditText notesTextBox;
    private Button okButton;
    private final String TAG = "Edit Spendings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_spendings);

        //region Type Spinner setup
        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.spending_type, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        //endregion

        dateTextView = (TextView) findViewById(R.id.dateTextView);
        calendarButton = (ImageButton) findViewById(R.id.calendarImageButton);
        amountTextBox = (EditText) findViewById(R.id.amountTextBox);
        categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        notesTextBox = (EditText) findViewById(R.id.notesTextBox);
        okButton = (Button) findViewById(R.id.okButton);

        Bundle data = getIntent().getExtras();
        SpendingsClass spendings = (SpendingsClass) data.getParcelable("spendings");

        double money = spendings.getMoney();
        String type = spendings.getType();
        String description = spendings.getDescription();
        String date = spendings.getDate();
        String category = spendings.getCategory();

        dateTextView.setText(date);
        amountTextBox.setText(String.valueOf(money));
        int typeSpinnerPosition = typeAdapter.getPosition(type);
        typeSpinner.setSelection(typeSpinnerPosition);
        getCategorySpinner(type);
        int categorySpinnerPosition = categoryAdapter.getPosition(category);
        categorySpinner.setSelection(categorySpinnerPosition);
        notesTextBox.setText(description);

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditSpendingsActivity.this,
                        CalendarActivity.class);
                startActivityForResult(intent, 2);
            }
        });
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                getCategorySpinner(selectedType);
                if (selectedType.equals(type)) {
                    categorySpinner.setSelection(categorySpinnerPosition);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double money = convertMoneyToDouble(amountTextBox.getText().toString());
                String type = typeSpinner.getSelectedItem().toString();
                String notes = notesTextBox.getText().toString();
                String date = dateTextView.getText().toString();
                String category = categorySpinner.getSelectedItem().toString();
                Intent intent = new Intent();
                intent.putExtra("edit_spendings",
                        new SpendingsClass(money, type, notes, date, category));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

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

    private void getCategorySpinner(String selectedType){
        String[] typesStringArray = getResources().getStringArray(R.array.spending_type);
        String liabilityStringResource = typesStringArray[0];
        String personalStringResource = typesStringArray[1];
        String savingsStringResource = typesStringArray[2];

        if(selectedType.equals(liabilityStringResource)){
            categoryAdapter = ArrayAdapter.createFromResource(this,
                    R.array.spending_liability_type, android.R.layout.simple_spinner_item);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
            Log.d(TAG, "Selected type = " + liabilityStringResource);
        } else if(selectedType.equals(personalStringResource)){
            categoryAdapter = ArrayAdapter.createFromResource(this,
                    R.array.spending_personal_type, android.R.layout.simple_spinner_item);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
            Log.d(TAG, "Selected type = " + personalStringResource);
        } else if (selectedType.equals(savingsStringResource)) {
            categoryAdapter = ArrayAdapter.createFromResource(this,
                    R.array.spending_savings_type, android.R.layout.simple_spinner_item);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
            Log.d(TAG, "Selected type = " + savingsStringResource);
        } else {
            Log.d(TAG, "Error at 'getCategorySpinner");
        }
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
