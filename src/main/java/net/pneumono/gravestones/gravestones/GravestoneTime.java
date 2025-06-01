package net.pneumono.gravestones.gravestones;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GravestoneTime {
    public static final SimpleDateFormat READABLE = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static final SimpleDateFormat FILE_SAVING = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");

    public static long getDifferenceInSeconds(String aTime, String bTime) {
        SimpleDateFormat formatter = READABLE;
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
