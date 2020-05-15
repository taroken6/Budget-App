package com.example.budgetapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    private TextView timeIntervalTextView;
    private File configFile;
    private ConfigClass config;
    private Properties prop;
    private Toast toast;
    private final String TAG = "Main Activity";
    private final DecimalFormat DECI_FORMAT = new DecimalFormat("0.00");
    private int[] dateArray;
    private ProgressBar progressBar;
    private TextView progressBarText;
    private final int CALENDAR_REQUEST_CODE = 1;

    private String earningsFilePath;
    private final String EARNINGS_FILE_NAME = "/earningsMap";
    private TextView earningsTotalTextView;
    private double[] earningsIncome = {0};
    private double[] earningsOther = {0};
    private ExpandableListView earningsListView;
    private List<String> earningsListParent;
    private Map<String, List<EarningsListObject>> earningsListMap;
    private ExpandableListAdapter earningsListViewAdapter;

    private TextView budgetTotalTextView;
    private double budgetLiabilityTotal;
    private double budgetPersonalTotal;
    private double budgetSavingsTotal;
    private double budgetTotal = 0;
    private ExpandableListView budgetListView;
    private List<String> budgetListParent;
    private Map<String, List<BudgetListObject>> budgetListMap;
    private ExpandableListAdapter budgetListViewAdapter;

    private TextView spendingsTotalTextView;
    private double spendingsLiabilityTotal;
    private double spendingsPersonalTotal;
    private double spendingsSavingsTotal;
    private double spendingsTotal = 0;
    private ExpandableListView spendingsListView;
    private List<String> spendingsListParent;
    private Map<String, List<SpendingsListObject>> spendingsListMap;
    private ExpandableListAdapter spendingsListViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Setup Config
        //==========================================================================================
        config = new ConfigClass();
        prop = getConfig();
        if (prop.isEmpty()) {
            Log.e(TAG, "Properties file not found!!!");
        } else {
            config.setTimeInterval(Integer.parseInt(prop.getProperty("timeInterval")));
            config.setUser(prop.getProperty("user"));
            config.printAll();
        }
        //==========================================================================================
        //endregion

        //region General setup
        earningsFilePath = getFilesDir() + EARNINGS_FILE_NAME;
        dateArray = getDate();
        earningsTotalTextView = findViewById(R.id.earningsTotalText);
        budgetTotalTextView = (TextView) findViewById(R.id.budgetTotalText);
        spendingsTotalTextView = (TextView) findViewById(R.id.spendingsTotalText);
        progressBarText = (TextView) findViewById(R.id.progressBarTextView);
        ImageButton timeIntervalLeftBtn = findViewById(R.id.timeIntervalLeftBtn);
        ImageButton timeIntervalRightBtn = findViewById(R.id.timeIntervalRightBtn);
        TextView dateTextView = findViewById(R.id.dateTextView);
        progressBar = findViewById(R.id.progressBar);

        setupUI(dateArray);
        //endregion

        // region Button Click
        timeIntervalLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(true);
                setupUI(dateArray);
            }
        });
        timeIntervalRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeIntervalArrowClick(false);
                setupUI(dateArray);
            }
        });
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        //endregion
    }

    //region General-use methods
    /**
     * Sets up TextViews
     *
     * @param dateIntent User-selected date
     */
    private void setupUI(int[] dateIntent) {
        displayTimeInterval();
        displayDate(dateIntent);

        String earningsTotal = getTotalEarnings(dateIntent, config.getTimeInterval());  //String variable for displaying earnings and budget
        setupEarningsListView();
        displayEarnings(earningsTotal);

        getBudgetTotals(earningsTotal);
        setupBudgetListView();
        displayBudgetTotal();

        getTotalSpendings(dateIntent, config.getTimeInterval());
        setupSpendingsListView();
        displaySpendingsTotal();

        updateProgressBar();
    }

    private void updateProgressBar(){
        double percentOfBudgetSpent = spendingsTotal / budgetTotal * 100;
        if(Double.isNaN(percentOfBudgetSpent) || Double.isInfinite(percentOfBudgetSpent))
            percentOfBudgetSpent = 0;
        progressBarText.setText(DECI_FORMAT.format(percentOfBudgetSpent) + "%");
        int value = (int) percentOfBudgetSpent;
        progressBar.setProgress(value);

    }

    /**
     * Returns String of a json file
     *
     * @param path     File path
     * @param encoding Character set
     * @return String of JOSN file
     * @throws IOException
     */
    private String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    //endregion

    //region Date-related methods
    private void displayDate(int[] selectedDate) {
        TextView dateTextView = findViewById(R.id.dateTextView);
        String dateString = getDateTextViewString(selectedDate);
        dateTextView.setText(dateString);
    }

    /**
     * @return user-selected date or system's current date if none selected
     */
    private int[] getDate() {
        int[] calendarOutput = getIntent().getIntArrayExtra("date");
        if (calendarOutput == null) {
            Log.d(TAG, "Intent returned null dateArray");
            Calendar calendar = Calendar.getInstance();
            int[] dateToday = {calendar.get(Calendar.MONTH), calendar.get(
                    Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)};
            calendarOutput = dateToday;
        }
        Log.d(TAG, "Returning calendar size of " + calendarOutput.length);
        return calendarOutput;
    }

    /**
     * Returns date {Month, day, year} to String for use in Back-end code
     *
     * @param selectedDateArray user-selected date
     * @return user-selected date as String
     */
    private String getDateToString(int[] selectedDateArray) {
        int month = selectedDateArray[0] + 1;
        int day = selectedDateArray[1];
        int year = selectedDateArray[2];
        Calendar date;
        String dateString = month + "/" + day + "/" + year;
        return dateString;
    }

    /**
     * Returns the {day, week, month, or year} of selected date for display on TextView
     *
     * @param selectedDateArray user-selected date
     * @return String of selected date depending on time interval
     */
    private String getDateTextViewString(int[] selectedDateArray) {
        int timeInterval = config.getTimeInterval();
        int month = selectedDateArray[0] + 1;
        int day = selectedDateArray[1];
        int year = selectedDateArray[2];
        Calendar date;
        String dateText = null;

        if (timeInterval == 0) {
            dateText = month + "/" + day + "/" + year;
        } else {
            date = new GregorianCalendar(year, month - 1, day);
            if (timeInterval == 1) {
                int dayOfWeek = (date.get(Calendar.DAY_OF_WEEK) - 1);
                date.add(Calendar.DAY_OF_MONTH, -dayOfWeek); // Set date to first day of week (SUN)
                String sundayText = (date.get(Calendar.MONTH) + 1) + "/" +
                        date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR);
                date.add(Calendar.DAY_OF_MONTH, 6); // Set date to last day of week (SAT)
                String saturdayText = (date.get(Calendar.MONTH) + 1) + "/" +
                        date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR);
                dateText = sundayText + " - " + saturdayText;
            } else if (timeInterval == 2) {
                dateText = date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            } else if (timeInterval == 3) {
                dateText = String.valueOf(date.get(Calendar.YEAR));
            }
        }
        return dateText;
    }

    /**
     * Converts String of user-selected date as a String key for map use
     *
     * @param dateString String of user-selected date
     * @return Key for map
     */
    private String getDateKey(String dateString) {
        String[] date;
        date = dateString.split("/");
        for (int i = 0; i < date.length; i++) {
            if (Integer.valueOf(date[i]) < 10) {
                date[i] = "0" + date[i];
            }
        }
        return TextUtils.join("", date);
    }
    //endregion

    //region Time Interval methods

    /**
     * Displays {Day, Week, Month, or Year} depending on current TimeInterval in config
     */
    private void displayTimeInterval() {
        String timeIntervalName = "time_interval" + config.getTimeInterval();
        int timeIntervalID = getTimeIntervalID(timeIntervalName);
        timeIntervalTextView = findViewById(R.id.timeIntervalTextView);
        timeIntervalTextView.setText(getString(timeIntervalID));
    }

    /**
     * Changes time interval if arrows are clicked
     *
     * @param leftArrowClicked Whether left or right was pressed
     */
    private void timeIntervalArrowClick(boolean leftArrowClicked) {
        int timeIntervalIndex;
        if (leftArrowClicked) {
            timeIntervalIndex = config.getTimeInterval() - 1;
            if (timeIntervalIndex == -1) {
                timeIntervalIndex = 3;
            }
        } else {
            timeIntervalIndex = config.getTimeInterval() + 1;
            if (timeIntervalIndex == 4) {
                timeIntervalIndex = 0;
            }
        }
        String timeIntervalName = "time_interval" + timeIntervalIndex;
        timeIntervalTextView.setText(getTimeIntervalID(timeIntervalName));
        prop.setProperty("timeInterval", Integer.toString(timeIntervalIndex));
        config.setTimeInterval(timeIntervalIndex);
    }

    /**
     * Get Resource ID of Time Interval selected
     *
     * @param timeIntervalName Name of String Resource
     * @return ID of String Resource
     */
    private int getTimeIntervalID(String timeIntervalName) {
        return this.getResources().getIdentifier(timeIntervalName, "string", this.getPackageName());
    }
    //endregion

    //region Earnings methods
    private void setupEarningsListView() {
        earningsListView = findViewById(R.id.earningsListView);
        List<String> types = Arrays.asList(getResources().getStringArray(R.array.earnings_type));
        List<EarningsListObject> children = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            children.add(new EarningsListObject(type, getEarningsAmountByType(type)));
        }
        earningsListParent = new ArrayList<>();
        earningsListParent.add("Earnings");
        earningsListMap = new HashMap<>();
        earningsListMap.put(earningsListParent.get(0), children);

        earningsListViewAdapter = new EarningsListAdapter(this, earningsListParent,
                earningsListMap);
        earningsListView.setAdapter(earningsListViewAdapter);
    }

    private double[] getEarningsAmountByType(String givenType) {
        if (givenType.equals("Income")) {
            return earningsIncome;
        } else if (givenType.equals("Other")) {
            return earningsOther;
        } else {
            return null;
        }
    }

    private void displayEarnings(String earningsTotal) {
        earningsTotalTextView.setText("$" + earningsTotal);
    }

    /**
     * Brings user to Earnings Activity when they click on Earnings TextView
     *
     * @param view
     */
    public void startEarningsActivity(View view) {
        Intent intent = new Intent(MainActivity.this, EarningsActivity.class);
        startActivity(intent);
        setupUI(getDate());
    }

    /**
     * Returns an array of earnings depending on Time Interval and selected date
     *
     * @param selectedDateArray user-selected date
     * @return Earnings per date and time interval
     */
    private double[] earningsByIntervals(int[] selectedDateArray) {
        double[] output = new double[4];
        for (int i = 0; i < output.length; i++) {
            output[i] = Double.parseDouble(getTotalEarnings(selectedDateArray, i));
            Log.d(TAG, "Will return " + output[i]);
        }
        return output;
    }

    /**
     * Calculates sum of earnings per date and time interval
     *
     * @param selectedDateArray user-selected date
     * @param timeInterval      current time interval
     * @return Sum of earnings formatted into a String
     */
    private String getTotalEarnings(int[] selectedDateArray, int timeInterval) {
        if (new File(earningsFilePath).exists()) {
            int selectedMonth = selectedDateArray[0];
            int selectedDay = selectedDateArray[1];
            int selectedYear = selectedDateArray[2];
            Calendar calendar = new GregorianCalendar(selectedYear, selectedMonth, selectedDay);
            int[] timeIntervals = {1, 7, calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                    calendar.getActualMaximum(Calendar.DAY_OF_YEAR)};

            Calendar startingDate = (Calendar) calendar.clone(); // Start with selected date
            Log.d(TAG, "Date copied = " + startingDate.getTime());
            if (timeInterval == 1) {
                calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) - 1));
                startingDate = calendar;
            } else if (timeInterval == 2) {
                calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_MONTH) - 1));
                startingDate = calendar;
            } else if (timeInterval == 3) {
                calendar.add(Calendar.DAY_OF_YEAR, -(calendar.get(Calendar.DAY_OF_YEAR) - 1));
                startingDate = calendar;
            }

            HashMap<String, ArrayList<MoneyInput>> earningsMap = deserializeEarningsMap(earningsFilePath);
            double[] total = {0};
            earningsIncome[0] = 0;
            earningsOther[0] = 0;

            for (int i = 0; i < timeIntervals[timeInterval]; i++) {
                int startMonth = startingDate.get(Calendar.MONTH);
                int startDay = startingDate.get(Calendar.DAY_OF_MONTH);
                int startYear = startingDate.get(Calendar.YEAR);
                int[] dateToConvert = {startMonth, startDay, startYear};
                String key = getDateKey(getDateToString(dateToConvert));
                if (earningsMap.containsKey(key)) {
                    earningsMap.get(key).forEach(input -> {
                        total[0] += input.getMoney();
                        if (input.getType().equals("Income")) {
                            earningsIncome[0] += input.getMoney();
                        } else if (input.getType().equals("Other")) {
                            earningsOther[0] += input.getMoney();
                        }
                    });
                }
                startingDate.add(Calendar.DAY_OF_MONTH, 1);
            }
            return DECI_FORMAT.format(total[0]);
        } else {
            return "0.00";
        }
    }

    private HashMap<String, ArrayList<MoneyInput>> deserializeEarningsMap(String filePath) {
        try {
            String fileContent = readFile(filePath, StandardCharsets.US_ASCII);
            if(fileContent == null){
                Log.i(TAG, fileContent);
            }
            Type type = new TypeToken<HashMap<String, ArrayList<MoneyInput>>>() {
            }.getType();
            HashMap<String, ArrayList<MoneyInput>> map = new Gson().fromJson(fileContent, type);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException at deserializeEarningsMap()");
            return null;
        }
    }
    //endregion

    //region Budget methods
    private void displayBudgetTotal(){
        budgetTotalTextView.setText("$" + DECI_FORMAT.format(budgetTotal));
    }

    public void startBudgetActivity(View view) {
        Intent intent = new Intent(MainActivity.this, BudgetActivity.class);
        intent.putExtra("date", getDateToString(dateArray));
        intent.putExtra("earnings", earningsByIntervals(dateArray));
        startActivity(intent);
    }

    private void getBudgetTotals(String earningsTotal) {
        BudgetClass[] budgetLiability = deserializeBudget(getFilesDir() + "/BudgetLiability");
        BudgetClass[] budgetPersonal = deserializeBudget(getFilesDir() + "/BudgetPersonal");
        BudgetClass[] budgetSavings = deserializeBudget(getFilesDir() + "/BudgetSavings");
        budgetLiabilityTotal = calculateBudgetTotal(budgetLiability, earningsTotal);
        budgetPersonalTotal = calculateBudgetTotal(budgetPersonal, earningsTotal);
        budgetSavingsTotal = calculateBudgetTotal(budgetSavings, earningsTotal);
        budgetTotal = budgetLiabilityTotal + budgetPersonalTotal + budgetSavingsTotal;
    }

    /**
     * Get calculated total of user-inputted budget for display on TextView
     *
     * @param budget              Deserialized Budget File
     * @param earningsTotalString Total of Earnings to calculate budget
     * @return String of budget for display on TextView
     */
    private double calculateBudgetTotal(BudgetClass[] budget, String earningsTotalString) {
        if (budget == null) { //First check if object is null, Need to access Budget Class first
            return 0;
        }

        double earningsTotal = Double.valueOf(earningsTotalString);
        double budgetInput = budget[config.getTimeInterval()].getInput();
        String budgetType = budget[config.getTimeInterval()].getType();
        double output = 0.00;
        if (budgetType.equals("%")) {
            output = earningsTotal * budgetInput / 100;
        } else if (budgetType.equals("Total")) {
            output = budgetInput;
        } else {
            output = -1;
        }
        return output;
    }

    /**
     * Deserialize Budget File at given location
     *
     * @param filePath File Location of serialized file
     * @return
     */
    private BudgetClass[] deserializeBudget(String filePath) {
        try {
            String fileContent = readFile(filePath, StandardCharsets.US_ASCII);
            Log.i(TAG, "fileContent: " + fileContent);
            Type type = new TypeToken<BudgetClass[]>() {
            }.getType();
            BudgetClass[] budgetArray = new Gson().fromJson(fileContent, type);
            Log.i(TAG, "Deserialized budget");
            return budgetArray;
        } catch (IOException e) {
            Log.e(TAG, "IOException at deserialize budget: " + e);
            return null;
        }
    }

    private double getBudgetAmountByType(String givenType){
        if(givenType.equals("Liability")){
            return budgetLiabilityTotal;
        } else if (givenType.equals("Personal")) {
            return budgetPersonalTotal;
        } else if(givenType.equals("Savings")){
            return budgetSavingsTotal;
        } else{
            Log.e(TAG, "Error at getBudgetAmountByType method");
            return -1;
        }
    }

    private void setupBudgetListView() {
        budgetListView = findViewById(R.id.budgetListView);
        List<String> types = Arrays.asList(getResources().getStringArray(R.array.spending_type));
        List<BudgetListObject> children = new ArrayList<>();
        for(int i = 0; i < types.size(); i++){
            String type = types.get(i);
            children.add(new BudgetListObject(type, getBudgetAmountByType(type)));
        }
        budgetListParent = new ArrayList<>();
        budgetListParent.add("Budget");
        budgetListMap = new HashMap<>();
        budgetListMap.put(budgetListParent.get(0), children);

        budgetListViewAdapter = new BudgetListAdapter(this, budgetListParent, budgetListMap);
        budgetListView.setAdapter(budgetListViewAdapter);
    }
    //endregion

    //region Spendings methods
    private void displaySpendingsTotal(){
        spendingsTotalTextView.setText("$" + DECI_FORMAT.format(spendingsTotal));
        if(spendingsTotal > budgetTotal){
            spendingsTotalTextView.setTextColor(Color.RED);
        } else{
            spendingsTotalTextView.setTextColor(Color.WHITE);
        }
    }

    public void startSpendingsActivity(View view) {
        Intent intent = new Intent(MainActivity.this, SpendingsActivity.class);
        startActivity(intent);
        setupUI(getDate());
    }

    private void setupSpendingsListView() {
        spendingsListView = findViewById(R.id.spendingsListView);
        List<String> types = Arrays.asList(getResources().getStringArray(R.array.spending_type));
        List<SpendingsListObject> children = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            children.add(new SpendingsListObject(type, getSpendingsAmountByType(type)));
        }
        spendingsListParent = new ArrayList<>();
        spendingsListParent.add("Spendings");
        spendingsListMap = new HashMap<>();
        spendingsListMap.put(spendingsListParent.get(0), children);

        spendingsListViewAdapter = new SpendingsListAdapter(this, spendingsListParent,
                spendingsListMap);
        spendingsListView.setAdapter(spendingsListViewAdapter);
    }

    private double getSpendingsAmountByType(String givenType) {
        if (givenType.equals("Liability")) {
            return spendingsLiabilityTotal;
        } else if (givenType.equals("Personal")) {
            return spendingsPersonalTotal;
        } else if (givenType.equals("Savings")) {
            return spendingsSavingsTotal;
        } else {
            return 0;
        }
    }

    /**
     * Get sums of spendings per a spending type. For display on TextView.
     *
     * @param selectedDateArray User-selected date
     * @param timeInterval      Currently selected time interval
     * @return String of sums per spending type
     */
    private void getTotalSpendings(int[] selectedDateArray, int timeInterval) {
        String spendingsFilePath = getFilesDir() + "/SpendingsMap";
        spendingsLiabilityTotal = 0;
        spendingsPersonalTotal = 0;
        spendingsSavingsTotal = 0;

        if (new File(spendingsFilePath).exists()) {
            int selectedMonth = selectedDateArray[0];
            int selectedDay = selectedDateArray[1];
            int selectedYear = selectedDateArray[2];
            Calendar calendar = new GregorianCalendar(selectedYear, selectedMonth, selectedDay);
            int[] timeIntervals = {1, 7, calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                    calendar.getActualMaximum(Calendar.DAY_OF_YEAR)};

            Calendar startingDate = (Calendar) calendar.clone(); // Start with selected date
            if (timeInterval == 1) {
                calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) - 1));
                startingDate = calendar;
            } else if (timeInterval == 2) {
                calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_MONTH) - 1));
                startingDate = calendar;
            } else if (timeInterval == 3) {
                calendar.add(Calendar.DAY_OF_YEAR, -(calendar.get(Calendar.DAY_OF_YEAR) - 1));
                startingDate = calendar;
            }

            HashMap<String, ArrayList<SpendingsClass>> spendingsMap = deserializeSpendingsMap(
                    spendingsFilePath);

            String[] typesStringArray = getResources().getStringArray(R.array.spending_type);
            String liabilityStringResource = typesStringArray[0];
            String personalStringResource = typesStringArray[1];
            String savingsStringResource = typesStringArray[2];

            for (int i = 0; i < timeIntervals[timeInterval]; i++) {
                int startMonth = startingDate.get(Calendar.MONTH);
                int startDay = startingDate.get(Calendar.DAY_OF_MONTH);
                int startYear = startingDate.get(Calendar.YEAR);
                int[] dateToConvert = {startMonth, startDay, startYear};
                String key = getDateKey(getDateToString(dateToConvert));
                if (spendingsMap.containsKey(key)) {
                    ArrayList<SpendingsClass> spendingsArrayList = spendingsMap.get(key);
                    for (int j = 0; j < spendingsArrayList.size(); j++) {
                        SpendingsClass spending = spendingsArrayList.get(j);
                        if (spending.getType().equals(liabilityStringResource)) {
                            spendingsLiabilityTotal += spending.getMoney();
                        } else if (spending.getType().equals(personalStringResource)) {
                            spendingsPersonalTotal += spending.getMoney();
                        } else if (spending.getType().equals(savingsStringResource)) {
                            spendingsSavingsTotal += spending.getMoney();
                        } else {
                            Log.e(TAG, "Error at getTotalSpendings: Couldn't find type");
                        }
                    }
                }
                startingDate.add(Calendar.DAY_OF_MONTH, 1);
            }

            spendingsTotal = spendingsLiabilityTotal + spendingsPersonalTotal +
                    spendingsSavingsTotal;
        } else {
            // No file exists
            spendingsLiabilityTotal = 0;
            spendingsPersonalTotal = 0;
            spendingsSavingsTotal = 0;
            Log.i(TAG,"Spendings file doesn't exist");
        }
    }

    /**
     * Deserializes Spendings Map file at given file location
     *
     * @param filePath File location of serialized Spendings Map file
     * @return HashMap of Spendings
     */
    private HashMap<String, ArrayList<SpendingsClass>> deserializeSpendingsMap(String filePath) {
        try {
            String fileContent = readFile(filePath, StandardCharsets.US_ASCII);
            Type type = new TypeToken<HashMap<String, ArrayList<SpendingsClass>>>() {
            }.getType();
            HashMap<String, ArrayList<SpendingsClass>> map = new Gson().fromJson(fileContent, type);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "IOException at deserializeSpendingsMap()");
            return null;
        }
    }

    //endregion

    //region Config methods

    /**
     * Loads Properties file for Time Interval or copies over a default file from assets
     * Mainly to keep TimeInterval when user reboots app
     *
     * @return Properties for Time Interval
     */
    private Properties getConfig() {
        try {
            configFile = new File(getFilesDir() + "/config.properties");
            InputStream assetsInputStream = getAssets().open("config.properties");
            Properties newProp = new Properties();

            /**
             * If configFile was created or is empty, copy default properties to file
             * Else show that file already exists
             */
            if (configFile.createNewFile() || (configFile.exists() && configFile.length() == 0)) {
                FileOutputStream fos = new FileOutputStream(getFilesDir() + "/config.properties");
                newProp.load(assetsInputStream);
                newProp.store(fos, "Properties for Budget App");
                fos.close();
                toast = Toast.makeText(getApplicationContext(), "New properties file created!", Toast.LENGTH_SHORT);
                toast.show();
                System.out.println("New properties file created at Internal Storage");
            } else {
                Log.e(TAG, "File already exists at: " + getFilesDir());
            }

            InputStream configInputStream = new FileInputStream(configFile);
            newProp.load(configInputStream);
            assetsInputStream.close();
            configInputStream.close();
            Log.i(TAG, "Success in loading config file");
            return newProp;
        } catch (IOException e) {
            Log.e(TAG, "Error getting config");
            return null;
        }
    }

    private void saveConfig() {
        prop.setProperty("timeInterval", Integer.toString(config.getTimeInterval()));
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            prop.store(fos, "Properties for Budget App");
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Config file not found at: OnDestroy");
        } catch (IOException e) {
            Log.e(TAG, "IOException @ onDestroy: " + e);
        }
    }
    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CALENDAR_REQUEST_CODE) { // Code for Calendar
            if (resultCode == Activity.RESULT_OK) {
                dateArray = intent.getIntArrayExtra("date");
                setupUI(dateArray);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUI(dateArray);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveConfig();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveConfig();
    }

}

