package net.pneumono.gravestones.gravestones;

@SuppressWarnings("unused")
public enum TimeFormat {
    DDMMYYYY("dd/MM/yyyy", "HH:mm:ss"),
    MMDDYYYY("MM/dd/yyyy", "HH:mm:ss"),
    YYYYMMDD("yyyy/MM/dd", "HH:mm:ss");

    private final String dateFormat;
    private final String timeFormat;

    TimeFormat(String dateFormat, String timeFormat) {
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }
}
