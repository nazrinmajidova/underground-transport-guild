package team3.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EconomyTest {

    @Test
    void earnAndChargeUpdateBalanceIncomeAndExpenses() {
        Economy economy = new Economy(1000.0f);

        economy.earn(150.0f, "delivery");
        economy.charge(40.0f, "maintenance");

        assertEquals(1110.0f, economy.getBalance());
        assertEquals(150.0f, economy.getTotalIncome());
        assertEquals(40.0f, economy.getTotalExpenses());
    }

    @Test
    void bankruptcyTriggersOnlyWhenBalanceDropsBelowZero() {
        Economy economy = new Economy(10.0f);

        economy.charge(10.0f, "equalize");
        assertFalse(economy.isBankrupt());

        economy.charge(0.5f, "overdraw");
        assertTrue(economy.isBankrupt());
    }
}
