package net.pneumono.gravestones.gravestones;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GravestoneTime extends GravestoneManager {
    // Inconsistent date formats suck ass, but it's for the best here, I think.
    // READABLE needs to stay as it is for backwards compatibility.
    // FILE_SAVING should be in the US format, since that's (unfortunately) standard, and it actually gets viewed by the player.
    public static final SimpleDateFormat READABLE = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static final SimpleDateFormat FILE_SAVING = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");

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
