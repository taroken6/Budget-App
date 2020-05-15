package com.example.budgetapp;

public class EarningsListObject {
    private String type;
    private double[] amount;

    public EarningsListObject(String type, double[] amount){
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[] getAmount() {
        return amount;
    }

    public void setAmount(double[] amount) {
        this.amount = amount;
    }
}
