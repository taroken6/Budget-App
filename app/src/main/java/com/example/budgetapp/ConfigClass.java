package com.example.budgetapp;
import android.content.Context;


public class ConfigClass {
    private String user;
    private int timeInterval;
    Context ctx;

    public void printAll(){
        System.out.println("User = " + this.user);
        System.out.println("Time Interval = " + timeInterval);
    }

    public void setUser(String user){
        this.user = user;
    }
    public String getUser(){
        return this.user;
    }

    public void setTimeInterval(int index){
        this.timeInterval = index;
    }
    public int getTimeInterval(){
        return timeInterval;
    }
}
