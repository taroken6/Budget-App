package com.example.budgetapp;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class EarningsActivity extends AppCompatActivity {
    private final int CALENDAR_REQUEST_CODE = 2;
    private final int EDIT_EARNINGS_REQUEST_CODE = 3;
    private final DecimalFormat DECI_FORMAT = new DecimalFormat("0.00");
    private final String TAG = "Earnings Activity"; // Debug purposes
    private String earningsFilePath;
    private final String EARNINGS_FILE_NAME = "/earningsMap";
    private String moneyType;
    private EditText moneyInput;
    private EditText moneyDescription;
    private TextView timeIntervalTextView;
    private HashMap<String, ArrayList<MoneyInput>> earningsMap = null;
    private int timeIntervalIndex = 0;
    private int[] dateArray;
    //region Variables for use when 'Edit' is selected in the popup menu
    private MoneyInput selectedInput;
    private String selectedKey;
    private MoneyInput newInput;
    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnings);

        setupMap();
        dateArray = getDateArray();

        //region Initialize Spinner
        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.earnings_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        //endregion

        //region Initialize views
        Button addButton = (Button) findViewById(R.id.addButton);
        ImageButton calendarButton = (ImageButton) findViewById(R.id.calendarImageButton);
        ImageButton timeIntervalLeftBtn = (ImageButton) findViewById(R.id.timeIntervalLeftBtn);
        ImageButton timeIntervalRightBtn = (ImageButton) findViewById(R.id.timeIntervalRightBtn);
        moneyInput = (EditText) findViewById(R.id.moneyTextBox);
        moneyDescription = (EditText) findViewById(R.id.notesTextBox);
        timeIntervalTextView = findViewById(R.id.timeIntervalTextView);
        //endregion

        setupUI(dateArray);

        //region Listeners
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EarningsActivity.this, CalendarActivity.class);
                startActivityForResult(intent, 2);
            }
        });
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moneyType = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double money = convertMoneyToDouble(moneyInput.getText().toString());
                String description = moneyDescription.getText().toString();
                TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
                addMoney(money, moneyType, description, dateTextView.getText().toString());
                Toast.makeText(EarningsActivity.this, "Money added!", Toast.LENGTH_SHORT).show();
                setupUI(dateArray);
                moneyInput.getText().clear();
                moneyDescription.getText().clear();
            }
        });
        timeIntervalLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(true);
                Log.i("Earnings Activity", "Right Button clicked!");
                setupUI(dateArray);
            }
        });
        timeIntervalRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(false);
                Log.i("Earnings Activity", "Right Button clicked!");
                setupUI(dateArray);
            }
        });
        //endregion
    }

    //region General methods
    public void setupUI(int[] selectedDate){
        displayTimeInterval();
        displayDate(selectedDate);
        displayEarnings(selectedDate);
    }

    private void setupMap(){
        earningsFilePath = getFilesDir() + EARNINGS_FILE_NAME;
        if (new File(earningsFilePath).exists()) {
            earningsMap = deserializeMap();
        } else{
            earningsMap = new HashMap<String, ArrayList<MoneyInput>>();
        }
    }

    private void displayEarnings(int[] selectedDateArray){
        TableLayout layout = findViewById(R.id.tableLayout);
        layout.removeAllViews();

        //region Display Table Header
        TableRow headerRow = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
        headerRow.setLayoutParams(rowParams);

        ImageView editImageView = new ImageView(this);
        TextView dateHeader = new TextView(this);
        TextView moneyHeader = new TextView(this);
        TextView typeHeader = new TextView(this);
        TextView descriptionHeader = new TextView(this);

        editImageView.setImageResource(R.drawable.pen);
        editImageView.setColorFilter(Color.argb(0, 0, 0, 0));
        dateHeader.setText(R.string.date);
        dateHeader.setGravity(Gravity.CENTER);
        dateHeader.setLayoutParams(rowParams);
        moneyHeader.setText(R.string.earnings_text);
        moneyHeader.setGravity(Gravity.CENTER);
        moneyHeader.setLayoutParams(rowParams);
        typeHeader.setText(R.string.type_text);
        typeHeader.setGravity(Gravity.CENTER);
        typeHeader.setLayoutParams(rowParams);
        descriptionHeader.setText(R.string.description);
        descriptionHeader.setGravity(Gravity.CENTER);
        descriptionHeader.setLayoutParams(rowParams);

        headerRow.addView(editImageView);
        headerRow.addView(dateHeader);
        headerRow.addView(moneyHeader);
        headerRow.addView(typeHeader);
        headerRow.addView(descriptionHeader);
        layout.addView(headerRow);
        //endregion

        int selectedMonth = selectedDateArray[0];
        int selectedDay = selectedDateArray[1];
        int selectedYear = selectedDateArray[2];
        Calendar calendar = new GregorianCalendar(selectedYear, selectedMonth, selectedDay);
        int[] timeIntervals = {1, 7, calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                calendar.getActualMaximum(Calendar.DAY_OF_YEAR)};

        Calendar startingDate = (Calendar) calendar.clone(); // Start with selected date
        if(timeIntervalIndex == 1){
            calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) - 1));
            startingDate = calendar;
        } else if(timeIntervalIndex == 2){
            calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_MONTH) - 1));
            startingDate = calendar;
        } else if(timeIntervalIndex == 3){
            calendar.add(Calendar.DAY_OF_YEAR, -(calendar.get(Calendar.DAY_OF_YEAR) - 1));
            startingDate = calendar;
        }

        //region Display key,value pair of each date per timeInterval
        for(int i = 0; i < timeIntervals[timeIntervalIndex]; i++){
            int startMonth = startingDate.get(Calendar.MONTH );
            int startDay = startingDate.get(Calendar.DAY_OF_MONTH);
            int startYear = startingDate.get(Calendar.YEAR);
            int[] dateToConvert = {startMonth, startDay, startYear};

            String key = getDateKey(getDateString(dateToConvert));
            if (earningsMap.containsKey(key)) {
                earningsMap.get(key).forEach(input -> displayRow(layout, input, key));
            }
            startingDate.add(Calendar.DAY_OF_MONTH, 1);
        }
        //endregion
    }

    private void displayRow(TableLayout parent, MoneyInput input, String key){
        TableRow row = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
        row.setLayoutParams(rowParams);

        TextView date = new TextView(this);
        TextView money = new TextView(this);
        TextView type = new TextView(this);
        TextView description = new TextView(this);
        ImageView editImageView = new ImageView(this);

        editImageView.setImageResource(R.drawable.pen);
        editImageView.setColorFilter(Color.argb(0, 0, 0, 0));
        editImageView.setClickable(true);
        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(EarningsActivity.this, editImageView);
                popupMenu.getMenuInflater().inflate(R.menu.edit_popup_menu,
                        popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.edit) {
                            getNewInput(input);
                            selectedKey = key;
                            selectedInput = input;
                        } else if (id == R.id.delete) {
                            deleteInput(key, input);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        date.setText(input.getDate());
        date.setLayoutParams(rowParams);
        date.setGravity(Gravity.CENTER);
        money.setText(DECI_FORMAT.format(input.getMoney()));
        money.setLayoutParams(rowParams);
        money.setGravity(Gravity.CENTER);
        type.setText(input.getType());
        type.setLayoutParams(rowParams);
        type.setGravity(Gravity.CENTER);
        description.setText(input.getDescription());
        description.setMaxEms(1);
        description.setLayoutParams(rowParams);
        description.setGravity(Gravity.CENTER);

        row.addView(editImageView);
        row.addView(date);
        row.addView(money);
        row.addView(type);
        row.addView(description);
        parent.addView(row);
    }

    private void getNewInput(MoneyInput input){
        Intent intent = new Intent(EarningsActivity.this, EditEarningsActivity.class);
        intent.putExtra("input", input);
        startActivityForResult(intent, 3);
        serialize();
    }

    private void deleteInput(String key, MoneyInput input){
        int selectedInputPosition = earningsMap.get(key).indexOf(input);
        earningsMap.get(key).remove(selectedInputPosition);
        setupUI(dateArray);
        serialize();
    }

    private void addMoney(double money, String moneyType, String moneyDescription, String date){
        String key = getDateKey(date);
        MoneyInput moneyInput = new MoneyInput(money, moneyType, moneyDescription, date);

        if(earningsMap != null){
            if (earningsMap.containsKey(key)) {
                earningsMap.get(key).add(moneyInput);
            } else {
                earningsMap.put(key, new ArrayList<MoneyInput>());
                earningsMap.get(key).add(moneyInput);
            }
            serialize();
        } else {
            earningsMap = new HashMap();
            earningsMap.put(key, new ArrayList());
            earningsMap.get(key).add(moneyInput);
            serialize();
        }

    }

    private double convertMoneyToDouble(String input){
        Log.i("Earnings Activity", "String given is \"" + input + "\"");
        String money = input.replaceAll("[^\\d.]", "");
        double returnValue;
        try{
            returnValue = Double.parseDouble(money);
        } catch(NumberFormatException e){
            returnValue = 0;
        }
        Log.i("Earnings Activity", "Value being returned is " + returnValue);
        return returnValue;
    }
    //endregion

    //region Date methods
    public int[] getDateArray(){
        int[] calendarOutput = getIntent().getIntArrayExtra("date");
        if (calendarOutput == null){
            Log.d("Earnings Activity", "Intent returned null dateArray");
            Calendar calendar = Calendar.getInstance();
            int[] dateToday = {calendar.get(Calendar.MONTH), calendar.get(
                    Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)};
            calendarOutput = dateToday;
        }
        Log.d("Earnings Activity", "Returning calendar size of " + calendarOutput.length);
        return calendarOutput;
    }

    /**
     * Get array {month, day, year} and return as String formatted as MM/dd/YYYY
     * @param dateArray
     * @return String of date in MM/dd/YYYY
     */
    private String getDateString(int[] dateArray){
        int month = dateArray[0] + 1;
        int day = dateArray[1];
        int year = dateArray[2];
        Calendar date;
        String dateString = month + "/" + day + "/" + year;
        return dateString;
    }

    private void displayDate(int[] selectedDate){
        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        String dateString = getDateString(selectedDate);
        dateTextView.setText(dateString);
    }

    private String getDateKey(String dateString){
        String[] date;
        date = dateString.split("/");
        for(int i = 0; i < date.length; i++){
            if (Integer.valueOf(date[i]) < 10) {
                date[i] = "0" + date[i];
            }
        }
        return TextUtils.join("", date);
    }
    //endregion

    //region Serialization/Deserialization methods
    private void serialize(){
        String fileContent = new Gson().toJson(earningsMap);
        try{
            FileWriter fw = new FileWriter(earningsFilePath);
            fw.write(fileContent);
            fw.flush();
        } catch(IOException e){
            Log.e("Earnings Activity", "IOException at serialize: " + e);
        }
    }

    private HashMap<String, ArrayList<MoneyInput>> deserializeMap(){
        try{
            String fileContent = readFile(earningsFilePath, StandardCharsets.US_ASCII);
            Type type = new TypeToken<HashMap<String, ArrayList<MoneyInput>>>(){}.getType();
            HashMap<String, ArrayList<MoneyInput>> map = new Gson().fromJson(fileContent, type);
            return map;
        } catch (IOException e){
            e.printStackTrace();
            Log.e("Earnings Activity", "IOException at deserializeMap()");
            return null;
        }
    }

    private String readFile(String path, Charset encoding) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    //endregion

    //region Time Interval methods

    /**
     * Displays Time Interval onto the Text View
     */
    private void displayTimeInterval(){
        String timeIntervalName = "time_interval" + timeIntervalIndex;
        int timeIntervalID = getTimeIntervalID(timeIntervalName);
        timeIntervalTextView.setText(getString(timeIntervalID));
    }

    /**
     * Retrieves String Resource ID of the given Time Interval from displayTimeInterval method
     * @param timeIntervalName String
     * @return Resource ID of Time Interval
     */
    private int getTimeIntervalID(String timeIntervalName){
        return this.getResources().getIdentifier(timeIntervalName,
                "string", this.getPackageName());
    }

    /**
     * Changes displayed time interval depending whether the left or right arrow is clicked
     * @param leftArrowClicked Whether the left or right arrow is clicked
     */
    private void timeIntervalArrowClick(boolean leftArrowClicked){
        if (leftArrowClicked){
            timeIntervalIndex -= 1;
            if (timeIntervalIndex == -1) { timeIntervalIndex = 3; }
        } else {
            timeIntervalIndex += 1;
            if (timeIntervalIndex == 4) { timeIntervalIndex = 0; }
        }
        String timeIntervalName = "time_interval" + timeIntervalIndex;
        timeIntervalTextView.setText(getTimeIntervalID(timeIntervalName));
    }
    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == CALENDAR_REQUEST_CODE) { // Retrieve from Calendar Activity
            if(resultCode == Activity.RESULT_OK){
                dateArray = intent.getIntArrayExtra("date");
                setupUI(dateArray);
            }
        }
        if (requestCode == EDIT_EARNINGS_REQUEST_CODE){ // Code for edit earnings Activity
            if(resultCode == Activity.RESULT_OK){
                newInput = (MoneyInput) intent.getParcelableExtra("edit_earnings");
                if(selectedInput.getDate().equals(newInput.getDate())){ // Date left the same
                    int selectedInputPosition = earningsMap.get(selectedKey).indexOf(selectedInput);
                    earningsMap.get(selectedKey).set(selectedInputPosition, newInput);
                } else {  // Remove old input and add to the different date array
                    String newKey = getDateKey(newInput.getDate());
                    if (earningsMap.containsKey(newKey)) { // If spendings exist at selected date, add
                        earningsMap.get(newKey).add(newInput);
                    }
                    else { // Else add a new key/value pair and add spending
                        earningsMap.put(newKey, new ArrayList<MoneyInput>());
                        earningsMap.get(newKey).add(newInput);
                    }
                    int selectedInputPosition = earningsMap.get(selectedKey).indexOf(selectedInput);
                    earningsMap.get(selectedKey).remove(selectedInputPosition);
                }
                setupUI(dateArray);
            }
        }
    }
}
