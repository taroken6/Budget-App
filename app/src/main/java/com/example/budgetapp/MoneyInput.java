package com.example.budgetapp;

import android.os.Parcel;
import android.os.Parcelable;

public class MoneyInput implements Parcelable {
    private double money;
    private String type;
    private String description;
    private String date;

    public MoneyInput(double money, String type, String description, String date){
        this.money = money;
        this.type = type;
        this.description = description;
        this.date = date;
    }

    public MoneyInput(Parcel in){
        this.money = in.readDouble();
        this.type = in.readString();
        this.description = in.readString();
        this.date = in.readString();
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate(){
        return this.date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.money);
        dest.writeString(this.type);
        dest.writeString(this.description);
        dest.writeString(this.date);
    }

    public static final Parcelable.Creator<MoneyInput> CREATOR = new Parcelable.Creator<MoneyInput>(){
        public MoneyInput createFromParcel(Parcel in){
            return new MoneyInput(in);
        }

        public MoneyInput[] newArray(int size) {
            return new MoneyInput[size];
        }
    };
}
