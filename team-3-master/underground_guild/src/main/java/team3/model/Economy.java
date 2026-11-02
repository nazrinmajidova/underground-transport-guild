package team3.model;

import java.io.Serializable;

/**
 * Tracks the financial state of the player. 
 * Handles income, expenses, and determines bankruptcy conditions.
 */
public class Economy implements Serializable {
    private float balance;
    private float totalIncome;
    private float totalExpenses;

    public Economy(float initialBalance) {
        this.balance = initialBalance;
        this.totalIncome = 0.0f;
        this.totalExpenses = 0.0f;
    }

    /**
     * Overloaded constructor to support existing Game class initializations
     * that pass a double value.
     */
    public Economy(double initialBalance) {
        this((float) initialBalance);
    }

    /**
     * Adds funds to the balance and updates total income.
     */
    public void earn(float amount, String reason) {
        if (amount > 0f) {
            balance += amount;
            totalIncome += amount;
        }
    }

    /**
     * Subtracts funds from the balance and updates total expenses.
     */
    public void charge(float amount, String reason) {
        if (amount > 0f) {
            balance -= amount;
            totalExpenses += amount;
        }
    }

    /**
     * Checks if the player has run out of money.
     * return true if the balance drops below 0, false otherwise.
     */
    public boolean isBankrupt() {
        return balance < 0f;
    }

    /**
     * Generates a readable summary string of the current financial state.
     * return A formatted multi-line string with balance, income, and expenses.
     */
    public String getFinancialReport() {
        return String.format(
            "Financial Report:\n" +
            "-----------------\n" +
            "Balance:        %.2f\n" +
            "Total Income:   %.2f\n" +
            "Total Expenses: %.2f", 
            balance, totalIncome, totalExpenses
        );
    }

    public float getBalance() { 
        return balance; 
    }
    
    public float getTotalIncome() { 
        return totalIncome; 
    }
    
    public float getTotalExpenses() { 
        return totalExpenses; 
    }
}
