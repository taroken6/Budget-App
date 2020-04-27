package com.example.budgetappattempt2;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class SpendingsActivity extends AppCompatActivity {
    private final String TAG = "Spendings Activity";
    private int timeIntervalIndex = 0;
    private TextView timeIntervalTextView;
    private String mapFilePath = null;
    private final String mapFileName = "/SpendingsMap";
    private int[] selectedDateArray;
    private HashMap<String, ArrayList<SpendingsClass>> map = null;
    private SpendingsClass selectedInput;
    private String selectedKey;
    private SpendingsClass newInput;
    private final DecimalFormat DECI_FORMAT = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spendings);

        declareFilePaths();
        map = deserializeMap();
        selectedDateArray = getDateArray();

        Spinner typeSpinner = getTypeSpinner();
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        ImageButton calendarButton = (ImageButton) findViewById(R.id.calendarImageButton);
        ImageButton timeIntervalLeftBtn = (ImageButton) findViewById(R.id.timeIntervalLeftBtn);
        ImageButton timeIntervalRightBtn = (ImageButton) findViewById(R.id.timeIntervalRightBtn);
        Button addButton = (Button) findViewById(R.id.addButton);
        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        timeIntervalTextView = (TextView) findViewById(R.id.timeIntervalTextView);
        EditText notesEditText = (EditText) findViewById(R.id.notesTextBox);
        EditText moneyEditText = (EditText) findViewById(R.id.moneyTextBox);

        setupUI(selectedDateArray);

        //region Listeners
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = dateTextView.getText().toString();
                String type = typeSpinner.getSelectedItem().toString();
                String category = categorySpinner.getSelectedItem().toString();
                String notes = notesEditText.getText().toString();
                double money = convertMoneyToDouble(moneyEditText.getText().toString());
                SpendingsClass spending = new SpendingsClass(money, type, notes, date, category);

                addSpendingsToMap(spending);
                setupUI(selectedDateArray);
                notesEditText.getText().clear();
                moneyEditText.getText().clear();

                Toast.makeText(SpendingsActivity.this,
                        "Added Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                getCategorySpinner(selectedType);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        timeIntervalLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(true);
                setupUI(selectedDateArray);
            }
        });
        timeIntervalRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(false);
                setupUI(selectedDateArray);
            }
        });
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpendingsActivity.this, CalendarActivity.class);
                startActivityForResult(intent, 2); //
            }
        });
        //endregion
    }

    private void setupUI(int[] selectedDate){
        displayTimeInterval();
        displayDate(selectedDate);
        displaySpendings(selectedDate);
    }


    //region Display table

    /**
     * Displays spendings onto table depending on date and time interval
     * @param selectedDateArray user-selected date
     */
    private void displaySpendings(int[] selectedDateArray){
        TableLayout table = findViewById(R.id.tableLayout);
        table.removeAllViews();
        displayTableHeader(table);

        int selectedMonth = selectedDateArray[0];
        int selectedDay = selectedDateArray[1];
        int selectedYear = selectedDateArray[2];
        Calendar calendar = new GregorianCalendar(selectedYear, selectedMonth, selectedDay);
        int[] timeIntervals = {1, 7, calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                calendar.getActualMaximum(Calendar.DAY_OF_YEAR)};
        Log.d(TAG, "Date received = " + calendar.getTime());

        Calendar startingDate = (Calendar) calendar.clone(); // Start with selected date
        if(timeIntervalIndex == 1){
            calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) - 1));
            startingDate = calendar;
            Log.d("Earnings Activity", "Start of week = " + startingDate.getTime());
        } else if(timeIntervalIndex == 2){
            calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_MONTH) - 1));
            startingDate = calendar;
            Log.d("Earnings Activity", "Start of month = " + startingDate.getTime());
        } else if(timeIntervalIndex == 3) {
            calendar.add(Calendar.DAY_OF_YEAR, -(calendar.get(Calendar.DAY_OF_YEAR) - 1));
            startingDate = calendar;
            Log.d("Earnings Activity", "Start of year = " + startingDate.getTime());
        }

        for(int i = 0; i < timeIntervals[timeIntervalIndex]; i++){
            int startMonth = startingDate.get(Calendar.MONTH );
            int startDay = startingDate.get(Calendar.DAY_OF_MONTH);
            int startYear = startingDate.get(Calendar.YEAR);
            int[] dateToConvert = {startMonth, startDay, startYear};

            String key = getDateKey(getDateToString(dateToConvert));
            if (map.containsKey(key)) {
                map.get(key).forEach(spending -> displayTableRow(table, spending, key));
            } else {
                Log.d("Earnings Activity", "Map doesn't contain key at date " + key);
            }
            startingDate.add(Calendar.DAY_OF_MONTH, 1);
        }

    }

    private void displayTableRow(TableLayout parent, SpendingsClass spending, String key){
        TableRow row = new TableRow(this);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
        row.setLayoutParams(rowParams);

        ImageView editImageView = new ImageView(this);
        TextView date = new TextView(this);
        TextView amt = new TextView(this);
        TextView type = new TextView(this);
        TextView category = new TextView(this);
        TextView notes = new TextView(this);

        editImageView.setImageResource(R.drawable.pen);
        editImageView.setColorFilter(Color.argb(0, 0, 0, 0));
        editImageView.setClickable(true);
        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(SpendingsActivity.this, editImageView);
                popupMenu.getMenuInflater().inflate(R.menu.edit_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String value = item.getTitle().toString();
                        int id = item.getItemId();
                        if (id == R.id.edit) {
                            getNewInput(spending);
                            selectedKey = key;
                            selectedInput = spending;
                            Log.d(TAG, "Selected key: " + selectedKey);
                        } else if (id == R.id.delete) {
                            deleteInput(key, spending);
                        }
                        Toast.makeText(SpendingsActivity.this, value, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        date.setText(spending.getDate());
        date.setLayoutParams(rowParams);
        date.setGravity(Gravity.CENTER);
        amt.setText(DECI_FORMAT.format(spending.getMoney()));
        amt.setLayoutParams(rowParams);
        amt.setGravity(Gravity.CENTER);
        type.setText(spending.getType());
        type.setLayoutParams(rowParams);
        type.setGravity(Gravity.CENTER);
        category.setText(spending.getCategory());
        category.setLayoutParams(rowParams);
        category.setGravity(Gravity.CENTER);
        notes.setText(spending.getDescription());
        notes.setMaxEms(1);
        notes.setLayoutParams(rowParams);
        notes.setGravity(Gravity.CENTER);

        row.addView(editImageView);
        row.addView(date);
        row.addView(amt);
        row.addView(type);
        row.addView(category);
        row.addView(notes);
        parent.addView(row);
    }

    private void getNewInput(SpendingsClass spending){
        Intent intent = new Intent(SpendingsActivity.this, EditSpendingsActivity.class);
        intent.putExtra("spendings", spending);
        startActivityForResult(intent, 4);
        serializeMap();
    }

    private void deleteInput(String key, SpendingsClass spending){
        int selectedInputPosition = map.get(key).indexOf(spending);
        map.get(key).remove(selectedInputPosition);
        setupUI(selectedDateArray);
        serializeMap();
    }

    private void displayTableHeader(TableLayout table){
        TableRow header = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1);
        header.setLayoutParams(params);

        ImageView editImageView = new ImageView(this);
        TextView date = new TextView(this);
        TextView amt = new TextView(this);
        TextView type = new TextView(this);
        TextView category = new TextView(this);
        TextView notes = new TextView(this);

        editImageView.setImageResource(R.drawable.pen);
        editImageView.setColorFilter(Color.argb(0, 0, 0, 0));
        date.setText(R.string.date);
        date.setGravity(Gravity.CENTER);
        date.setLayoutParams(params);
        amt.setText(R.string.amt);
        amt.setGravity(Gravity.CENTER);
        amt.setLayoutParams(params);
        type.setText(R.string.type_text);
        type.setGravity(Gravity.CENTER);
        type.setLayoutParams(params);
        category.setText(R.string.category);
        category.setGravity(Gravity.CENTER);
        category.setLayoutParams(params);
        notes.setText(R.string.notes);
        notes.setGravity(Gravity.CENTER);
        notes.setLayoutParams(params);

        header.addView(editImageView);
        header.addView(date);
        header.addView(amt);
        header.addView(type);
        header.addView(category);
        header.addView(notes);
        table.addView(header);
    }
    //endregion

    //region Serialization/Deserialization

    /**
     * Serializes the map to the file location
     */
    private void serializeMap(){
        String fileContent = new Gson().toJson(map);
        try{
            FileWriter fw = new FileWriter(mapFilePath);
            fw.write(fileContent);
            fw.flush();
            Log.d(TAG, "Successfully serialized file!");
        } catch(IOException e){
            Log.e(TAG, "IOException at serializeMap: " + e);
        }
    }

    /**
     * Deserializes the file set on the global variable 'mapFilePath'
     * @return The map this class will be using
     */
    private HashMap<String, ArrayList<SpendingsClass>> deserializeMap(){
        if(new File(mapFilePath).exists()){
            try{
                String fileContent = readFile(mapFilePath, StandardCharsets.US_ASCII);
                Type type = new TypeToken<HashMap<String, ArrayList<SpendingsClass>>>(){}.getType();
                HashMap<String, ArrayList<SpendingsClass>> mapOut =
                        new Gson().fromJson(fileContent, type);
                Log.d(TAG, "Returning existing map... " + mapOut.toString());
                return mapOut;
            } catch(IOException e){
                Log.e(TAG, "IOException at deserialize " + e);
                return new HashMap<String, ArrayList<SpendingsClass>>();
            }
        } else {
            Log.d(TAG, "Returning empty map");
            return new HashMap<String, ArrayList<SpendingsClass>>();
        }
    }

    /**
     * Reads serialized file and returns as a String
     * @param path File path
     * @param encoding In this case, US Characters
     * @return String of the entire file
     * @throws IOException
     */
    private String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private void declareFilePaths(){
        mapFilePath = getFilesDir() + mapFileName;
    }
    //endregion

    //region General methods

    /**
     * Converts the String of EditText to Double
     * @param input Amount in $#.##
     * @return Double of String input
     */
    private double convertMoneyToDouble(String input){
        Log.i(TAG, "String given is \"" + input + "\"");
        String money = input.replaceAll("[^\\d.]", "");
        double returnValue;
        try{
            returnValue = Double.parseDouble(money);
        } catch(NumberFormatException e){
            returnValue = 0;
        }
        Log.i(TAG, "Value being returned is " + returnValue);
        return returnValue;
    }

    private void addSpendingsToMap(SpendingsClass spending){
        String key = getDateKey(spending.getDate());

        if(map.containsKey(key)){
            map.get(key).add(spending);
        } else {
            map.put(key, new ArrayList<SpendingsClass>());
            map.get(key).add(spending);
        }
        serializeMap();
    }
    //endregion

    //region Spinner methods
    /**
     * General setup for 'type' spinner object
     * @return 'Spending Types' Spinner
     */
    private Spinner getTypeSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spending_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.spendingsTypeSpinner);
        spinner.setAdapter(adapter);
        return spinner;
    }

    /**
     * Displays the 'Category' spinner associated with the 'Spending Type' the user selected
     * @param selectedType The item selected from 'Type' spinner
     */
    private void getCategorySpinner(String selectedType){
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        String[] typesStringArray = getResources().getStringArray(R.array.spending_type);
        String liabilityStringResource = typesStringArray[0];
        String personalStringResource = typesStringArray[1];
        String savingsStringResource = typesStringArray[2];

        if(selectedType.equals(liabilityStringResource)){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.spending_liability_type, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            Log.d(TAG, "Selected type = " + liabilityStringResource);
        } else if(selectedType.equals(personalStringResource)){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.spending_personal_type, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            Log.d(TAG, "Selected type = " + personalStringResource);
        } else if (selectedType.equals(savingsStringResource)) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.spending_savings_type, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
            Log.d(TAG, "Selected type = " + savingsStringResource);
        } else {
            Log.d(TAG, "Error at 'getCategorySpinner");
        }
    }
    //endregion

    //region Date related methods

    /**
     * Displays date to TextView
     * @param selectedDate User-selected date
     */
    private void displayDate(int[] selectedDate){
        TextView dateTextView = (TextView) findViewById(R.id.dateTextView);
        String dateString = getDateToString(selectedDate);
        dateTextView.setText(dateString);
    }

    /**
     * Get array {month, day, year} and return as String formatted as MM/dd/YYYY
     * @param dateArray
     * @return String of date in MM/dd/YYYY
     */
    private String getDateToString(int[] dateArray){
        int month = dateArray[0] + 1;
        int day = dateArray[1];
        int year = dateArray[2];
        String dateString = month + "/" + day + "/" + year;
        return dateString;
    }

    /**
     * Converts String of the selected date to a key for the Map
     * @param dateString User selected date as String
     * @return Key for map use
     */
    private String getDateKey(String dateString){
        Log.i(TAG, "getDateKey received date \"" + dateString + "\"");
        String[] date;
        date = dateString.split("/");
        for(int i = 0; i < date.length; i++){
            if (Integer.valueOf(date[i]) < 10) {
                date[i] = "0" + date[i];
            }
        }
        Log.i(TAG, "Returning date key \'" + TextUtils.join("", date) + "\'");
        return TextUtils.join("", date);
    }

    /**
     * Returns user-selected date or system's current date if none selected
     * @return Array of date as {Month, day, year}
     */
    public int[] getDateArray(){
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

    //region Time Interval Methods

    /**
     * Displays [Day, Week, Month, or Year] of date onto TextView
     */
    private void displayTimeInterval(){
        String timeIntervalName = "time_interval" + timeIntervalIndex;
        int timeIntervalID = getTimeIntervalID(timeIntervalName);
        System.out.println("String resource: " + getString(timeIntervalID));
        timeIntervalTextView = findViewById(R.id.timeIntervalTextView);
        timeIntervalTextView.setText(getString(timeIntervalID));
    }

    /**
     * Returns resource ID for Time Interval String from XML
     * @param timeIntervalName Name of string
     * @return ID of resource
     */
    private int getTimeIntervalID(String timeIntervalName){
        return this.getResources().getIdentifier(timeIntervalName, "string",
                this.getPackageName());
    }

    /**
     * Changes Time Interval TextView on arrow click
     * @param leftArrowClicked Whether the left arrow was clicked
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent){ //Mainly for date
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                selectedDateArray = intent.getIntArrayExtra("date");
                setupUI(selectedDateArray);
            }
        }
        if (requestCode == 4){ // Code for edit spendings Activity
            if(resultCode == Activity.RESULT_OK){
                newInput = (SpendingsClass) intent.getParcelableExtra("edit_spendings");
                Log.d(TAG, "on result 4: " + selectedKey +  " " + selectedInput.getDate());
                Log.d(TAG, "Selected at position: " + map.get(selectedKey).indexOf(selectedInput));
                if(selectedInput.getDate().equals(newInput.getDate())){ // Date left the same
                    int selectedInputPosition = map.get(selectedKey).indexOf(selectedInput);
                    map.get(selectedKey).set(selectedInputPosition, newInput);
                } else {  // Remove old input and add to the different date array
                    String newKey = getDateKey(newInput.getDate());
                    if (map.containsKey(newKey)) { // If spendings exist at selected date, add
                        map.get(newKey).add(newInput);
                    }
                    else { // Else add a new key/value pair and add spending
                        map.put(newKey, new ArrayList<SpendingsClass>());
                        map.get(newKey).add(newInput);
                    }
                    int selectedInputPosition = map.get(selectedKey).indexOf(selectedInput);
                    map.get(selectedKey).remove(selectedInputPosition);
                }
                setupUI(selectedDateArray);
            }
        }
    }
}
