package com.example.budgetappattempt2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class BudgetActivity extends AppCompatActivity {
    private final String TAG = "Budget Activity";
    private String budgetArrayFilePath;
    private String budgetLiabilityPath;
    private final String LIABILITY_FILE_NAME = "/BudgetLiability";
    private BudgetClass[] budgetLiability = new BudgetClass[4];
    private String budgetPersonalPath;
    private final String PERSONAL_FILE_NAME = "/BudgetPersonal";
    private BudgetClass[] budgetPersonal = new BudgetClass[4];
    private String budgetSavingsPath;
    private final String SAVINGS_FILE_NAME = "/BudgetSavings";
    private BudgetClass[] budgetSavings = new BudgetClass[4];
    private int timeIntervalIndex = 0;
    private TextView moneyTextView;
    private double[] earnings;
    private int[] dateArray;

    private TextView dateTextView;
    private EditText liabilityEditText;
    private TextView liabilityTotalTextView;
    private Spinner liabilitySpinner;
    private EditText personalEditText;
    private TextView personalTotalTextView;
    private Spinner personalSpinner;
    private EditText savingsEditText;
    private TextView savingsTotalTextView;
    private Spinner savingsSpinner;

    final DecimalFormat DECI_FORMAT = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        Log.d(TAG, "Budget Activity onCreate");

        getBudgetArray(); //Either return a serialized or blank budget array

        //region setup spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.budget_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        liabilitySpinner = (Spinner) findViewById(R.id.liabilitySpinner);
        personalSpinner = (Spinner) findViewById(R.id.personalSpinner);
        savingsSpinner = (Spinner) findViewById(R.id.savingsSpinner);
        liabilitySpinner.setAdapter(adapter);
        personalSpinner.setAdapter(adapter);
        savingsSpinner.setAdapter(adapter);
        //endregion

        dateArray = getDate();
        dateTextView = findViewById(R.id.dateTextView);
        ImageButton timeIntervalLeftBtn = (ImageButton) findViewById(R.id.timeIntervalLeftBtn);
        ImageButton timeIntervalRightBtn = (ImageButton) findViewById(R.id.timeIntervalRightBtn);
        liabilityEditText = (EditText) findViewById(R.id.liabilityEditText);
        personalEditText = (EditText) findViewById(R.id.personalEditText);
        savingsEditText = (EditText) findViewById(R.id.savingsEditText);
        liabilityTotalTextView = (TextView) findViewById(R.id.liabilityTotal);
        personalTotalTextView = (TextView) findViewById(R.id.personalTotal);
        savingsTotalTextView = (TextView) findViewById(R.id.savingsTotal);
        moneyTextView = (TextView) findViewById(R.id.moneyTextView);

        setupUI();

        //region Listeners
        //region Text Change Listeners
        liabilityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){ //Doing the same via 'displayTotal' method would crash
                    Double doubleTotal = Double.valueOf(s.toString());
                    String stringTotal = DECI_FORMAT.format(doubleTotal);
                    BudgetClass budget = budgetLiability[timeIntervalIndex];
                    budget.setInput(doubleTotal);
                    if(budget.getType() == null){
                        liabilityTotalTextView.setText("Null");
                    } else if(budget.getType().equals("Total")){
                        liabilityTotalTextView.setText("$" + stringTotal);
                    } else if(budget.getType().equals("%")){
                        Double earningsTotal = earnings[timeIntervalIndex];
                        String totalOutput = "$" + DECI_FORMAT.format(
                                earningsTotal * doubleTotal / 100);
                        liabilityTotalTextView.setText(totalOutput);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                serializeBudgetArray(budgetLiability, budgetLiabilityPath);
            }
        });
        personalEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){ //Doing the same via 'displayTotal' method would crash
                    Double doubleTotal = Double.valueOf(s.toString());
                    String stringTotal = DECI_FORMAT.format(doubleTotal);
                    BudgetClass budget = budgetPersonal[timeIntervalIndex];
                    budget.setInput(doubleTotal);
                    if(budget.getType() == null){
                        personalTotalTextView.setText("Null");
                    } else if(budget.getType().equals("Total")){
                        personalTotalTextView.setText("$" + stringTotal);
                    } else if(budget.getType().equals("%")){
                        Double earningsTotal = earnings[timeIntervalIndex];
                        String totalOutput = "$" + DECI_FORMAT.format(
                                earningsTotal * doubleTotal / 100);
                        personalTotalTextView.setText(totalOutput);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                serializeBudgetArray(budgetPersonal, budgetPersonalPath);
            }
        });
        savingsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty()){ //Doing the same via 'displayTotal' method would crash
                    Double doubleTotal = Double.valueOf(s.toString());
                    String stringTotal = DECI_FORMAT.format(doubleTotal);
                    BudgetClass budget = budgetSavings[timeIntervalIndex];
                    budget.setInput(doubleTotal);
                    if(budget.getType() == null){
                        savingsTotalTextView.setText("Null");
                    } else if(budget.getType().equals("Total")){
                        savingsTotalTextView.setText("$" + stringTotal);
                    } else if(budget.getType().equals("%")){
                        Double earningsTotal = earnings[timeIntervalIndex];
                        String totalOutput = "$" + DECI_FORMAT.format(
                                earningsTotal * doubleTotal / 100);
                        savingsTotalTextView.setText(totalOutput);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                serializeBudgetArray(budgetSavings, budgetSavingsPath);
            }
        });
        //endregion
        //region Spinners
        liabilitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                budgetLiability[timeIntervalIndex].setType(selected);
                displayTotal(liabilityEditText, liabilityTotalTextView, budgetLiability);
                serializeBudgetArray(budgetLiability, budgetLiabilityPath);
                Log.d(TAG, "Liability Type selected = " + selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        personalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                budgetPersonal[timeIntervalIndex].setType(selected);
                displayTotal(personalEditText, personalTotalTextView, budgetPersonal);
                Log.d(TAG, "Personal Type selected = " + selected);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        savingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                budgetSavings[timeIntervalIndex].setType(selected);
                displayTotal(savingsEditText, savingsTotalTextView, budgetSavings);
                Log.d(TAG, "Savings Type selected = " + selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //endregion
        //region Time Interval Buttons
        timeIntervalLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(true);
                Log.i("Earnings Activity", "Right Button clicked!");
                setupUI();
            }
        });
        timeIntervalRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(false);
                Log.i("Earnings Activity", "Right Button clicked!");
                setupUI();
            }
        });
        //endregion
        //endregion
    }

    //region general methods
    private void setupUI(){
        Intent intent = getIntent();

        //region Setup date
        displayDate();
        //endregion

        //region Setup Time Interval
        TextView timeIntervalTextView = (TextView) findViewById(R.id.timeIntervalTextView);
        String timeIntervalName = "time_interval" + timeIntervalIndex;
        int timeIntervalID = getTimeIntervalID(timeIntervalName);
        timeIntervalTextView.setText(getString(timeIntervalID));
        //endregion

        //region Setup total earnings
        earnings = intent.getExtras().getDoubleArray("earnings");
        moneyTextView.setText("$" + DECI_FORMAT.format(earnings[timeIntervalIndex]));

        String debugString = "";
        for(int i = 0; i < earnings.length; i++){
            debugString += String.valueOf(earnings[i]) + " ";
        }
        Log.d(TAG, "Earnings total received " + debugString);
        //endregion

        //region Setup spinner type
        setSpinnerType(liabilitySpinner, budgetLiability);
        setSpinnerType(personalSpinner, budgetPersonal);
        setSpinnerType(savingsSpinner, budgetSavings);
        //endregion

        //region Setup totals
        displayTotal(liabilityEditText, liabilityTotalTextView, budgetLiability);
        displayTotal(personalEditText, personalTotalTextView, budgetPersonal);
        displayTotal(savingsEditText, savingsTotalTextView, budgetSavings);
        //endregion
    }

    public void displayTotal(EditText textBox, TextView textTotal, BudgetClass[] budgetArray){
        BudgetClass budget = budgetArray[timeIntervalIndex];
        String inputString = String.valueOf(budget.getInput());
        String inputType = budget.getType();
        Double earningsTotal = earnings[timeIntervalIndex];
        textBox.setText(inputString);

        Log.d(TAG, "Display Total: inputType = " + inputType);

        if(inputType == null){
            textTotal.setText("Type == Null");
        } else if(inputType.equals("Total")){
            textTotal.setText("$" + DECI_FORMAT.format(budget.getInput()));
        } else if (inputType.equals("%")){
            double total = earningsTotal * budget.getInput() / 100;
            textTotal.setText("$" + DECI_FORMAT.format(total));
        } else{
            textTotal.setText("Error");
        }
    }

    private void setSpinnerType(Spinner spinner, BudgetClass[] budget){
        if(budget[timeIntervalIndex].getType().equals("Total")){
            spinner.setSelection(1);
        } else {
            spinner.setSelection(0);
        }
    }

    private void getBudgetArray(){
        // Get either an existing BudgeArray file or create a new one
        budgetLiabilityPath = getFilesDir() + LIABILITY_FILE_NAME;
        budgetPersonalPath = getFilesDir() + PERSONAL_FILE_NAME;
        budgetSavingsPath = getFilesDir() + SAVINGS_FILE_NAME;
        if (new File(budgetLiabilityPath).exists()) {
            budgetLiability = deserializeBudgetArray(budgetLiabilityPath);
            Log.d(TAG, "Liability Budget file exists!");
        } else {
            for (int i = 0; i < budgetLiability.length; i++) {
                budgetLiability[i] = new BudgetClass(0, "%");
            }
            Log.d(TAG, "Liability Budget file missing... Returning new Budget Array");
        }
        if (new File(budgetPersonalPath).exists()) {
            budgetPersonal = deserializeBudgetArray(budgetPersonalPath);
            Log.d(TAG, "Personal Budget file exists!");
        } else {
            for (int i = 0; i < budgetLiability.length; i++) {
                budgetPersonal[i] = new BudgetClass(0, "%");
            }
            Log.d(TAG, "Personal Budget file missing... Returning new Budget Array");
        }
        if (new File(budgetSavingsPath).exists()) {
            budgetSavings = deserializeBudgetArray(budgetSavingsPath);
            Log.d(TAG, "Liability Budget file exists!");
        } else {
            for (int i = 0; i < budgetLiability.length; i++) {
                budgetSavings[i] = new BudgetClass(0, "%");
            }
            Log.d(TAG, "Savings Budget file missing... Returning new Budget Array");
        }
    }
    //endregion

    //region Date methods
    private void displayDate(){
        String dateString = getDateTextViewString(dateArray);
        dateTextView.setText(dateString);
    }

    private String getDateTextViewString(int[] selectedDateArray) {
        int month = selectedDateArray[0] + 1;
        int day = selectedDateArray[1];
        int year = selectedDateArray[2];
        Calendar date;
        String dateText = null;

        if (timeIntervalIndex == 0) {
            dateText = month + "/" + day + "/" + year;
        } else {
            date = new GregorianCalendar(year, month - 1, day);
            if (timeIntervalIndex == 1) {
                int dayOfWeek = (date.get(Calendar.DAY_OF_WEEK) - 1);
                date.add(Calendar.DAY_OF_MONTH, -dayOfWeek); // Set date to first day of week (SUN)
                String sundayText = (date.get(Calendar.MONTH) + 1) + "/" +
                        date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR);
                date.add(Calendar.DAY_OF_MONTH, 6); // Set date to last day of week (SAT)
                String saturdayText = (date.get(Calendar.MONTH) + 1) + "/" +
                        date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR);
                dateText = sundayText + " - " + saturdayText;
            } else if (timeIntervalIndex == 2) {
                dateText = date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            } else if (timeIntervalIndex == 3) {
                dateText = String.valueOf(date.get(Calendar.YEAR));
            }
        }
        return dateText;
    }

    public int[] getDate(){
        int[] calendarOutput = getIntent().getIntArrayExtra("date");
        if (calendarOutput == null){
            Log.d(TAG, "Intent returned null dateArray");
            Calendar calendar = Calendar.getInstance();
            int[] dateToday = {calendar.get(Calendar.MONTH), calendar.get(
                    Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)};
            calendarOutput = dateToday;
        }
        Log.d(TAG, "Returning calendar size of " + calendarOutput.length);
        return calendarOutput;
    }
    //endregion

    //region Serialization/Deserialization methods
    private void serializeBudgetArray(BudgetClass[] budget, String filePath){
        String fileContent = new Gson().toJson(budget);
        try{
            FileWriter fw = new FileWriter(filePath);
            fw.write(fileContent);
            fw.flush();
        } catch(IOException e){
            Log.e("Earnings Activity", "IOException at Serialize: " + e);
        }
    }

    private BudgetClass[] deserializeBudgetArray(String filePath){
        try{
            String fileContent = readFile(filePath, StandardCharsets.US_ASCII);
            Log.i(TAG, "fileContent: " + fileContent);
            Type type = new TypeToken<BudgetClass[]>(){}.getType();
            BudgetClass[] budgetArray = new Gson().fromJson(fileContent, type);
            Log.i(TAG, "Deserialized");
            return budgetArray;
        } catch (IOException e){
            Log.e(TAG, "IOException at deserialize: " + e);
            return null;
        }
    }

    private String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    //endregion

    //region Time Interval methods
    private void timeIntervalArrowClick(boolean leftArrowClicked){
        if (leftArrowClicked){
            timeIntervalIndex -= 1;
            if (timeIntervalIndex == -1) { timeIntervalIndex = 3; }
        } else {
            timeIntervalIndex += 1;
            if (timeIntervalIndex == 4) { timeIntervalIndex = 0; }
        }
        String timeIntervalName = "time_interval" + timeIntervalIndex;
        TextView timeIntervalTextView = (TextView) findViewById(R.id.timeIntervalTextView);
        timeIntervalTextView.setText(getTimeIntervalID(timeIntervalName));
    }

    private int getTimeIntervalID(String timeIntervalName){
        return this.getResources().getIdentifier(timeIntervalName, "string", this.getPackageName());
    }
    //endregion

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Budget Activity onDestroy");
        serializeBudgetArray(budgetLiability, budgetLiabilityPath);
    }
}
