package com.example.budgetappattempt2;

import androidx.annotation.NonNull;

public class BudgetClass {
    private double input;
    private String type;

    public BudgetClass(double input, String type){
        this.input = input;
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return this.input + "-" + this.type + " ";
    }

    public double getInput() {
        return input;
    }

    public void setInput(double input) {
        this.input = input;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
