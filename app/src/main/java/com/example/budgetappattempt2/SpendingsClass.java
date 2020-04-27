package com.example.budgetappattempt2;

import android.os.Parcel;
import android.os.Parcelable;

public class SpendingsClass implements Parcelable {
    private double money;
    private String type;
    private String description;
    private String date;
    private String category;

    public SpendingsClass(double money, String type, String description, String date,
                          String category){
        this.money = money;
        this.type = type;
        this.description = description;
        this.date = date;
        this.category = category;
    }

    public SpendingsClass(Parcel in){
        this.money = in.readDouble();
        this.type = in.readString();
        this.description = in.readString();
        this.date = in.readString();
        this.category = in.readString();
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
        dest.writeString(this.category);
    }

    public static final Parcelable.Creator<SpendingsClass> CREATOR = new
            Parcelable.Creator<SpendingsClass>(){
        public SpendingsClass createFromParcel(Parcel in){
            return new SpendingsClass(in);
        }

        public SpendingsClass[] newArray(int size) {
            return new SpendingsClass[size];
        }
    };
}
