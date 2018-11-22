package com.sapfir.helpers;


import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Workshop {
    public static void main(String[] args) {
        BigDecimal unitOutcome = new BigDecimal("0");
        BigDecimal betUnits = new BigDecimal("1");
        BigDecimal betUnitsQuarterGoal = new BigDecimal("0.5");
        BigDecimal userPickValue = new BigDecimal("2.03");
        String result = "not-played";

        switch (result) {
            case "won":
                unitOutcome = userPickValue.subtract(betUnits);
                break;
            case "lost":
                unitOutcome = unitOutcome.subtract(betUnits);
                break;
            case "void-won":
                unitOutcome = betUnitsQuarterGoal.multiply(userPickValue).add(betUnitsQuarterGoal).subtract(betUnits);
                break;
            case "void-lost":
                unitOutcome = unitOutcome.subtract(betUnitsQuarterGoal);
                break;
            case "void":
                break;
            default:
                System.out.println("Result not supported");
        }

        System.out.println(unitOutcome);
    }
}
