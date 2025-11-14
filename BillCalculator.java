package com.hotel;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillCalculator {
    public static long nights(LocalDate in, LocalDate out) {
        return ChronoUnit.DAYS.between(in, out);
    }
    public static double total(double pricePerNight, LocalDate in, LocalDate out) {
        return pricePerNight * nights(in, out);
    }
}
