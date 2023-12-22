package net.pneumono.gravestones.gravestones;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GravestoneTime {
    public static SimpleDateFormat getSimpleDateFormat() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    public static String getCurrentTimeAsString() {
        SimpleDateFormat formatter = getSimpleDateFormat();
        Date date = new Date();
        return formatter.format(date);
    }

    public static long getDifferenceInSeconds(String aTime, String bTime) {
        SimpleDateFormat formatter = getSimpleDateFormat();
        try {
            Date aDate = formatter.parse(aTime);
            Date bDate = formatter.parse(bTime);
            long diffInMilli = aDate.getTime() - bDate.getTime();
            return TimeUnit.SECONDS.convert(diffInMilli, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
