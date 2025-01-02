package ru.jadegg2568.randomitempillars.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    public static String getDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return currentDate.format(formatter);
    }

    public static String getMMSS(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
